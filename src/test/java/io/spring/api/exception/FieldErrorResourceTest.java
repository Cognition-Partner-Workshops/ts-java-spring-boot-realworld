package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FieldErrorResourceTest {

  @Test
  public void should_create_field_error_resource_with_all_fields() {
    FieldErrorResource fieldError = new FieldErrorResource("User", "email", "INVALID", "Invalid email format");

    assertEquals("User", fieldError.getResource());
    assertEquals("email", fieldError.getField());
    assertEquals("INVALID", fieldError.getCode());
    assertEquals("Invalid email format", fieldError.getMessage());
  }

  @Test
  public void should_create_field_error_resource_with_different_values() {
    FieldErrorResource fieldError = new FieldErrorResource("Article", "title", "REQUIRED", "Title is required");

    assertEquals("Article", fieldError.getResource());
    assertEquals("title", fieldError.getField());
    assertEquals("REQUIRED", fieldError.getCode());
    assertEquals("Title is required", fieldError.getMessage());
  }

  @Test
  public void should_create_field_error_resource_with_null_values() {
    FieldErrorResource fieldError = new FieldErrorResource(null, null, null, null);

    assertNull(fieldError.getResource());
    assertNull(fieldError.getField());
    assertNull(fieldError.getCode());
    assertNull(fieldError.getMessage());
  }
}
