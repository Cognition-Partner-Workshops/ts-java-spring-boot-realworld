package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    String email = "test@example.com";
    String username = "testuser";
    String password = "password123";
    String bio = "Test bio";
    String image = "https://example.com/image.jpg";

    User user = new User(email, username, password, bio, image);

    assertNotNull(user.getId());
    assertEquals(email, user.getEmail());
    assertEquals(username, user.getUsername());
    assertEquals(password, user.getPassword());
    assertEquals(bio, user.getBio());
    assertEquals(image, user.getImage());
  }

  @Test
  public void should_generate_unique_id_for_each_user() {
    User user1 = new User("user1@example.com", "user1", "pass1", "bio1", "image1");
    User user2 = new User("user2@example.com", "user2", "pass2", "bio2", "image2");

    assertNotNull(user1.getId());
    assertNotNull(user2.getId());
    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  public void should_update_email_when_not_empty() {
    User user = new User("old@example.com", "username", "password", "bio", "image");
    String newEmail = "new@example.com";

    user.update(newEmail, null, null, null, null);

    assertEquals(newEmail, user.getEmail());
  }

  @Test
  public void should_not_update_email_when_empty() {
    User user = new User("original@example.com", "username", "password", "bio", "image");

    user.update("", null, null, null, null);

    assertEquals("original@example.com", user.getEmail());
  }

  @Test
  public void should_not_update_email_when_null() {
    User user = new User("original@example.com", "username", "password", "bio", "image");

    user.update(null, null, null, null, null);

    assertEquals("original@example.com", user.getEmail());
  }

  @Test
  public void should_update_username_when_not_empty() {
    User user = new User("email@example.com", "oldusername", "password", "bio", "image");
    String newUsername = "newusername";

    user.update(null, newUsername, null, null, null);

    assertEquals(newUsername, user.getUsername());
  }

  @Test
  public void should_not_update_username_when_empty() {
    User user = new User("email@example.com", "originalusername", "password", "bio", "image");

    user.update(null, "", null, null, null);

    assertEquals("originalusername", user.getUsername());
  }

  @Test
  public void should_update_password_when_not_empty() {
    User user = new User("email@example.com", "username", "oldpassword", "bio", "image");
    String newPassword = "newpassword";

    user.update(null, null, newPassword, null, null);

    assertEquals(newPassword, user.getPassword());
  }

  @Test
  public void should_not_update_password_when_empty() {
    User user = new User("email@example.com", "username", "originalpassword", "bio", "image");

    user.update(null, null, "", null, null);

    assertEquals("originalpassword", user.getPassword());
  }

  @Test
  public void should_update_bio_when_not_empty() {
    User user = new User("email@example.com", "username", "password", "old bio", "image");
    String newBio = "new bio";

    user.update(null, null, null, newBio, null);

    assertEquals(newBio, user.getBio());
  }

  @Test
  public void should_not_update_bio_when_empty() {
    User user = new User("email@example.com", "username", "password", "original bio", "image");

    user.update(null, null, null, "", null);

    assertEquals("original bio", user.getBio());
  }

  @Test
  public void should_update_image_when_not_empty() {
    User user = new User("email@example.com", "username", "password", "bio", "old image");
    String newImage = "new image";

    user.update(null, null, null, null, newImage);

    assertEquals(newImage, user.getImage());
  }

  @Test
  public void should_not_update_image_when_empty() {
    User user = new User("email@example.com", "username", "password", "bio", "original image");

    user.update(null, null, null, null, "");

    assertEquals("original image", user.getImage());
  }

  @Test
  public void should_update_all_fields_at_once() {
    User user = new User("old@example.com", "olduser", "oldpass", "old bio", "old image");

    user.update("new@example.com", "newuser", "newpass", "new bio", "new image");

    assertEquals("new@example.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("new image", user.getImage());
  }

  @Test
  public void should_have_equals_based_on_id() {
    User user1 = new User("email@example.com", "username", "password", "bio", "image");
    User user2 = new User("email@example.com", "username", "password", "bio", "image");

    assertNotEquals(user1, user2);
    assertEquals(user1, user1);
  }

  @Test
  public void should_have_consistent_hashcode() {
    User user = new User("email@example.com", "username", "password", "bio", "image");

    int hashCode1 = user.hashCode();
    int hashCode2 = user.hashCode();

    assertEquals(hashCode1, hashCode2);
  }
}
