package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ProfileDatafetcher profileDatafetcher;

  private User testUser;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
    profileData =
        new ProfileData(testUser.getId(), testUser.getUsername(), "bio", "image", false);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getUserProfile_success() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(testUser);
    when(profileQueryService.findByUsername(eq(testUser.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals(testUser.getUsername(), result.getUsername());
    assertEquals("bio", result.getBio());
    assertEquals("image", result.getImage());
  }

  @Test
  void getAuthor_success() {
    Article article = Article.newBuilder().slug("test-slug").build();
    ArticleData articleData =
        new ArticleData(
            "id",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            DateTime.now(),
            DateTime.now(),
            null,
            profileData);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq(testUser.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals(testUser.getUsername(), result.getUsername());
  }

  @Test
  void getCommentAuthor_success() {
    Comment comment = Comment.newBuilder().id("comment-id").build();
    CommentData commentData =
        new CommentData("comment-id", "body", "article-id", DateTime.now(), DateTime.now(), profileData);

    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq(testUser.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals(testUser.getUsername(), result.getUsername());
  }

  @Test
  void queryProfile_success() {
    String username = "testuser";

    when(dataFetchingEnvironment.getArgument("username")).thenReturn(username);
    when(profileQueryService.findByUsername(eq(username), any()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result =
        profileDatafetcher.queryProfile(username, dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals(username, result.getProfile().getUsername());
  }

  @Test
  void queryProfile_withAuthenticatedUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));
    String username = "testuser";

    when(dataFetchingEnvironment.getArgument("username")).thenReturn(username);
    when(profileQueryService.findByUsername(eq(username), eq(testUser)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result =
        profileDatafetcher.queryProfile(username, dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getProfile());
  }
}
