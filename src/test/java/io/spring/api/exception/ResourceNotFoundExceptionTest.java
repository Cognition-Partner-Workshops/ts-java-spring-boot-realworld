package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ResourceNotFoundExceptionTest {

  @Test
  void constructor_createsException() {
    ResourceNotFoundException exception = new ResourceNotFoundException();

    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }
}
