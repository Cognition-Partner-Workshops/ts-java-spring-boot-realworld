package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  void constructor_and_getters() {
    UserData userData = new UserData("user-id", "test@example.com", "testuser", "bio", "image");
    UserWithToken userWithToken = new UserWithToken(userData, "token123");

    assertEquals("test@example.com", userWithToken.getEmail());
    assertEquals("testuser", userWithToken.getUsername());
    assertEquals("bio", userWithToken.getBio());
    assertEquals("image", userWithToken.getImage());
    assertEquals("token123", userWithToken.getToken());
  }

  @Test
  void getters_fromUserData() {
    UserData userData = new UserData("user-id", "email@test.com", "username", "user bio", "user image");
    UserWithToken userWithToken = new UserWithToken(userData, "mytoken");

    assertEquals(userData.getEmail(), userWithToken.getEmail());
    assertEquals(userData.getUsername(), userWithToken.getUsername());
    assertEquals(userData.getBio(), userWithToken.getBio());
    assertEquals(userData.getImage(), userWithToken.getImage());
  }
}
