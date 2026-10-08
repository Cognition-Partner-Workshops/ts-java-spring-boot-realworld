package io.spring.infrastructure;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;

@Import({MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class UniqueConstraintTest extends DbTestBase {
  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("unique@test.com", "unique", "123", "", "");
    userRepository.save(user);
  }

  @Test
  public void should_reject_duplicate_slug() {
    articleRepository.save(
        new Article("same title", "desc", "body", Arrays.asList("java"), user.getId()));
    Article duplicate =
        new Article("same title", "other", "other", Arrays.asList("spring"), user.getId());

    DataAccessException e =
        Assertions.assertThrows(DataAccessException.class, () -> articleRepository.save(duplicate));
    Assertions.assertTrue(e.getMessage().contains("articles.slug"), e.getMessage());
    Assertions.assertFalse(articleRepository.findById(duplicate.getId()).isPresent());
  }

  @Test
  public void should_reject_duplicate_username() {
    User duplicate = new User("another@test.com", "unique", "123", "", "");

    DataAccessException e =
        Assertions.assertThrows(DataAccessException.class, () -> userRepository.save(duplicate));
    Assertions.assertTrue(e.getMessage().contains("users.username"), e.getMessage());
    Assertions.assertFalse(userRepository.findByEmail("another@test.com").isPresent());
  }

  @Test
  public void should_reject_duplicate_email() {
    User duplicate = new User("unique@test.com", "another", "123", "", "");

    DataAccessException e =
        Assertions.assertThrows(DataAccessException.class, () -> userRepository.save(duplicate));
    Assertions.assertTrue(e.getMessage().contains("users.email"), e.getMessage());
    Assertions.assertFalse(userRepository.findByUsername("another").isPresent());
  }
}
