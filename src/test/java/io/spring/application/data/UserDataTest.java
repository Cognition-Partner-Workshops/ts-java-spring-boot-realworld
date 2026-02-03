package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  void constructor_and_getters() {
    UserData userData = new UserData("user-id", "test@example.com", "testuser", "bio", "image");

    assertEquals("user-id", userData.getId());
    assertEquals("test@example.com", userData.getEmail());
    assertEquals("testuser", userData.getUsername());
    assertEquals("bio", userData.getBio());
    assertEquals("image", userData.getImage());
  }

  @Test
  void setters() {
    UserData userData = new UserData();

    userData.setId("new-id");
    userData.setEmail("new@example.com");
    userData.setUsername("newuser");
    userData.setBio("new bio");
    userData.setImage("new image");

    assertEquals("new-id", userData.getId());
    assertEquals("new@example.com", userData.getEmail());
    assertEquals("newuser", userData.getUsername());
    assertEquals("new bio", userData.getBio());
    assertEquals("new image", userData.getImage());
  }

  @Test
  void equals_and_hashCode() {
    UserData userData1 = new UserData("id", "email@test.com", "user", "bio", "image");
    UserData userData2 = new UserData("id", "email@test.com", "user", "bio", "image");

    assertEquals(userData1, userData2);
    assertEquals(userData1.hashCode(), userData2.hashCode());
  }

  @Test
  void notEquals_differentId() {
    UserData userData1 = new UserData("id1", "email@test.com", "user", "bio", "image");
    UserData userData2 = new UserData("id2", "email@test.com", "user", "bio", "image");

    assertNotEquals(userData1, userData2);
  }

  @Test
  void toString_notNull() {
    UserData userData = new UserData("id", "email@test.com", "user", "bio", "image");
    assertNotNull(userData.toString());
  }

  @Test
  void noArgsConstructor() {
    UserData userData = new UserData();
    assertNull(userData.getId());
    assertNull(userData.getEmail());
    assertNull(userData.getUsername());
    assertNull(userData.getBio());
    assertNull(userData.getImage());
  }
}
