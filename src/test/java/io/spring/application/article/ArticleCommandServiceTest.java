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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  private User creator;

  @BeforeEach
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
    creator = new User("creator@email.com", "creator", "password", "bio", "image");
  }

  @Test
  public void should_create_article_with_all_fields() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertEquals("Test Article", article.getTitle());
    assertEquals("Test Description", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals("test-article", article.getSlug());
    assertEquals(creator.getId(), article.getUserId());
    assertEquals(2, article.getTags().size());

    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_without_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Article Without Tags")
            .description("Description")
            .body("Body")
            .tagList(Collections.emptyList())
            .build();

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertEquals("Article Without Tags", article.getTitle());
    assertTrue(article.getTags().isEmpty());

    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_save_article_to_repository() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Description")
            .body("Body")
            .tagList(Arrays.asList("java"))
            .build();

    articleCommandService.createArticle(param, creator);

    ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository).save(articleCaptor.capture());

    Article savedArticle = articleCaptor.getValue();
    assertEquals("Test Article", savedArticle.getTitle());
    assertEquals(creator.getId(), savedArticle.getUserId());
  }

  @Test
  public void should_update_article_title() {
    Article article =
        new Article("Old Title", "Description", "Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "", "");

    Article updatedArticle = articleCommandService.updateArticle(article, updateParam);

    assertEquals("New Title", updatedArticle.getTitle());
    assertEquals("new-title", updatedArticle.getSlug());
    assertEquals("Description", updatedArticle.getDescription());
    assertEquals("Body", updatedArticle.getBody());

    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_description() {
    Article article =
        new Article("Title", "Old Description", "Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "New Description");

    Article updatedArticle = articleCommandService.updateArticle(article, updateParam);

    assertEquals("Title", updatedArticle.getTitle());
    assertEquals("New Description", updatedArticle.getDescription());
    assertEquals("Body", updatedArticle.getBody());

    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_body() {
    Article article =
        new Article("Title", "Description", "Old Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("", "New Body", "");

    Article updatedArticle = articleCommandService.updateArticle(article, updateParam);

    assertEquals("Title", updatedArticle.getTitle());
    assertEquals("Description", updatedArticle.getDescription());
    assertEquals("New Body", updatedArticle.getBody());

    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_multiple_fields() {
    Article article =
        new Article(
            "Old Title", "Old Description", "Old Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam updateParam =
        new UpdateArticleParam("New Title", "New Body", "New Description");

    Article updatedArticle = articleCommandService.updateArticle(article, updateParam);

    assertEquals("New Title", updatedArticle.getTitle());
    assertEquals("New Description", updatedArticle.getDescription());
    assertEquals("New Body", updatedArticle.getBody());

    verify(articleRepository).save(article);
  }

  @Test
  public void should_not_update_fields_with_empty_values() {
    Article article =
        new Article("Title", "Description", "Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "");

    Article updatedArticle = articleCommandService.updateArticle(article, updateParam);

    assertEquals("Title", updatedArticle.getTitle());
    assertEquals("Description", updatedArticle.getDescription());
    assertEquals("Body", updatedArticle.getBody());

    verify(articleRepository).save(article);
  }
}
