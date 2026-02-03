package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class NoAuthorizationExceptionTest {

  @Test
  void constructor_createsException() {
    NoAuthorizationException exception = new NoAuthorizationException();

    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }
}
