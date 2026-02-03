package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleTest2 {

  @Test
  void constructor_withTagList() {
    Article article =
        new Article("Title", "Description", "Body", Arrays.asList("java", "spring"), "user-id");

    assertNotNull(article.getId());
    assertEquals("title", article.getSlug());
    assertEquals("Title", article.getTitle());
    assertEquals("Description", article.getDescription());
    assertEquals("Body", article.getBody());
    assertEquals("user-id", article.getUserId());
    assertEquals(2, article.getTags().size());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
  }

  @Test
  void constructor_withCreatedAt() {
    DateTime createdAt = new DateTime(2023, 1, 15, 10, 30, 0);
    Article article =
        new Article(
            "Title", "Description", "Body", Arrays.asList("java"), "user-id", createdAt);

    assertEquals(createdAt, article.getCreatedAt());
    assertEquals(createdAt, article.getUpdatedAt());
  }

  @Test
  void constructor_withEmptyTagList() {
    Article article =
        new Article("Title", "Description", "Body", Collections.emptyList(), "user-id");

    assertTrue(article.getTags().isEmpty());
  }

  @Test
  void constructor_withDuplicateTags() {
    Article article =
        new Article("Title", "Description", "Body", Arrays.asList("java", "java", "spring"), "user-id");

    assertEquals(2, article.getTags().size());
  }

  @Test
  void noArgsConstructor() {
    Article article = new Article();

    assertNull(article.getId());
    assertNull(article.getSlug());
    assertNull(article.getTitle());
    assertNull(article.getDescription());
    assertNull(article.getBody());
    assertNull(article.getUserId());
    assertNull(article.getTags());
    assertNull(article.getCreatedAt());
    assertNull(article.getUpdatedAt());
  }

  @Test
  void update_allFields() {
    Article article =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), "user-id");
    DateTime originalUpdatedAt = article.getUpdatedAt();

    article.update("New Title", "New Description", "New Body");

    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("New Description", article.getDescription());
    assertEquals("New Body", article.getBody());
  }

  @Test
  void update_onlyTitle() {
    Article article =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), "user-id");

    article.update("New Title", null, null);

    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("Old Description", article.getDescription());
    assertEquals("Old Body", article.getBody());
  }

  @Test
  void update_onlyDescription() {
    Article article =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), "user-id");

    article.update(null, "New Description", null);

    assertEquals("Old Title", article.getTitle());
    assertEquals("New Description", article.getDescription());
    assertEquals("Old Body", article.getBody());
  }

  @Test
  void update_onlyBody() {
    Article article =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), "user-id");

    article.update(null, null, "New Body");

    assertEquals("Old Title", article.getTitle());
    assertEquals("Old Description", article.getDescription());
    assertEquals("New Body", article.getBody());
  }

  @Test
  void update_emptyStrings() {
    Article article =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), "user-id");

    article.update("", "", "");

    assertEquals("Old Title", article.getTitle());
    assertEquals("Old Description", article.getDescription());
    assertEquals("Old Body", article.getBody());
  }

  @Test
  void toSlug_withSpecialCharacters() {
    assertEquals("how-to-train-your-dragon", Article.toSlug("How to Train Your Dragon"));
    assertEquals("hello-world", Article.toSlug("Hello World"));
    assertEquals("test-article", Article.toSlug("Test Article"));
  }

  @Test
  void toSlug_withAmpersand() {
    assertEquals("java-spring", Article.toSlug("Java & Spring"));
  }

  @Test
  void toSlug_withQuotes() {
    String result = Article.toSlug("It's a Test");
    assertNotNull(result);
    assertTrue(result.contains("it"));
  }

  @Test
  void equals_sameId() {
    Article article1 =
        new Article("Title1", "Desc1", "Body1", Arrays.asList("java"), "user1");
    Article article2 =
        new Article("Title2", "Desc2", "Body2", Arrays.asList("spring"), "user2");

    assertNotEquals(article1, article2);
  }

  @Test
  void equals_sameArticle() {
    Article article =
        new Article("Title", "Description", "Body", Arrays.asList("java"), "user-id");

    assertEquals(article, article);
  }

  @Test
  void hashCode_consistent() {
    Article article =
        new Article("Title", "Description", "Body", Arrays.asList("java"), "user-id");
    int hash1 = article.hashCode();
    int hash2 = article.hashCode();

    assertEquals(hash1, hash2);
  }
}
