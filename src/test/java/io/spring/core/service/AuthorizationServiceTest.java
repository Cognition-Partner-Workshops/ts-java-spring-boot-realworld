package io.spring.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void should_allow_article_author_to_write_article() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    
    assertThat(AuthorizationService.canWriteArticle(user, article), is(true));
  }

  @Test
  public void should_not_allow_non_author_to_write_article() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    User otherUser = new User("other@example.com", "otheruser", "password", "bio", "image");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), otherUser.getId());
    
    assertThat(AuthorizationService.canWriteArticle(user, article), is(false));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User articleAuthor = new User("author@example.com", "author", "password", "bio", "image");
    User commenter = new User("commenter@example.com", "commenter", "password", "bio", "image");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());
    
    assertThat(AuthorizationService.canWriteComment(articleAuthor, article, comment), is(true));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User articleAuthor = new User("author@example.com", "author", "password", "bio", "image");
    User commenter = new User("commenter@example.com", "commenter", "password", "bio", "image");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());
    
    assertThat(AuthorizationService.canWriteComment(commenter, article, comment), is(true));
  }

  @Test
  public void should_not_allow_non_author_to_write_comment() {
    User articleAuthor = new User("author@example.com", "author", "password", "bio", "image");
    User commenter = new User("commenter@example.com", "commenter", "password", "bio", "image");
    User otherUser = new User("other@example.com", "other", "password", "bio", "image");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commenter.getId(), article.getId());
    
    assertThat(AuthorizationService.canWriteComment(otherUser, article, comment), is(false));
  }
}
