package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RelationMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;

  private RelationMutation relationMutation;

  @BeforeEach
  public void setUp() {
    relationMutation = new RelationMutation(userRepository, profileQueryService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User user) {
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  @Test
  public void should_follow_user_successfully() {
    User currentUser = new User("current@test.com", "currentuser", "password", "", "");
    User targetUser = new User("target@test.com", "targetuser", "password", "", "");
    authenticateUser(currentUser);

    ProfileData profileData = new ProfileData(targetUser.getId(), "targetuser", "bio", "image", true);

    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(profileQueryService.findByUsername("targetuser", currentUser)).thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  public void should_follow_another_user() {
    User currentUser = new User("current@test.com", "currentuser", "password", "", "");
    User targetUser = new User("target@test.com", "anotheruser", "password", "", "");
    authenticateUser(currentUser);

    ProfileData profileData = new ProfileData(targetUser.getId(), "anotheruser", "bio", "image", true);

    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(targetUser));
    when(profileQueryService.findByUsername("anotheruser", currentUser)).thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.follow("anotheruser");

    assertNotNull(result);
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  public void should_throw_resource_not_found_when_target_user_not_found_for_follow() {
    User currentUser = new User("current@test.com", "currentuser", "password", "", "");
    authenticateUser(currentUser);

    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.follow("nonexistent"));
  }

  @Test
  public void should_unfollow_user_successfully() {
    User currentUser = new User("current@test.com", "currentuser", "password", "", "");
    User targetUser = new User("target@test.com", "targetuser", "password", "", "");
    FollowRelation relation = new FollowRelation(currentUser.getId(), targetUser.getId());
    authenticateUser(currentUser);

    ProfileData profileData = new ProfileData(targetUser.getId(), "targetuser", "bio", "image", false);

    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId())).thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername("targetuser", currentUser)).thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("targetuser", result.getProfile().getUsername());
    verify(userRepository).removeRelation(relation);
  }

  @Test
  public void should_unfollow_another_user() {
    User currentUser = new User("current@test.com", "currentuser", "password", "", "");
    User targetUser = new User("target@test.com", "anotheruser", "password", "", "");
    FollowRelation relation = new FollowRelation(currentUser.getId(), targetUser.getId());
    authenticateUser(currentUser);

    ProfileData profileData = new ProfileData(targetUser.getId(), "anotheruser", "bio", "image", false);

    when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId())).thenReturn(Optional.of(relation));
    when(profileQueryService.findByUsername("anotheruser", currentUser)).thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("anotheruser");

    assertNotNull(result);
    verify(userRepository).removeRelation(relation);
  }

  @Test
  public void should_throw_resource_not_found_when_target_user_not_found_for_unfollow() {
    User currentUser = new User("current@test.com", "currentuser", "password", "", "");
    authenticateUser(currentUser);

    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("nonexistent"));
  }

  @Test
  public void should_throw_resource_not_found_when_relation_not_found_for_unfollow() {
    User currentUser = new User("current@test.com", "currentuser", "password", "", "");
    User targetUser = new User("target@test.com", "targetuser", "password", "", "");
    authenticateUser(currentUser);

    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> relationMutation.unfollow("targetuser"));
  }
}
