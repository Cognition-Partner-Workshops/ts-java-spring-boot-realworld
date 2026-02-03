package io.spring.application.facade;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Unified API Facade for User operations. This facade provides a single entry point for both REST
 * and GraphQL APIs, ensuring consistent business logic and reducing code duplication.
 */
@Service
@AllArgsConstructor
public class UserApiFacade {

  private final UserRepository userRepository;
  private final UserQueryService userQueryService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserService userService;

  /**
   * Registers a new user and returns user data with authentication token.
   *
   * @param email user's email
   * @param username user's username
   * @param password user's password
   * @return UserWithToken containing user data and JWT token
   */
  public UserWithToken registerUser(String email, String username, String password) {
    RegisterParam registerParam = new RegisterParam(email, username, password);
    User user = userService.createUser(registerParam);
    UserData userData = userQueryService.findById(user.getId()).get();
    return new UserWithToken(userData, jwtService.toToken(user));
  }

  /**
   * Authenticates a user with email and password.
   *
   * @param email user's email
   * @param password user's password
   * @return UserWithToken containing user data and JWT token
   * @throws InvalidAuthenticationException if credentials are invalid
   */
  public UserWithToken login(String email, String password) {
    Optional<User> optional = userRepository.findByEmail(email);
    if (optional.isPresent()
        && passwordEncoder.matches(password, optional.get().getPassword())) {
      User user = optional.get();
      UserData userData = userQueryService.findById(user.getId()).get();
      return new UserWithToken(userData, jwtService.toToken(user));
    } else {
      throw new InvalidAuthenticationException();
    }
  }

  /**
   * Gets the current user's data with the provided token.
   *
   * @param currentUser the authenticated user
   * @param token the current JWT token
   * @return UserWithToken containing user data and the provided token
   */
  public UserWithToken getCurrentUser(User currentUser, String token) {
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return new UserWithToken(userData, token);
  }

  /**
   * Updates the current user's profile.
   *
   * @param currentUser the authenticated user
   * @param email new email (optional)
   * @param username new username (optional)
   * @param password new password (optional)
   * @param bio new bio (optional)
   * @param image new image URL (optional)
   * @param token the current JWT token
   * @return UserWithToken containing updated user data and the provided token
   */
  public UserWithToken updateUser(
      User currentUser,
      String email,
      String username,
      String password,
      String bio,
      String image,
      String token) {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email(email)
            .username(username)
            .password(password)
            .bio(bio)
            .image(image)
            .build();
    userService.updateUser(new UpdateUserCommand(currentUser, param));
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return new UserWithToken(userData, token);
  }

  /**
   * Gets the User entity for a given user (used for GraphQL local context).
   *
   * @param user the user
   * @return the User entity
   */
  public User getUserEntity(User user) {
    return user;
  }
}
