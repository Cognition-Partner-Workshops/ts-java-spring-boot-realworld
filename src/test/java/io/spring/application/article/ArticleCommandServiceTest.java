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
  private User user;

  @BeforeEach
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @Test
  public void should_create_article_with_valid_params() {
    NewArticleParam param =
        new NewArticleParam("Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"));

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertEquals("test-title", article.getSlug());
    assertEquals("Test Description", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals(user.getId(), article.getUserId());
    assertEquals(2, article.getTags().size());

    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_without_tags() {
    NewArticleParam param =
        new NewArticleParam("No Tags Article", "Description", "Body", Collections.emptyList());

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertEquals("no-tags-article", article.getSlug());
    assertTrue(article.getTags().isEmpty());

    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_with_single_tag() {
    NewArticleParam param =
        new NewArticleParam("Single Tag", "Description", "Body", Arrays.asList("java"));

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertEquals(1, article.getTags().size());
    assertEquals("java", article.getTags().get(0).getName());
  }

  @Test
  public void should_save_article_to_repository() {
    NewArticleParam param =
        new NewArticleParam("Save Test", "Description", "Body", Arrays.asList("test"));

    articleCommandService.createArticle(param, user);

    ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository).save(articleCaptor.capture());

    Article savedArticle = articleCaptor.getValue();
    assertEquals("save-test", savedArticle.getSlug());
    assertEquals(user.getId(), savedArticle.getUserId());
  }

  @Test
  public void should_update_article_title() {
    Article existingArticle =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", null, null);

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("new-title", updatedArticle.getSlug());
    assertEquals("Old Description", updatedArticle.getDescription());
    assertEquals("Old Body", updatedArticle.getBody());

    verify(articleRepository).save(existingArticle);
  }

  @Test
  public void should_update_article_description() {
    Article existingArticle =
        new Article("Title", "Old Description", "Body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "New Description");

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("New Description", updatedArticle.getDescription());

    verify(articleRepository).save(existingArticle);
  }

  @Test
  public void should_update_article_body() {
    Article existingArticle =
        new Article("Title", "Description", "Old Body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("", "New Body", "");

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("New Body", updatedArticle.getBody());

    verify(articleRepository).save(existingArticle);
  }

  @Test
  public void should_update_multiple_article_fields() {
    Article existingArticle =
        new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam =
        new UpdateArticleParam("New Title", "New Body", "New Description");

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals("new-title", updatedArticle.getSlug());
    assertEquals("New Description", updatedArticle.getDescription());
    assertEquals("New Body", updatedArticle.getBody());

    verify(articleRepository).save(existingArticle);
  }

  @Test
  public void should_not_update_fields_with_null_values() {
    Article existingArticle =
        new Article("Original Title", "Original Description", "Original Body", Arrays.asList("java"), user.getId());
    String originalSlug = existingArticle.getSlug();
    UpdateArticleParam updateParam = new UpdateArticleParam(null, null, null);

    Article updatedArticle = articleCommandService.updateArticle(existingArticle, updateParam);

    assertEquals(originalSlug, updatedArticle.getSlug());
    assertEquals("Original Description", updatedArticle.getDescription());
    assertEquals("Original Body", updatedArticle.getBody());

    verify(articleRepository).save(existingArticle);
  }

  @Test
  public void should_generate_correct_slug_for_title_with_special_characters() {
    NewArticleParam param =
        new NewArticleParam("what?the.hell,w", "Description", "Body", Arrays.asList("test"));

    Article article = articleCommandService.createArticle(param, user);

    assertEquals("what-the-hell-w", article.getSlug());
  }

  @Test
  public void should_generate_correct_slug_for_title_with_multiple_spaces() {
    NewArticleParam param =
        new NewArticleParam("Title   With   Multiple   Spaces", "Description", "Body", Arrays.asList("test"));

    Article article = articleCommandService.createArticle(param, user);

    assertEquals("title-with-multiple-spaces", article.getSlug());
  }
}
