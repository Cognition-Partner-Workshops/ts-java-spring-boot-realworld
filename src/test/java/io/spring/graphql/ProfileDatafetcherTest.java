package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ProfileDatafetcherTest {

  private ProfileQueryService profileQueryService;
  private ProfileDatafetcher profileDatafetcher;
  private User user;
  private DataFetchingEnvironment dataFetchingEnvironment;

  @BeforeEach
  public void setUp() {
    profileQueryService = mock(ProfileQueryService.class);
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    dataFetchingEnvironment = mock(DataFetchingEnvironment.class);
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_get_user_profile() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), 
        user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq("testuser"), any())).thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getUsername(), is("testuser"));
    assertThat(result.getBio(), is("bio"));
    assertThat(result.getImage(), is("image"));
  }

  @Test
  public void should_get_article_author() {
    ProfileData authorProfile = new ProfileData("author-id", "authoruser", "author bio", "author image", false);
    ArticleData articleData = new ArticleData("article-id", "test-slug", "Test Title", 
        "Description", "Body", false, 0, DateTime.now(), DateTime.now(), 
        java.util.Arrays.asList("tag1"), authorProfile);
    
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    
    Article article = Article.newBuilder().slug("test-slug").build();
    
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq("authoruser"), any())).thenReturn(Optional.of(authorProfile));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getUsername(), is("authoruser"));
  }

  @Test
  public void should_get_comment_author() {
    ProfileData authorProfile = new ProfileData("author-id", "commentauthor", "author bio", "author image", false);
    CommentData commentData = new CommentData("comment-id", "Comment body", "article-id", 
        DateTime.now(), DateTime.now(), authorProfile);
    
    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);
    
    Comment comment = Comment.newBuilder().id("comment-id").build();
    
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq("commentauthor"), any())).thenReturn(Optional.of(authorProfile));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getUsername(), is("commentauthor"));
  }

  @Test
  public void should_query_profile_by_username() {
    ProfileData profileData = new ProfileData(user.getId(), "querieduser", "bio", "image", true);
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("querieduser");
    when(profileQueryService.findByUsername(eq("querieduser"), any())).thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("querieduser", dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getProfile(), is(notNullValue()));
    assertThat(result.getProfile().getUsername(), is("querieduser"));
    assertThat(result.getProfile().getFollowing(), is(true));
  }

  @Test
  public void should_throw_exception_when_profile_not_found() {
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("nonexistent");
    when(profileQueryService.findByUsername(eq("nonexistent"), any())).thenReturn(Optional.empty());

    try {
      profileDatafetcher.queryProfile("nonexistent", dataFetchingEnvironment);
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_query_profile_with_authenticated_user() {
    setAuthenticatedUser(user);
    ProfileData profileData = new ProfileData("other-id", "otheruser", "other bio", "other image", true);
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("otheruser");
    when(profileQueryService.findByUsername("otheruser", user)).thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("otheruser", dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getProfile().getFollowing(), is(true));
  }
}
