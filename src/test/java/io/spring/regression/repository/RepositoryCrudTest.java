package io.spring.regression.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import io.spring.regression.RegressionFixtures;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full CRUD + constraint coverage of every MyBatis repository against the V1 schema (test profile,
 * in-memory SQLite, each test rolled back by {@link MybatisTest}).
 */
@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  MyBatisUserRepository.class,
  MyBatisArticleRepository.class,
  MyBatisCommentRepository.class,
  MyBatisArticleFavoriteRepository.class
})
class RepositoryCrudTest {

  @Autowired UserRepository userRepository;
  @Autowired ArticleRepository articleRepository;
  @Autowired CommentRepository commentRepository;
  @Autowired ArticleFavoriteRepository favoriteRepository;
  @Autowired JdbcTemplate jdbcTemplate;

  private User savedUser(String seed) {
    User user = RegressionFixtures.user(seed);
    userRepository.save(user);
    return user;
  }

  @Nested
  class Users {
    @Test
    void save_and_find_by_id_username_email() {
      User user = savedUser("alice");
      assertEquals(Optional.of(user), userRepository.findById(user.getId()));
      assertEquals(Optional.of(user), userRepository.findByUsername("alice"));
      assertEquals(Optional.of(user), userRepository.findByEmail("alice@example.com"));
      User loaded = userRepository.findById(user.getId()).get();
      assertEquals("bio alice", loaded.getBio());
      assertEquals("img-alice", loaded.getImage());
      assertEquals("secret-alice", loaded.getPassword());
      assertFalse(userRepository.findById("nope").isPresent());
      assertFalse(userRepository.findByUsername("nope").isPresent());
      assertFalse(userRepository.findByEmail("nope@x").isPresent());
    }

    @Test
    void save_existing_user_updates_row() {
      User user = savedUser("bob");
      user.update("bob2@example.com", "bob2", "pw2", "bio2", "img2");
      userRepository.save(user);
      User loaded = userRepository.findById(user.getId()).get();
      assertEquals("bob2@example.com", loaded.getEmail());
      assertEquals("bob2", loaded.getUsername());
      assertEquals("pw2", loaded.getPassword());
      assertEquals("bio2", loaded.getBio());
      assertEquals("img2", loaded.getImage());
      assertFalse(userRepository.findByUsername("bob").isPresent());
      assertEquals(
          Integer.valueOf(1),
          jdbcTemplate.queryForObject("select count(*) from users", Integer.class));
    }

    @Test
    void duplicate_username_or_email_violates_unique_constraint() {
      savedUser("carol");
      User sameName = new User("other@example.com", "carol", "pw", "", "");
      assertThrows(DataAccessException.class, () -> userRepository.save(sameName));
      User sameEmail = new User("carol@example.com", "other", "pw", "", "");
      assertThrows(DataAccessException.class, () -> userRepository.save(sameEmail));
    }

    @Test
    void follow_relation_lifecycle() {
      User a = savedUser("fa");
      User b = savedUser("fb");
      assertFalse(userRepository.findRelation(a.getId(), b.getId()).isPresent());
      FollowRelation relation = new FollowRelation(a.getId(), b.getId());
      userRepository.saveRelation(relation);
      assertEquals(Optional.of(relation), userRepository.findRelation(a.getId(), b.getId()));
      assertFalse(userRepository.findRelation(b.getId(), a.getId()).isPresent());
      userRepository.removeRelation(relation);
      assertFalse(userRepository.findRelation(a.getId(), b.getId()).isPresent());
    }
  }

  @Nested
  class Articles {
    @Test
    void save_find_by_id_and_slug_with_tags() {
      User author = savedUser("author");
      Article article =
          new Article(
              "Hello Repo", "d", "b", Arrays.asList("java", "spring", "java"), author.getId());
      articleRepository.save(article);

      Optional<Article> byId = articleRepository.findById(article.getId());
      Optional<Article> bySlug = articleRepository.findBySlug("hello-repo");
      assertEquals(Optional.of(article), byId);
      assertEquals(Optional.of(article), bySlug);
      Article loaded = byId.get();
      assertEquals("Hello Repo", loaded.getTitle());
      assertEquals(author.getId(), loaded.getUserId());
      assertEquals(
          Arrays.asList("java", "spring"),
          loaded.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList()));
      assertEquals(
          RegressionFixtures.millisOf(article.getCreatedAt()),
          RegressionFixtures.millisOf(loaded.getCreatedAt()));
      assertEquals(
          Integer.valueOf(2),
          jdbcTemplate.queryForObject("select count(*) from tags", Integer.class));
      assertEquals(
          Integer.valueOf(2),
          jdbcTemplate.queryForObject("select count(*) from article_tags", Integer.class));
      assertFalse(articleRepository.findBySlug("missing").isPresent());
      assertFalse(articleRepository.findById("missing").isPresent());
    }

    @Test
    void saving_second_article_reuses_existing_tags() {
      User author = savedUser("author2");
      articleRepository.save(
          new Article("One", "d", "b", Arrays.asList("shared", "a"), author.getId()));
      articleRepository.save(
          new Article("Two", "d", "b", Arrays.asList("shared", "b"), author.getId()));
      assertEquals(
          Integer.valueOf(3),
          jdbcTemplate.queryForObject("select count(*) from tags", Integer.class));
      assertEquals(
          Integer.valueOf(4),
          jdbcTemplate.queryForObject("select count(*) from article_tags", Integer.class));
    }

    @Test
    void update_persists_new_title_slug_body_and_updated_at() {
      User author = savedUser("author3");
      Article article =
          new Article(
              "Original",
              "d",
              "b",
              Collections.emptyList(),
              author.getId(),
              RegressionFixtures.atMillis(1000L));
      articleRepository.save(article);
      article.update("Changed Title", "new d", "new b");
      articleRepository.save(article);
      Article loaded = articleRepository.findBySlug("changed-title").get();
      assertEquals("Changed Title", loaded.getTitle());
      assertEquals("new d", loaded.getDescription());
      assertEquals("new b", loaded.getBody());
      assertEquals(1000L, RegressionFixtures.millisOf(loaded.getCreatedAt()));
      assertFalse(articleRepository.findBySlug("original").isPresent());
    }

    @Test
    void duplicate_slug_violates_unique_constraint() {
      User author = savedUser("author4");
      articleRepository.save(
          new Article("Same", "d", "b", Collections.emptyList(), author.getId()));
      Article duplicate = new Article("Same", "d", "b", Collections.emptyList(), author.getId());
      assertThrows(DataAccessException.class, () -> articleRepository.save(duplicate));
    }

    @Test
    void remove_deletes_article() {
      User author = savedUser("author5");
      Article article = new Article("Gone", "d", "b", Arrays.asList("t"), author.getId());
      articleRepository.save(article);
      articleRepository.remove(article);
      assertFalse(articleRepository.findById(article.getId()).isPresent());
      assertFalse(articleRepository.findBySlug("gone").isPresent());
    }
  }

  @Nested
  class Comments {
    @Test
    void save_find_and_remove_comment() {
      User author = savedUser("c-author");
      Article article = new Article("Commented", "d", "b", Collections.emptyList(), author.getId());
      articleRepository.save(article);
      Comment comment = new Comment("nice", author.getId(), article.getId());
      commentRepository.save(comment);

      Optional<Comment> loaded = commentRepository.findById(article.getId(), comment.getId());
      assertEquals(Optional.of(comment), loaded);
      assertEquals("nice", loaded.get().getBody());
      assertEquals(author.getId(), loaded.get().getUserId());
      assertNotNull(loaded.get().getCreatedAt());
      assertFalse(commentRepository.findById("other-article", comment.getId()).isPresent());
      assertFalse(commentRepository.findById(article.getId(), "missing").isPresent());

      commentRepository.remove(comment);
      assertFalse(commentRepository.findById(article.getId(), comment.getId()).isPresent());
    }
  }

  @Nested
  class Favorites {
    @Test
    void save_find_and_remove_favorite() {
      User user = savedUser("f-user");
      Article article = new Article("Fav", "d", "b", Collections.emptyList(), user.getId());
      articleRepository.save(article);
      ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
      assertFalse(favoriteRepository.find(article.getId(), user.getId()).isPresent());
      favoriteRepository.save(favorite);
      assertEquals(Optional.of(favorite), favoriteRepository.find(article.getId(), user.getId()));
      favoriteRepository.remove(favorite);
      assertFalse(favoriteRepository.find(article.getId(), user.getId()).isPresent());
    }

    @Test
    void favorite_twice_violates_composite_primary_key() {
      User user = savedUser("f-user2");
      Article article = new Article("Fav2", "d", "b", Collections.emptyList(), user.getId());
      articleRepository.save(article);
      favoriteRepository.save(new ArticleFavorite(article.getId(), user.getId()));
      try {
        favoriteRepository.save(new ArticleFavorite(article.getId(), user.getId()));
      } catch (DataAccessException expected) {
        // composite primary key rejects the duplicate
      }
      assertEquals(
          Integer.valueOf(1),
          jdbcTemplate.queryForObject(
              "select count(*) from article_favorites where article_id = ? and user_id = ?",
              Integer.class,
              article.getId(),
              user.getId()));
    }
  }
}
