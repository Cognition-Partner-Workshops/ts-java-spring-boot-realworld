package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;

  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;

  private User testUser;
  private User targetUser;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
    targetUser = new User("target@example.com", "targetuser", "password", "bio", "image");
    profileData =
        new ProfileData(
            targetUser.getId(), targetUser.getUsername(), "bio", "image", true);
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));
  }

  @Test
  void follow_success() {
    authenticateUser();
    String username = "targetuser";

    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(targetUser));
    when(profileQueryService.findByUsername(eq(username), eq(testUser)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow(username);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals(username, result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void follow_notAuthenticated() {
    String username = "targetuser";

    assertThrows(AuthenticationException.class, () -> relationMutation.follow(username));
  }

  @Test
  void follow_userNotFound() {
    authenticateUser();
    String username = "nonexistent";

    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow(username));
  }

  @Test
  void unfollow_success() {
    authenticateUser();
    String username = "targetuser";
    FollowRelation relation = new FollowRelation(testUser.getId(), targetUser.getId());

    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(testUser.getId()), eq(targetUser.getId())))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername(eq(username), eq(testUser)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow(username);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    verify(userRepository).removeRelation(eq(relation));
  }

  @Test
  void unfollow_notAuthenticated() {
    String username = "targetuser";

    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow(username));
  }

  @Test
  void unfollow_userNotFound() {
    authenticateUser();
    String username = "nonexistent";

    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow(username));
  }

  @Test
  void unfollow_relationNotFound() {
    authenticateUser();
    String username = "targetuser";

    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(testUser.getId()), eq(targetUser.getId())))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow(username));
  }
}
