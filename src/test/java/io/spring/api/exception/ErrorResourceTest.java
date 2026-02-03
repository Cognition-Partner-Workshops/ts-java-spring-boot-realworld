package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ErrorResourceTest {

  @Test
  public void should_create_error_resource_with_field_errors() {
    FieldErrorResource fieldError = new FieldErrorResource("User", "email", "INVALID", "Invalid email");
    List<FieldErrorResource> fieldErrors = Arrays.asList(fieldError);

    ErrorResource errorResource = new ErrorResource(fieldErrors);

    assertNotNull(errorResource.getFieldErrors());
    assertEquals(1, errorResource.getFieldErrors().size());
    assertEquals("email", errorResource.getFieldErrors().get(0).getField());
  }

  @Test
  public void should_create_error_resource_with_empty_list() {
    ErrorResource errorResource = new ErrorResource(Collections.emptyList());

    assertNotNull(errorResource.getFieldErrors());
    assertTrue(errorResource.getFieldErrors().isEmpty());
  }

  @Test
  public void should_create_error_resource_with_multiple_errors() {
    FieldErrorResource error1 = new FieldErrorResource("User", "email", "INVALID", "Invalid email");
    FieldErrorResource error2 = new FieldErrorResource("User", "username", "REQUIRED", "Username is required");
    List<FieldErrorResource> fieldErrors = Arrays.asList(error1, error2);

    ErrorResource errorResource = new ErrorResource(fieldErrors);

    assertEquals(2, errorResource.getFieldErrors().size());
  }
}
