package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("User Entity Tests")
public class UserTest {

  @Test
  @DisplayName("should create user with all fields")
  public void should_create_user_with_all_fields() {
    User user = new User("test@example.com", "testuser", "password123", "Test bio", "http://example.com/image.jpg");
    
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password123"));
    assertThat(user.getBio(), is("Test bio"));
    assertThat(user.getImage(), is("http://example.com/image.jpg"));
  }

  @Test
  @DisplayName("should generate unique id on creation")
  public void should_generate_unique_id_on_creation() {
    User user1 = new User("test1@example.com", "user1", "pass1", "bio1", "image1");
    User user2 = new User("test2@example.com", "user2", "pass2", "bio2", "image2");
    
    assertThat(user1.getId(), notNullValue());
    assertThat(user2.getId(), notNullValue());
    assertThat(user1.getId(), not(is(user2.getId())));
  }

  @Test
  @DisplayName("should update email when not empty")
  public void should_update_email_when_not_empty() {
    User user = new User("old@example.com", "testuser", "password", "bio", "image");
    user.update("new@example.com", null, null, null, null);
    
    assertThat(user.getEmail(), is("new@example.com"));
  }

  @Test
  @DisplayName("should not update email when empty")
  public void should_not_update_email_when_empty() {
    User user = new User("old@example.com", "testuser", "password", "bio", "image");
    user.update("", null, null, null, null);
    
    assertThat(user.getEmail(), is("old@example.com"));
  }

  @Test
  @DisplayName("should update username when not empty")
  public void should_update_username_when_not_empty() {
    User user = new User("test@example.com", "olduser", "password", "bio", "image");
    user.update(null, "newuser", null, null, null);
    
    assertThat(user.getUsername(), is("newuser"));
  }

  @Test
  @DisplayName("should not update username when empty")
  public void should_not_update_username_when_empty() {
    User user = new User("test@example.com", "olduser", "password", "bio", "image");
    user.update(null, "", null, null, null);
    
    assertThat(user.getUsername(), is("olduser"));
  }

  @Test
  @DisplayName("should update password when not empty")
  public void should_update_password_when_not_empty() {
    User user = new User("test@example.com", "testuser", "oldpass", "bio", "image");
    user.update(null, null, "newpass", null, null);
    
    assertThat(user.getPassword(), is("newpass"));
  }

  @Test
  @DisplayName("should not update password when empty")
  public void should_not_update_password_when_empty() {
    User user = new User("test@example.com", "testuser", "oldpass", "bio", "image");
    user.update(null, null, "", null, null);
    
    assertThat(user.getPassword(), is("oldpass"));
  }

  @Test
  @DisplayName("should update bio when not empty")
  public void should_update_bio_when_not_empty() {
    User user = new User("test@example.com", "testuser", "password", "old bio", "image");
    user.update(null, null, null, "new bio", null);
    
    assertThat(user.getBio(), is("new bio"));
  }

  @Test
  @DisplayName("should not update bio when empty")
  public void should_not_update_bio_when_empty() {
    User user = new User("test@example.com", "testuser", "password", "old bio", "image");
    user.update(null, null, null, "", null);
    
    assertThat(user.getBio(), is("old bio"));
  }

  @Test
  @DisplayName("should update image when not empty")
  public void should_update_image_when_not_empty() {
    User user = new User("test@example.com", "testuser", "password", "bio", "old-image.jpg");
    user.update(null, null, null, null, "new-image.jpg");
    
    assertThat(user.getImage(), is("new-image.jpg"));
  }

  @Test
  @DisplayName("should not update image when empty")
  public void should_not_update_image_when_empty() {
    User user = new User("test@example.com", "testuser", "password", "bio", "old-image.jpg");
    user.update(null, null, null, null, "");
    
    assertThat(user.getImage(), is("old-image.jpg"));
  }

  @Test
  @DisplayName("should update multiple fields at once")
  public void should_update_multiple_fields_at_once() {
    User user = new User("old@example.com", "olduser", "oldpass", "old bio", "old-image.jpg");
    user.update("new@example.com", "newuser", "newpass", "new bio", "new-image.jpg");
    
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("new-image.jpg"));
  }

  @Test
  @DisplayName("should preserve id after update")
  public void should_preserve_id_after_update() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    String originalId = user.getId();
    
    user.update("new@example.com", "newuser", "newpass", "new bio", "new-image.jpg");
    
    assertThat(user.getId(), is(originalId));
  }

  @Test
  @DisplayName("users with same id should be equal")
  public void users_with_same_id_should_be_equal() {
    User user1 = new User("test1@example.com", "user1", "pass1", "bio1", "image1");
    User user2 = new User("test2@example.com", "user2", "pass2", "bio2", "image2");
    
    assertThat(user1.equals(user1), is(true));
    assertThat(user1.equals(user2), is(false));
  }

  @Test
  @DisplayName("should handle null values in constructor")
  public void should_handle_null_values_in_constructor() {
    User user = new User("test@example.com", "testuser", "password", null, null);
    
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is((String) null));
    assertThat(user.getImage(), is((String) null));
  }
}
