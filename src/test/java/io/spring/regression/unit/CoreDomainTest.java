package io.spring.regression.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.Util;
import io.spring.core.article.Article;
import io.spring.core.article.Tag;
import io.spring.core.comment.Comment;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.regression.RegressionFixtures;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoreDomainTest {

  @Nested
  class ArticleTests {
    @Test
    void constructor_assigns_id_slug_and_timestamps() {
      Article article =
          new Article("Hello World", "desc", "body", Arrays.asList("a", "b", "a"), "user-1");
      assertNotNull(article.getId());
      assertEquals("hello-world", article.getSlug());
      assertEquals("Hello World", article.getTitle());
      assertEquals("desc", article.getDescription());
      assertEquals("body", article.getBody());
      assertEquals("user-1", article.getUserId());
      assertNotNull(article.getCreatedAt());
      assertEquals(article.getCreatedAt(), article.getUpdatedAt());
      List<String> tagNames =
          article.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList());
      assertEquals(Arrays.asList("a", "b"), tagNames, "duplicate tags are collapsed");
    }

    @Test
    void constructor_with_explicit_created_at_uses_it_for_both_timestamps() {
      Article article =
          new Article(
              "T", "d", "b", Collections.emptyList(), "u", RegressionFixtures.atMillis(1000L));
      assertEquals(1000L, RegressionFixtures.millisOf(article.getCreatedAt()));
      assertEquals(1000L, RegressionFixtures.millisOf(article.getUpdatedAt()));
      assertTrue(article.getTags().isEmpty());
    }

    @Test
    void update_changes_only_non_empty_fields_and_bumps_updated_at() throws Exception {
      Article article =
          new Article(
              "Old Title",
              "old desc",
              "old body",
              Collections.emptyList(),
              "u",
              RegressionFixtures.atMillis(0L));
      article.update("", "", "");
      assertEquals("Old Title", article.getTitle());
      assertEquals("old-title", article.getSlug());
      assertEquals(0L, RegressionFixtures.millisOf(article.getUpdatedAt()));

      article.update(null, null, null);
      assertEquals("old desc", article.getDescription());
      assertEquals(0L, RegressionFixtures.millisOf(article.getUpdatedAt()));

      article.update("New Title", null, null);
      assertEquals("New Title", article.getTitle());
      assertEquals("new-title", article.getSlug());
      assertEquals("old desc", article.getDescription());
      assertTrue(RegressionFixtures.millisOf(article.getUpdatedAt()) > 0L);

      article.update(null, "new desc", null);
      assertEquals("new desc", article.getDescription());
      assertEquals("old body", article.getBody());

      article.update(null, null, "new body");
      assertEquals("new body", article.getBody());
      assertEquals(0L, RegressionFixtures.millisOf(article.getCreatedAt()));
    }

    @ParameterizedTest
    @CsvSource({
      "'Hello World','hello-world'",
      "'What is Spring?','what-is-spring-'",
      "'A & B','a-b'",
      "'UPPER lower','upper-lower'",
      "'comma, separated. words','comma-separated-words'",
      "'tabs\tand  multiple   spaces','tabs-and-multiple-spaces'",
      "'already-slugged','already-slugged'",
      "'Ünïcödé Tïtlé','ünïcödé-tïtlé'",
    })
    void to_slug_normalises_titles(String title, String expected) {
      assertEquals(expected, Article.toSlug(title));
    }

    @Test
    void to_slug_replaces_fullwidth_cjk_punctuation() {
      assertEquals("中文-标题", Article.toSlug("中文：标题"));
      assertEquals("quote-s-and-“quotes-", Article.toSlug("quote’s and “quotes”"));
    }

    @Test
    void equality_is_by_id_only() {
      Article a = new Article("T", "d", "b", Collections.emptyList(), "u");
      Article b = new Article("T", "d", "b", Collections.emptyList(), "u");
      assertNotEquals(a, b);
      assertEquals(a, a);
      assertEquals(a.hashCode(), a.hashCode());
      assertNotEquals(a.hashCode(), b.hashCode());
      assertFalse(a.equals(null));
      assertFalse(a.equals("not an article"));
      assertNotNull(new Article());
    }
  }

  @Nested
  class TagTests {
    @Test
    void tags_with_same_name_are_equal_regardless_of_id() {
      Tag a = new Tag("java");
      Tag b = new Tag("java");
      assertNotEquals(a.getId(), b.getId());
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertNotEquals(a, new Tag("spring"));
      assertEquals(1, new HashSet<>(Arrays.asList(a, b)).size());
      assertTrue(a.toString().contains("java"));
    }

    @Test
    void no_args_constructor_and_setters() {
      Tag tag = new Tag();
      assertNull(tag.getId());
      tag.setId("id");
      tag.setName("name");
      assertEquals("id", tag.getId());
      assertEquals("name", tag.getName());
    }
  }

  @Nested
  class UserTests {
    @Test
    void constructor_assigns_random_id() {
      User user = new User("e@e.com", "name", "pw", "bio", "img");
      assertNotNull(user.getId());
      assertEquals("e@e.com", user.getEmail());
      assertEquals("name", user.getUsername());
      assertEquals("pw", user.getPassword());
      assertEquals("bio", user.getBio());
      assertEquals("img", user.getImage());
      assertNotNull(new User());
    }

    @Test
    void update_skips_null_and_empty_values() {
      User user = new User("e@e.com", "name", "pw", "bio", "img");
      user.update("", "", "", "", "");
      assertEquals("e@e.com", user.getEmail());
      user.update(null, null, null, null, null);
      assertEquals("name", user.getUsername());

      user.update("new@e.com", null, null, null, null);
      assertEquals("new@e.com", user.getEmail());
      user.update(null, "newname", null, null, null);
      assertEquals("newname", user.getUsername());
      user.update(null, null, "newpw", null, null);
      assertEquals("newpw", user.getPassword());
      user.update(null, null, null, "newbio", null);
      assertEquals("newbio", user.getBio());
      user.update(null, null, null, null, "newimg");
      assertEquals("newimg", user.getImage());
    }

    @Test
    void equality_is_by_id() {
      User a = new User("e@e.com", "name", "pw", "bio", "img");
      User b = new User("e@e.com", "name", "pw", "bio", "img");
      assertNotEquals(a, b);
      assertEquals(a, a);
      assertNotEquals(a.hashCode(), b.hashCode());
    }
  }

  @Nested
  class RelationAndFavoriteTests {
    @Test
    void follow_relation_is_a_value_object() {
      FollowRelation a = new FollowRelation("u1", "u2");
      FollowRelation b = new FollowRelation("u1", "u2");
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertEquals("u1", a.getUserId());
      assertEquals("u2", a.getTargetId());
      assertNotEquals(a, new FollowRelation("u2", "u1"));
      FollowRelation empty = new FollowRelation();
      empty.setUserId("x");
      empty.setTargetId("y");
      assertEquals(new FollowRelation("x", "y"), empty);
      assertTrue(a.toString().contains("u1"));
    }

    @Test
    void article_favorite_is_a_value_object() {
      ArticleFavorite a = new ArticleFavorite("a1", "u1");
      ArticleFavorite b = new ArticleFavorite("a1", "u1");
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertNotEquals(a, new ArticleFavorite("a1", "u2"));
      assertNotEquals(a, new ArticleFavorite("a2", "u1"));
      assertEquals("a1", a.getArticleId());
      assertEquals("u1", a.getUserId());
      assertNull(new ArticleFavorite().getArticleId());
    }

    @Test
    void comment_has_id_and_created_at() {
      Comment c = new Comment("body", "u1", "a1");
      assertNotNull(c.getId());
      assertNotNull(c.getCreatedAt());
      assertEquals("body", c.getBody());
      assertEquals("u1", c.getUserId());
      assertEquals("a1", c.getArticleId());
      assertNotEquals(c, new Comment("body", "u1", "a1"));
      assertEquals(c, c);
      assertNull(new Comment().getId());
    }
  }

  @Nested
  class AuthorizationServiceTests {
    @Test
    void only_author_can_write_article() {
      User author = RegressionFixtures.user("author");
      User other = RegressionFixtures.user("other");
      Article article = RegressionFixtures.article("a", author);
      assertTrue(AuthorizationService.canWriteArticle(author, article));
      assertFalse(AuthorizationService.canWriteArticle(other, article));
    }

    @Test
    void comment_author_or_article_author_can_write_comment() {
      User author = RegressionFixtures.user("author");
      User commenter = RegressionFixtures.user("commenter");
      User stranger = RegressionFixtures.user("stranger");
      Article article = RegressionFixtures.article("a", author);
      Comment comment = new Comment("c", commenter.getId(), article.getId());
      assertTrue(AuthorizationService.canWriteComment(author, article, comment));
      assertTrue(AuthorizationService.canWriteComment(commenter, article, comment));
      assertFalse(AuthorizationService.canWriteComment(stranger, article, comment));
      assertNotNull(new AuthorizationService());
    }
  }

  @Test
  void util_is_empty() {
    assertTrue(Util.isEmpty(null));
    assertTrue(Util.isEmpty(""));
    assertFalse(Util.isEmpty(" "));
    assertFalse(Util.isEmpty("x"));
    assertNotNull(new Util());
  }
}
