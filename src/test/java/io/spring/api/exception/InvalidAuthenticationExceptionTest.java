package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class InvalidAuthenticationExceptionTest {

  @Test
  void constructor_setsDefaultMessage() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    assertEquals("invalid email or password", exception.getMessage());
  }

  @Test
  void isRuntimeException() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    assertTrue(exception instanceof RuntimeException);
  }
}
