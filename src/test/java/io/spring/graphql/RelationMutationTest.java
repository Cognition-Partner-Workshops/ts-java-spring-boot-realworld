package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class RelationMutationTest {

  private UserRepository userRepository;
  private ProfileQueryService profileQueryService;
  private RelationMutation relationMutation;
  private User user;
  private User targetUser;

  @BeforeEach
  public void setUp() {
    userRepository = mock(UserRepository.class);
    profileQueryService = mock(ProfileQueryService.class);
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    targetUser = new User("target@test.com", "targetuser", "password", "target bio", "target image");
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_follow_user_successfully() {
    setAuthenticatedUser(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    ProfileData profileData = new ProfileData(targetUser.getId(), targetUser.getUsername(), 
        targetUser.getBio(), targetUser.getImage(), true);
    when(profileQueryService.findByUsername("targetuser", user)).thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertThat(result, is(notNullValue()));
    assertThat(result.getProfile(), is(notNullValue()));
    assertThat(result.getProfile().getUsername(), is("targetuser"));
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  public void should_throw_exception_when_follow_without_auth() {
    try {
      relationMutation.follow("targetuser");
      assertThat("Should have thrown exception", false);
    } catch (AuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_follow_nonexistent_user() {
    setAuthenticatedUser(user);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    try {
      relationMutation.follow("nonexistent");
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_unfollow_user_successfully() {
    setAuthenticatedUser(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    FollowRelation relation = new FollowRelation(user.getId(), targetUser.getId());
    when(userRepository.findRelation(user.getId(), targetUser.getId()))
        .thenReturn(Optional.of(relation));
    ProfileData profileData = new ProfileData(targetUser.getId(), targetUser.getUsername(), 
        targetUser.getBio(), targetUser.getImage(), false);
    when(profileQueryService.findByUsername("targetuser", user)).thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertThat(result, is(notNullValue()));
    assertThat(result.getProfile(), is(notNullValue()));
    assertThat(result.getProfile().getUsername(), is("targetuser"));
    verify(userRepository).removeRelation(relation);
  }

  @Test
  public void should_throw_exception_when_unfollow_without_auth() {
    try {
      relationMutation.unfollow("targetuser");
      assertThat("Should have thrown exception", false);
    } catch (AuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_unfollow_nonexistent_user() {
    setAuthenticatedUser(user);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    try {
      relationMutation.unfollow("nonexistent");
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_unfollow_user_not_followed() {
    setAuthenticatedUser(user);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(user.getId(), targetUser.getId()))
        .thenReturn(Optional.empty());

    try {
      relationMutation.unfollow("targetuser");
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }
}
