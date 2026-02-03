package io.spring.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void should_instantiate_authorization_service() {
    AuthorizationService service = new AuthorizationService();
    assertThat(service, is(notNullValue()));
  }

  @Test
  public void should_allow_article_author_to_write_article() {
    User author = new User("author@test.com", "author", "password", "bio", "image");
    Article article = new Article("title", "description", "body", Arrays.asList("tag"), author.getId());

    boolean canWrite = AuthorizationService.canWriteArticle(author, article);

    assertThat(canWrite, is(true));
  }

  @Test
  public void should_not_allow_non_author_to_write_article() {
    User author = new User("author@test.com", "author", "password", "bio", "image");
    User otherUser = new User("other@test.com", "other", "password", "bio", "image");
    Article article = new Article("title", "description", "body", Arrays.asList("tag"), author.getId());

    boolean canWrite = AuthorizationService.canWriteArticle(otherUser, article);

    assertThat(canWrite, is(false));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User author = new User("author@test.com", "author", "password", "bio", "image");
    User commenter = new User("commenter@test.com", "commenter", "password", "bio", "image");
    Article article = new Article("title", "description", "body", Arrays.asList("tag"), author.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());

    boolean canWrite = AuthorizationService.canWriteComment(author, article, comment);

    assertThat(canWrite, is(true));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User author = new User("author@test.com", "author", "password", "bio", "image");
    User commenter = new User("commenter@test.com", "commenter", "password", "bio", "image");
    Article article = new Article("title", "description", "body", Arrays.asList("tag"), author.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());

    boolean canWrite = AuthorizationService.canWriteComment(commenter, article, comment);

    assertThat(canWrite, is(true));
  }

  @Test
  public void should_not_allow_non_author_non_commenter_to_write_comment() {
    User author = new User("author@test.com", "author", "password", "bio", "image");
    User commenter = new User("commenter@test.com", "commenter", "password", "bio", "image");
    User otherUser = new User("other@test.com", "other", "password", "bio", "image");
    Article article = new Article("title", "description", "body", Arrays.asList("tag"), author.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());

    boolean canWrite = AuthorizationService.canWriteComment(otherUser, article, comment);

    assertThat(canWrite, is(false));
  }
}
