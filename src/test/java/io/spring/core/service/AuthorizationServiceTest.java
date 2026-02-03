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
    articleAuthor = new User("author@example.com", "articleauthor", "password", "bio", "image");
    commentAuthor = new User("commenter@example.com", "commentauthor", "password", "bio", "image");
    otherUser = new User("other@example.com", "otheruser", "password", "bio", "image");

    article =
        new Article("Test Article", "Description", "Body", Arrays.asList("java"), articleAuthor.getId());
    comment = new Comment("Test comment", commentAuthor.getId(), article.getId());
  }

  @Test
  public void should_allow_article_author_to_write_article() {
    assertTrue(AuthorizationService.canWriteArticle(articleAuthor, article));
  }

  @Test
  public void should_not_allow_other_user_to_write_article() {
    assertFalse(AuthorizationService.canWriteArticle(otherUser, article));
  }

  @Test
  public void should_not_allow_comment_author_to_write_article() {
    assertFalse(AuthorizationService.canWriteArticle(commentAuthor, article));
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
    Comment authorComment = new Comment("Author's comment", articleAuthor.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, authorComment));
  }

  @Test
  public void should_handle_different_articles_correctly() {
    Article anotherArticle =
        new Article("Another Article", "Description", "Body", Arrays.asList("java"), otherUser.getId());

    assertTrue(AuthorizationService.canWriteArticle(otherUser, anotherArticle));
    assertFalse(AuthorizationService.canWriteArticle(articleAuthor, anotherArticle));
  }

  @Test
  public void should_handle_comment_on_different_article() {
    Article anotherArticle =
        new Article("Another Article", "Description", "Body", Arrays.asList("java"), otherUser.getId());
    Comment commentOnAnotherArticle =
        new Comment("Comment on another article", commentAuthor.getId(), anotherArticle.getId());

    assertTrue(AuthorizationService.canWriteComment(otherUser, anotherArticle, commentOnAnotherArticle));
    assertTrue(AuthorizationService.canWriteComment(commentAuthor, anotherArticle, commentOnAnotherArticle));
    assertFalse(AuthorizationService.canWriteComment(articleAuthor, anotherArticle, commentOnAnotherArticle));
  }
}
