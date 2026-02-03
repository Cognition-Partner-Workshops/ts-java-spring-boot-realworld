package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock
  private CommentReadService commentReadService;

  @Mock
  private UserRelationshipQueryService userRelationshipQueryService;

  private CommentQueryService commentQueryService;

  @BeforeEach
  public void setUp() {
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
  }

  private CommentData createCommentData(String id, String authorId) {
    ProfileData author = new ProfileData(authorId, "author", "bio", "image", false);
    return new CommentData(id, "comment body", "article-123", DateTime.now(), DateTime.now(), author);
  }

  @Test
  public void should_find_comment_by_id() {
    String commentId = "comment-123";
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    CommentData commentData = createCommentData(commentId, "author-123");
    
    when(commentReadService.findById(commentId)).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-123")).thenReturn(false);

    Optional<CommentData> result = commentQueryService.findById(commentId, user);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getId(), is(commentId));
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    String commentId = "nonexistent";
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    
    when(commentReadService.findById(commentId)).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById(commentId, user);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_find_comments_by_article_id() {
    String articleId = "article-123";
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    CommentData comment1 = createCommentData("comment-1", "author-1");
    CommentData comment2 = createCommentData("comment-2", "author-2");
    
    when(commentReadService.findByArticleId(articleId)).thenReturn(Arrays.asList(comment1, comment2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("author-1")));

    List<CommentData> result = commentQueryService.findByArticleId(articleId, user);

    assertThat(result.size(), is(2));
    assertThat(result.get(0).getProfileData().isFollowing(), is(true));
    assertThat(result.get(1).getProfileData().isFollowing(), is(false));
  }

  @Test
  public void should_return_empty_list_when_no_comments() {
    String articleId = "article-123";
    
    when(commentReadService.findByArticleId(articleId)).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId(articleId, null);

    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void should_find_comments_without_user() {
    String articleId = "article-123";
    CommentData comment = createCommentData("comment-1", "author-1");
    
    when(commentReadService.findByArticleId(articleId)).thenReturn(Arrays.asList(comment));

    List<CommentData> result = commentQueryService.findByArticleId(articleId, null);

    assertThat(result.size(), is(1));
    assertThat(result.get(0).getProfileData().isFollowing(), is(false));
  }
}
