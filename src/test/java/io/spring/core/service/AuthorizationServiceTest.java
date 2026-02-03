package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  void canWriteArticle_authorCanWrite() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    Article article = new Article("Title", "Description", "Body", Arrays.asList("tag"), user.getId());

    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  void canWriteArticle_nonAuthorCannotWrite() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    User otherUser = new User("other@example.com", "otheruser", "password", "bio", "image");
    Article article = new Article("Title", "Description", "Body", Arrays.asList("tag"), otherUser.getId());

    assertFalse(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  void canWriteComment_articleAuthorCanWrite() {
    User articleAuthor = new User("author@example.com", "author", "password", "bio", "image");
    User commentAuthor = new User("commenter@example.com", "commenter", "password", "bio", "image");
    Article article = new Article("Title", "Description", "Body", Arrays.asList("tag"), articleAuthor.getId());
    Comment comment = new Comment("Comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  void canWriteComment_commentAuthorCanWrite() {
    User articleAuthor = new User("author@example.com", "author", "password", "bio", "image");
    User commentAuthor = new User("commenter@example.com", "commenter", "password", "bio", "image");
    Article article = new Article("Title", "Description", "Body", Arrays.asList("tag"), articleAuthor.getId());
    Comment comment = new Comment("Comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(commentAuthor, article, comment));
  }

  @Test
  void canWriteComment_otherUserCannotWrite() {
    User articleAuthor = new User("author@example.com", "author", "password", "bio", "image");
    User commentAuthor = new User("commenter@example.com", "commenter", "password", "bio", "image");
    User otherUser = new User("other@example.com", "other", "password", "bio", "image");
    Article article = new Article("Title", "Description", "Body", Arrays.asList("tag"), articleAuthor.getId());
    Comment comment = new Comment("Comment body", commentAuthor.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(otherUser, article, comment));
  }
}
