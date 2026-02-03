package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileQueryServiceTest {

  @Mock
  private UserReadService userReadService;

  @Mock
  private UserRelationshipQueryService userRelationshipQueryService;

  private ProfileQueryService profileQueryService;

  @BeforeEach
  public void setUp() {
    profileQueryService = new ProfileQueryService(userReadService, userRelationshipQueryService);
  }

  @Test
  public void should_find_profile_by_username() {
    String username = "testuser";
    UserData userData = new UserData("user-123", "test@test.com", username, "bio", "image");
    when(userReadService.findByUsername(username)).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername(username, null);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getUsername(), is(username));
    assertThat(result.get().getBio(), is("bio"));
    assertThat(result.get().getImage(), is("image"));
    assertThat(result.get().isFollowing(), is(false));
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    String username = "nonexistent";
    when(userReadService.findByUsername(username)).thenReturn(null);

    Optional<ProfileData> result = profileQueryService.findByUsername(username, null);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_return_following_status_when_current_user_follows() {
    String username = "targetuser";
    User currentUser = new User("current@test.com", "currentuser", "password", "bio", "image");
    UserData userData = new UserData("target-123", "target@test.com", username, "bio", "image");
    
    when(userReadService.findByUsername(username)).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(currentUser.getId(), "target-123")).thenReturn(true);

    Optional<ProfileData> result = profileQueryService.findByUsername(username, currentUser);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().isFollowing(), is(true));
  }

  @Test
  public void should_return_not_following_when_current_user_does_not_follow() {
    String username = "targetuser";
    User currentUser = new User("current@test.com", "currentuser", "password", "bio", "image");
    UserData userData = new UserData("target-123", "target@test.com", username, "bio", "image");
    
    when(userReadService.findByUsername(username)).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(currentUser.getId(), "target-123")).thenReturn(false);

    Optional<ProfileData> result = profileQueryService.findByUsername(username, currentUser);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().isFollowing(), is(false));
  }
}
