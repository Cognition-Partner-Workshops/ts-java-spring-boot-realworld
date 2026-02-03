package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  public void should_create_article_successfully() {
    User creator = new User("test@test.com", "testuser", "password", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, creator);

    assertNotNull(result);
    assertEquals("Test Article", result.getTitle());
    assertEquals("Test Description", result.getDescription());
    assertEquals("Test Body", result.getBody());
    assertEquals(creator.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_without_tags() {
    User creator = new User("test@test.com", "testuser", "password", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(Collections.emptyList())
            .build();

    Article result = articleCommandService.createArticle(param, creator);

    assertNotNull(result);
    assertEquals("Test Article", result.getTitle());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_successfully() {
    User creator = new User("test@test.com", "testuser", "password", "", "");
    Article article = new Article("Original Title", "Original Description", "Original Body", Collections.emptyList(), creator.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("Updated Title", "Updated Body", "Updated Description");

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    assertEquals("Updated Description", result.getDescription());
    assertEquals("Updated Body", result.getBody());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_with_partial_data() {
    User creator = new User("test@test.com", "testuser", "password", "", "");
    Article article = new Article("Original Title", "Original Description", "Original Body", Collections.emptyList(), creator.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("Updated Title", null, null);

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    verify(articleRepository).save(article);
  }
}
