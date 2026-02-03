package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  private User testUser;

  @BeforeEach
  void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @Test
  void createArticle_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, testUser);

    assertNotNull(result);
    assertEquals("Test Title", result.getTitle());
    assertEquals("Test Description", result.getDescription());
    assertEquals("Test Body", result.getBody());
    assertEquals(testUser.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void createArticle_withEmptyTagList() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList())
            .build();

    Article result = articleCommandService.createArticle(param, testUser);

    assertNotNull(result);
    assertTrue(result.getTags().isEmpty());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void updateArticle_success() {
    Article existingArticle =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), testUser.getId());

    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Description");

    Article result = articleCommandService.updateArticle(existingArticle, param);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("New Description", result.getDescription());
    assertEquals("New Body", result.getBody());
    verify(articleRepository).save(eq(existingArticle));
  }

  @Test
  void updateArticle_withPartialUpdate() {
    Article existingArticle =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), testUser.getId());

    UpdateArticleParam param = new UpdateArticleParam("New Title", null, null);

    Article result = articleCommandService.updateArticle(existingArticle, param);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    verify(articleRepository).save(eq(existingArticle));
  }
}
