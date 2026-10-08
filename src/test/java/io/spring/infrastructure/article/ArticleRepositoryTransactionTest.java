package io.spring.infrastructure.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.mapper.ArticleMapper;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ArticleRepositoryTransactionTest {
  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleMapper articleMapper;

  private User user;
  private String suffix;

  @BeforeEach
  public void setUp() {
    suffix = UUID.randomUUID().toString().substring(0, 8);
    user = new User(suffix + "@gmail.com", "user" + suffix, "123", "bio", "default");
    userRepository.save(user);
  }

  @Test
  public void transactional_test() {
    Article article =
        new Article(
            "test " + suffix, "desc", "body", Arrays.asList("java", "spring"), user.getId());
    articleRepository.save(article);
    Article anotherArticle =
        new Article(
            "test " + suffix,
            "desc",
            "body",
            Arrays.asList("java", "spring", "other" + suffix),
            user.getId());

    Assertions.assertThrows(
        DataAccessException.class, () -> articleRepository.save(anotherArticle));

    Assertions.assertNull(
        articleMapper.findTag("other" + suffix), "tags written before the failure must roll back");
    Assertions.assertNull(articleMapper.findById(anotherArticle.getId()));
  }

  @Test
  public void should_commit_article_and_tags_together() {
    Article article =
        new Article(
            "commit " + suffix,
            "desc",
            "body",
            Arrays.asList("tag-a-" + suffix, "tag-b-" + suffix),
            user.getId());

    articleRepository.save(article);

    Optional<Article> fetched = articleRepository.findById(article.getId());
    Assertions.assertTrue(fetched.isPresent());
    Assertions.assertEquals(article.getCreatedAt(), fetched.get().getCreatedAt());
    Assertions.assertTrue(fetched.get().getTags().contains(new Tag("tag-a-" + suffix)));
    Assertions.assertTrue(fetched.get().getTags().contains(new Tag("tag-b-" + suffix)));
    Assertions.assertNotNull(articleMapper.findTag("tag-a-" + suffix));
    Assertions.assertNotNull(articleMapper.findTag("tag-b-" + suffix));
  }

  @Test
  public void should_keep_first_article_when_duplicate_slug_rolls_back() {
    Article article =
        new Article("dup " + suffix, "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);
    Article duplicate =
        new Article("dup " + suffix, "desc2", "body2", Arrays.asList("java"), user.getId());

    Assertions.assertThrows(DataAccessException.class, () -> articleRepository.save(duplicate));

    Optional<Article> fetched = articleRepository.findBySlug(article.getSlug());
    Assertions.assertTrue(fetched.isPresent());
    Assertions.assertEquals(article.getId(), fetched.get().getId());
    Assertions.assertEquals("desc", fetched.get().getDescription());
  }
}
