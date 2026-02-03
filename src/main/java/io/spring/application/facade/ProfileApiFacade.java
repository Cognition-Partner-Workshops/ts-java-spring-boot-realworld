package io.spring.application.facade;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Unified API Facade for Profile operations. This facade provides a single entry point for both
 * REST and GraphQL APIs, ensuring consistent business logic and reducing code duplication.
 */
@Service
@AllArgsConstructor
public class ProfileApiFacade {

  private final ProfileQueryService profileQueryService;
  private final UserRepository userRepository;

  /**
   * Gets a user's profile by username.
   *
   * @param username the username to look up
   * @param currentUser the current user (optional, for following status)
   * @return ProfileData
   * @throws ResourceNotFoundException if user not found
   */
  public ProfileData getProfile(String username, User currentUser) {
    return profileQueryService
        .findByUsername(username, currentUser)
        .orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Follows a user.
   *
   * @param username the username to follow
   * @param currentUser the authenticated user
   * @return ProfileData with updated following status
   * @throws ResourceNotFoundException if user not found
   */
  public ProfileData followUser(String username, User currentUser) {
    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    FollowRelation followRelation = new FollowRelation(currentUser.getId(), target.getId());
    userRepository.saveRelation(followRelation);
    return profileQueryService.findByUsername(username, currentUser).get();
  }

  /**
   * Unfollows a user.
   *
   * @param username the username to unfollow
   * @param currentUser the authenticated user
   * @return ProfileData with updated following status
   * @throws ResourceNotFoundException if user not found or not following
   */
  public ProfileData unfollowUser(String username, User currentUser) {
    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    FollowRelation relation =
        userRepository
            .findRelation(currentUser.getId(), target.getId())
            .orElseThrow(ResourceNotFoundException::new);
    userRepository.removeRelation(relation);
    return profileQueryService.findByUsername(username, currentUser).get();
  }
}
