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
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;
  private User user;
  private User targetUser;

  @BeforeEach
  void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    targetUser = new User("target@example.com", "targetuser", "password123", "bio", "image.jpg");
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void follow_success() {
    setAuthenticatedUser();

    ProfileData profileData =
        new ProfileData(
            targetUser.getId(),
            targetUser.getUsername(),
            targetUser.getBio(),
            targetUser.getImage(),
            true);

    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(targetUser));
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void follow_notAuthenticated() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertThrows(AuthenticationException.class, () -> relationMutation.follow("targetuser"));
  }

  @Test
  void follow_userNotFound() {
    setAuthenticatedUser();

    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  void unfollow_success() {
    setAuthenticatedUser();

    FollowRelation relation = new FollowRelation(user.getId(), targetUser.getId());
    ProfileData profileData =
        new ProfileData(
            targetUser.getId(),
            targetUser.getUsername(),
            targetUser.getBio(),
            targetUser.getImage(),
            false);

    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(user.getId()), eq(targetUser.getId())))
        .thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername(eq("targetuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).removeRelation(eq(relation));
  }

  @Test
  void unfollow_notAuthenticated() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertThrows(AuthenticationException.class, () -> relationMutation.unfollow("targetuser"));
  }

  @Test
  void unfollow_userNotFound() {
    setAuthenticatedUser();

    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  void unfollow_relationNotFound() {
    setAuthenticatedUser();

    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(eq(user.getId()), eq(targetUser.getId())))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
