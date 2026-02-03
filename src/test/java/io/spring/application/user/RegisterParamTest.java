package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegisterParamTest {

  @Test
  void allArgsConstructor_createsWithAllFields() {
    RegisterParam param = new RegisterParam("test@example.com", "testuser", "password123");

    assertEquals("test@example.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("password123", param.getPassword());
  }

  @Test
  void noArgsConstructor_createsEmptyObject() {
    RegisterParam param = new RegisterParam();

    assertNull(param.getEmail());
    assertNull(param.getUsername());
    assertNull(param.getPassword());
  }
}
