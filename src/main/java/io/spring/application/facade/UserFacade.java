package io.spring.application.facade;

import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserParam;
import io.spring.core.user.User;

/**
 * Facade interface for user-related operations. This provides a unified interface for both REST and
 * GraphQL APIs to handle user registration, authentication, and profile management.
 */
public interface UserFacade {

  /**
   * Register a new user.
   *
   * @param registerParam the registration parameters
   * @return the created user with authentication token
   */
  UserWithToken registerUser(RegisterParam registerParam);

  /**
   * Authenticate a user with email and password.
   *
   * @param email the user's email
   * @param password the user's password
   * @return the authenticated user with token
   */
  UserWithToken login(String email, String password);

  /**
   * Get the current authenticated user's data.
   *
   * @param currentUser the authenticated user
   * @param token the current authentication token
   * @return the user data with token
   */
  UserWithToken getCurrentUser(User currentUser, String token);

  /**
   * Update the current user's profile.
   *
   * @param currentUser the authenticated user
   * @param updateUserParam the update parameters
   * @param token the current authentication token
   * @return the updated user data with token
   */
  UserWithToken updateUser(User currentUser, UpdateUserParam updateUserParam, String token);

  /**
   * Find user data by user ID.
   *
   * @param userId the user ID
   * @return the user data if found
   */
  UserData findById(String userId);
}
