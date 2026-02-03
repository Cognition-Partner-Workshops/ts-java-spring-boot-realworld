package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthenticationExceptionTest {

  @Test
  void constructor_createsException() {
    AuthenticationException exception = new AuthenticationException();
    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }

  @Test
  void canBeThrown() {
    assertThrows(AuthenticationException.class, () -> {
      throw new AuthenticationException();
    });
  }
}
