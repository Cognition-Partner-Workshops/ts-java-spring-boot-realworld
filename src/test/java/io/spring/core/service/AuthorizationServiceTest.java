package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void should_allow_article_author_to_write_article() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    Article article = new Article("Title", "Description", "Body", Collections.emptyList(), user.getId());

    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  public void should_not_allow_non_author_to_write_article() {
    User author = new User("author@test.com", "author", "password", "", "");
    User otherUser = new User("other@test.com", "other", "password", "", "");
    Article article = new Article("Title", "Description", "Body", Collections.emptyList(), author.getId());

    assertFalse(AuthorizationService.canWriteArticle(otherUser, article));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "password", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "password", "", "");
    Article article = new Article("Title", "Description", "Body", Collections.emptyList(), articleAuthor.getId());
    Comment comment = new Comment("Comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "password", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "password", "", "");
    Article article = new Article("Title", "Description", "Body", Collections.emptyList(), articleAuthor.getId());
    Comment comment = new Comment("Comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(commentAuthor, article, comment));
  }

  @Test
  public void should_not_allow_other_user_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "password", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "password", "", "");
    User otherUser = new User("other@test.com", "other", "password", "", "");
    Article article = new Article("Title", "Description", "Body", Collections.emptyList(), articleAuthor.getId());
    Comment comment = new Comment("Comment body", commentAuthor.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(otherUser, article, comment));
  }
}
