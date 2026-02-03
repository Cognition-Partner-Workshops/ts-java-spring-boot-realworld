package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User articleAuthor;
  private User commentAuthor;
  private User otherUser;
  private Article article;
  private Comment comment;

  @BeforeEach
  public void setUp() {
    articleAuthor = new User("author@email.com", "author", "password", "bio", "image");
    commentAuthor = new User("commenter@email.com", "commenter", "password", "bio", "image");
    otherUser = new User("other@email.com", "other", "password", "bio", "image");

    article =
        new Article("Test Article", "description", "body", Arrays.asList("java"), articleAuthor.getId());
    comment = new Comment("Test comment", commentAuthor.getId(), article.getId());
  }

  @Test
  public void should_allow_article_author_to_write_article() {
    assertTrue(AuthorizationService.canWriteArticle(articleAuthor, article));
  }

  @Test
  public void should_not_allow_non_author_to_write_article() {
    assertFalse(AuthorizationService.canWriteArticle(otherUser, article));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    assertTrue(AuthorizationService.canWriteComment(commentAuthor, article, comment));
  }

  @Test
  public void should_not_allow_other_user_to_write_comment() {
    assertFalse(AuthorizationService.canWriteComment(otherUser, article, comment));
  }

  @Test
  public void should_allow_user_who_is_both_article_and_comment_author() {
    Comment authorComment = new Comment("Author comment", articleAuthor.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, authorComment));
  }
}
