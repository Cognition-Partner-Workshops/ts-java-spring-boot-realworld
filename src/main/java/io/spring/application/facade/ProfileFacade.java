package io.spring.application.facade;

import io.spring.application.data.ProfileData;
import io.spring.core.user.User;

/**
 * Facade interface for profile-related operations. This provides a unified interface for both REST
 * and GraphQL APIs to handle profile viewing and follow/unfollow operations.
 */
public interface ProfileFacade {

  /**
   * Get a user's profile by username.
   *
   * @param username the username to look up
   * @param currentUser the current user (can be null for anonymous access)
   * @return the profile data
   */
  ProfileData getProfile(String username, User currentUser);

  /**
   * Follow a user.
   *
   * @param username the username to follow
   * @param currentUser the authenticated user
   * @return the updated profile data
   */
  ProfileData followUser(String username, User currentUser);

  /**
   * Unfollow a user.
   *
   * @param username the username to unfollow
   * @param currentUser the authenticated user
   * @return the updated profile data
   */
  ProfileData unfollowUser(String username, User currentUser);
}
