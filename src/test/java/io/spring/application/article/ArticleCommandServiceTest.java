package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

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

  @Mock
  private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  public void should_create_article() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    NewArticleParam param = new NewArticleParam("Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"));

    Article article = articleCommandService.createArticle(param, creator);

    assertThat(article, is(notNullValue()));
    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getDescription(), is("Test Description"));
    assertThat(article.getBody(), is("Test Body"));
    assertThat(article.getUserId(), is(creator.getId()));
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article = new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Description");

    Article updatedArticle = articleCommandService.updateArticle(article, param);

    assertThat(updatedArticle.getTitle(), is("New Title"));
    assertThat(updatedArticle.getDescription(), is("New Description"));
    assertThat(updatedArticle.getBody(), is("New Body"));
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_with_partial_data() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article = new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), creator.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", null, null);

    Article updatedArticle = articleCommandService.updateArticle(article, param);

    assertThat(updatedArticle.getTitle(), is("New Title"));
    assertThat(updatedArticle.getDescription(), is("Old Description"));
    assertThat(updatedArticle.getBody(), is("Old Body"));
    verify(articleRepository).save(any(Article.class));
  }
}
