package io.spring.regression.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.regression.RegressionFixtures;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Verifies that repository writes participate in Spring-managed transactions and roll back. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class RepositoryTransactionRollbackTest {

  @DynamicPropertySource
  static void freshSchemaOnlyDatabase(DynamicPropertyRegistry registry) throws Exception {
    Path dir = Paths.get("build", "regression-db");
    Files.createDirectories(dir);
    Path db = dir.resolve("rollback-" + UUID.randomUUID() + ".db");
    db.toFile().deleteOnExit();
    registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db.toAbsolutePath());
    registry.add("spring.flyway.target", () -> "1");
  }

  @Autowired ArticleRepository articleRepository;
  @Autowired UserRepository userRepository;
  @Autowired JdbcTemplate jdbcTemplate;
  @Autowired PlatformTransactionManager transactionManager;

  private int count(String table) {
    return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
  }

  @Test
  void article_and_tag_rows_roll_back_when_surrounding_transaction_fails() {
    User author = RegressionFixtures.user("tx-author");
    userRepository.save(author);
    int tagsBefore = count("tags");
    int articleTagsBefore = count("article_tags");
    int articlesBefore = count("articles");

    TransactionTemplate tx = new TransactionTemplate(transactionManager);
    Article article =
        new Article("Rolled Back", "d", "b", Arrays.asList("tx-a", "tx-b"), author.getId());
    assertThrows(
        IllegalStateException.class,
        () ->
            tx.executeWithoutResult(
                status -> {
                  articleRepository.save(article);
                  assertTrue(articleRepository.findById(article.getId()).isPresent());
                  throw new IllegalStateException("boom");
                }));

    assertFalse(articleRepository.findById(article.getId()).isPresent());
    assertFalse(articleRepository.findBySlug("rolled-back").isPresent());
    assertEquals(articlesBefore, count("articles"));
    assertEquals(tagsBefore, count("tags"));
    assertEquals(articleTagsBefore, count("article_tags"));
  }

  @Test
  void explicit_rollback_only_discards_user_insert() {
    TransactionTemplate tx = new TransactionTemplate(transactionManager);
    User user = RegressionFixtures.user("tx-rollback-only");
    tx.executeWithoutResult(
        status -> {
          userRepository.save(user);
          status.setRollbackOnly();
        });
    assertFalse(userRepository.findById(user.getId()).isPresent());
    assertFalse(userRepository.findByUsername("tx-rollback-only").isPresent());
  }

  @Test
  void committed_transaction_persists_everything() {
    TransactionTemplate tx = new TransactionTemplate(transactionManager);
    User author = RegressionFixtures.user("tx-commit");
    Article article = new Article("Committed", "d", "b", Arrays.asList("tx-c"), author.getId());
    tx.executeWithoutResult(
        status -> {
          userRepository.save(author);
          articleRepository.save(article);
        });
    assertTrue(userRepository.findById(author.getId()).isPresent());
    assertTrue(articleRepository.findBySlug("committed").isPresent());
    assertEquals(
        Integer.valueOf(1),
        jdbcTemplate.queryForObject(
            "select count(*) from article_tags where article_id = ?",
            Integer.class,
            article.getId()));
  }

  @Test
  void failed_duplicate_insert_does_not_leave_partial_rows() {
    User author = RegressionFixtures.user("tx-dup");
    userRepository.save(author);
    articleRepository.save(
        new Article("Dup Slug", "d", "b", Arrays.asList("dup-1"), author.getId()));
    int tagsBefore = count("tags");
    int articleTagsBefore = count("article_tags");
    Article duplicate =
        new Article("Dup Slug", "d", "b", Arrays.asList("dup-1", "dup-2"), author.getId());
    assertThrows(Exception.class, () -> articleRepository.save(duplicate));
    assertEquals(tagsBefore, count("tags"), "tags inserted before the failing insert roll back");
    assertEquals(articleTagsBefore, count("article_tags"));
    assertFalse(articleRepository.findById(duplicate.getId()).isPresent());
  }
}
