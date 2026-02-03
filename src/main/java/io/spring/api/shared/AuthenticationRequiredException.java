package io.spring.api.shared;

/**
 * Exception thrown when an operation requires authentication but no user is currently
 * authenticated. This is a unified exception that can be handled consistently across both REST and
 * GraphQL APIs.
 */
public class AuthenticationRequiredException extends RuntimeException {

  public AuthenticationRequiredException() {
    super("Authentication required");
  }

  public AuthenticationRequiredException(String message) {
    super(message);
  }
}
