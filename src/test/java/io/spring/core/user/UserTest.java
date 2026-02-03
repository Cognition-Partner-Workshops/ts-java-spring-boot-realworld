package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    assertNotNull(user.getId());
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_create_user_with_empty_bio_and_image() {
    User user = new User("test@test.com", "testuser", "password", "", "");

    assertNotNull(user.getId());
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("", user.getBio());
    assertEquals("", user.getImage());
  }

  @Test
  public void should_update_all_fields() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    user.update("new@test.com", "newuser", "newpassword", "new bio", "new image");

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpassword", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("new image", user.getImage());
  }

  @Test
  public void should_not_update_email_when_empty() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    user.update("", "newuser", "newpassword", "new bio", "new image");

    assertEquals("test@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
  }

  @Test
  public void should_not_update_username_when_empty() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    user.update("new@test.com", "", "newpassword", "new bio", "new image");

    assertEquals("new@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
  }

  @Test
  public void should_not_update_password_when_empty() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    user.update("new@test.com", "newuser", "", "new bio", "new image");

    assertEquals("password", user.getPassword());
  }

  @Test
  public void should_not_update_bio_when_empty() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    user.update("new@test.com", "newuser", "newpassword", "", "new image");

    assertEquals("bio", user.getBio());
  }

  @Test
  public void should_not_update_image_when_empty() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    user.update("new@test.com", "newuser", "newpassword", "new bio", "");

    assertEquals("image", user.getImage());
  }

  @Test
  public void should_not_update_fields_when_null() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    user.update(null, null, null, null, null);

    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_have_unique_ids() {
    User user1 = new User("test1@test.com", "testuser1", "password", "", "");
    User user2 = new User("test2@test.com", "testuser2", "password", "", "");

    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  public void should_be_equal_when_same_id() {
    User user1 = new User("test@test.com", "testuser", "password", "", "");
    User user2 = user1;

    assertEquals(user1, user2);
  }

  @Test
  public void should_not_be_equal_when_different_id() {
    User user1 = new User("test@test.com", "testuser", "password", "", "");
    User user2 = new User("test@test.com", "testuser", "password", "", "");

    assertNotEquals(user1, user2);
  }
}
