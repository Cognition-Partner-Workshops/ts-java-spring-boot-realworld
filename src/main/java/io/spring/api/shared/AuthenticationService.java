package io.spring.api.shared;

import io.spring.core.user.User;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Unified authentication service that provides a consistent way to retrieve the current
 * authenticated user across both REST and GraphQL APIs. This service abstracts the Spring Security
 * context access, ensuring both API layers use the same authentication logic.
 */
@Service
public class AuthenticationService {

  /**
   * Retrieves the currently authenticated user from the Spring Security context.
   *
   * @return Optional containing the authenticated User, or empty if not authenticated
   */
  public Optional<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || authentication instanceof AnonymousAuthenticationToken
        || authentication.getPrincipal() == null) {
      return Optional.empty();
    }
    return Optional.of((User) authentication.getPrincipal());
  }

  /**
   * Checks if there is a currently authenticated user.
   *
   * @return true if a user is authenticated, false otherwise
   */
  public boolean isAuthenticated() {
    return getCurrentUser().isPresent();
  }

  /**
   * Gets the current user or throws an exception if not authenticated. This is useful for endpoints
   * that require authentication.
   *
   * @return the authenticated User
   * @throws AuthenticationRequiredException if no user is authenticated
   */
  public User requireCurrentUser() {
    return getCurrentUser().orElseThrow(AuthenticationRequiredException::new);
  }
}
