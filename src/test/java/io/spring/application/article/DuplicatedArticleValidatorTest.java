package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;

  @Mock private ConstraintValidatorContext context;

  private DuplicatedArticleValidator validator;

  @BeforeEach
  public void setUp() {
    validator = new DuplicatedArticleValidator();
    try {
      java.lang.reflect.Field field =
          DuplicatedArticleValidator.class.getDeclaredField("articleQueryService");
      field.setAccessible(true);
      field.set(validator, articleQueryService);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void should_return_true_when_article_with_slug_does_not_exist() {
    String title = "New Article Title";
    when(articleQueryService.findBySlug("new-article-title", null)).thenReturn(Optional.empty());

    boolean result = validator.isValid(title, context);

    assertTrue(result);
    verify(articleQueryService).findBySlug("new-article-title", null);
  }

  @Test
  public void should_return_false_when_article_with_slug_already_exists() {
    String title = "Existing Article";
    ArticleData existingArticle = mock(ArticleData.class);
    when(articleQueryService.findBySlug("existing-article", null))
        .thenReturn(Optional.of(existingArticle));

    boolean result = validator.isValid(title, context);

    assertFalse(result);
    verify(articleQueryService).findBySlug("existing-article", null);
  }

  @Test
  public void should_convert_title_to_slug_before_checking() {
    String title = "Title With   Multiple   Spaces";
    when(articleQueryService.findBySlug("title-with-multiple-spaces", null))
        .thenReturn(Optional.empty());

    boolean result = validator.isValid(title, context);

    assertTrue(result);
    verify(articleQueryService).findBySlug("title-with-multiple-spaces", null);
  }

  @Test
  public void should_handle_title_with_special_characters() {
    String title = "what?the.hell,w";
    when(articleQueryService.findBySlug("what-the-hell-w", null)).thenReturn(Optional.empty());

    boolean result = validator.isValid(title, context);

    assertTrue(result);
    verify(articleQueryService).findBySlug("what-the-hell-w", null);
  }

  @Test
  public void should_handle_uppercase_title() {
    String title = "UPPERCASE TITLE";
    when(articleQueryService.findBySlug("uppercase-title", null)).thenReturn(Optional.empty());

    boolean result = validator.isValid(title, context);

    assertTrue(result);
    verify(articleQueryService).findBySlug("uppercase-title", null);
  }
}
