package io.spring.graphql;

import io.spring.api.shared.AuthenticationService;
import io.spring.core.user.User;
import java.util.Optional;

/**
 * Utility class for GraphQL authentication. This class delegates to the shared
 * AuthenticationService to ensure consistent authentication handling across REST and GraphQL APIs.
 *
 * @deprecated Use {@link AuthenticationService} directly instead. This class is maintained for
 *     backward compatibility with existing GraphQL resolvers.
 */
@Deprecated
public class SecurityUtil {

  private static final AuthenticationService authService = new AuthenticationService();

  /**
   * Retrieves the currently authenticated user.
   *
   * @return Optional containing the authenticated User, or empty if not authenticated
   * @deprecated Use {@link AuthenticationService#getCurrentUser()} instead
   */
  @Deprecated
  public static Optional<User> getCurrentUser() {
    return authService.getCurrentUser();
  }
}
