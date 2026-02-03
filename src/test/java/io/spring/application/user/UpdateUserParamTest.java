package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UpdateUserParamTest {

  @Test
  void builder_createsWithAllFields() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("test@example.com")
            .username("testuser")
            .password("password123")
            .bio("Test bio")
            .image("image.jpg")
            .build();

    assertEquals("test@example.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("password123", param.getPassword());
    assertEquals("Test bio", param.getBio());
    assertEquals("image.jpg", param.getImage());
  }

  @Test
  void noArgsConstructor_createsWithEmptyStrings() {
    UpdateUserParam param = new UpdateUserParam();

    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void allArgsConstructor_createsWithAllFields() {
    UpdateUserParam param =
        new UpdateUserParam("test@example.com", "password123", "testuser", "bio", "image.jpg");

    assertEquals("test@example.com", param.getEmail());
    assertEquals("password123", param.getPassword());
    assertEquals("testuser", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("image.jpg", param.getImage());
  }

  @Test
  void builder_withDefaultValues() {
    UpdateUserParam param = UpdateUserParam.builder().build();

    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void builder_withPartialValues() {
    UpdateUserParam param =
        UpdateUserParam.builder().email("test@example.com").username("testuser").build();

    assertEquals("test@example.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }
}
