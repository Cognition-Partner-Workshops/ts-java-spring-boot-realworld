package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("test@email.com", "testuser", "password123", "bio text", "image.jpg");

    assertNotNull(user.getId());
    assertEquals("test@email.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password123", user.getPassword());
    assertEquals("bio text", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  public void should_generate_unique_id_for_each_user() {
    User user1 = new User("test1@email.com", "user1", "pass1", "", "");
    User user2 = new User("test2@email.com", "user2", "pass2", "", "");

    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  public void should_update_email_when_not_empty() {
    User user = new User("old@email.com", "username", "password", "bio", "image");
    user.update("new@email.com", "", "", "", "");

    assertEquals("new@email.com", user.getEmail());
    assertEquals("username", user.getUsername());
  }

  @Test
  public void should_update_username_when_not_empty() {
    User user = new User("test@email.com", "oldusername", "password", "bio", "image");
    user.update("", "newusername", "", "", "");

    assertEquals("newusername", user.getUsername());
    assertEquals("test@email.com", user.getEmail());
  }

  @Test
  public void should_update_password_when_not_empty() {
    User user = new User("test@email.com", "username", "oldpassword", "bio", "image");
    user.update("", "", "newpassword", "", "");

    assertEquals("newpassword", user.getPassword());
  }

  @Test
  public void should_update_bio_when_not_empty() {
    User user = new User("test@email.com", "username", "password", "old bio", "image");
    user.update("", "", "", "new bio", "");

    assertEquals("new bio", user.getBio());
  }

  @Test
  public void should_update_image_when_not_empty() {
    User user = new User("test@email.com", "username", "password", "bio", "old.jpg");
    user.update("", "", "", "", "new.jpg");

    assertEquals("new.jpg", user.getImage());
  }

  @Test
  public void should_not_update_fields_when_empty() {
    User user = new User("test@email.com", "username", "password", "bio", "image.jpg");
    user.update("", "", "", "", "");

    assertEquals("test@email.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  public void should_not_update_fields_when_null() {
    User user = new User("test@email.com", "username", "password", "bio", "image.jpg");
    user.update(null, null, null, null, null);

    assertEquals("test@email.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  public void should_update_multiple_fields_at_once() {
    User user = new User("old@email.com", "olduser", "oldpass", "old bio", "old.jpg");
    user.update("new@email.com", "newuser", "newpass", "new bio", "new.jpg");

    assertEquals("new@email.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("new.jpg", user.getImage());
  }

  @Test
  public void should_have_equality_based_on_id() {
    User user1 = new User("test@email.com", "username", "password", "bio", "image");
    User user2 = new User("test@email.com", "username", "password", "bio", "image");

    assertNotEquals(user1, user2);
    assertEquals(user1, user1);
  }
}
