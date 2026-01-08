package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("test@example.com", "testuser", "password123", "Test bio", "http://image.url");
    
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password123"));
    assertThat(user.getBio(), is("Test bio"));
    assertThat(user.getImage(), is("http://image.url"));
  }

  @Test
  public void should_generate_uuid_for_id() {
    User user = new User("test@example.com", "testuser", "password123", "bio", "image");
    
    assertThat(user.getId(), notNullValue());
    assertThat(user.getId().length(), is(36));
  }

  @Test
  public void should_generate_unique_ids_for_different_users() {
    User user1 = new User("test1@example.com", "user1", "pass1", "bio1", "img1");
    User user2 = new User("test2@example.com", "user2", "pass2", "bio2", "img2");
    
    assertThat(user1.getId(), not(user2.getId()));
  }

  @Test
  public void should_update_email_when_not_empty() {
    User user = new User("old@example.com", "testuser", "password", "bio", "image");
    
    user.update("new@example.com", null, null, null, null);
    
    assertThat(user.getEmail(), is("new@example.com"));
  }

  @Test
  public void should_not_update_email_when_empty() {
    User user = new User("old@example.com", "testuser", "password", "bio", "image");
    
    user.update("", null, null, null, null);
    
    assertThat(user.getEmail(), is("old@example.com"));
  }

  @Test
  public void should_not_update_email_when_null() {
    User user = new User("old@example.com", "testuser", "password", "bio", "image");
    
    user.update(null, null, null, null, null);
    
    assertThat(user.getEmail(), is("old@example.com"));
  }

  @Test
  public void should_update_username_when_not_empty() {
    User user = new User("test@example.com", "olduser", "password", "bio", "image");
    
    user.update(null, "newuser", null, null, null);
    
    assertThat(user.getUsername(), is("newuser"));
  }

  @Test
  public void should_not_update_username_when_empty() {
    User user = new User("test@example.com", "olduser", "password", "bio", "image");
    
    user.update(null, "", null, null, null);
    
    assertThat(user.getUsername(), is("olduser"));
  }

  @Test
  public void should_update_password_when_not_empty() {
    User user = new User("test@example.com", "testuser", "oldpass", "bio", "image");
    
    user.update(null, null, "newpass", null, null);
    
    assertThat(user.getPassword(), is("newpass"));
  }

  @Test
  public void should_not_update_password_when_empty() {
    User user = new User("test@example.com", "testuser", "oldpass", "bio", "image");
    
    user.update(null, null, "", null, null);
    
    assertThat(user.getPassword(), is("oldpass"));
  }

  @Test
  public void should_update_bio_when_not_empty() {
    User user = new User("test@example.com", "testuser", "password", "old bio", "image");
    
    user.update(null, null, null, "new bio", null);
    
    assertThat(user.getBio(), is("new bio"));
  }

  @Test
  public void should_not_update_bio_when_empty() {
    User user = new User("test@example.com", "testuser", "password", "old bio", "image");
    
    user.update(null, null, null, "", null);
    
    assertThat(user.getBio(), is("old bio"));
  }

  @Test
  public void should_update_image_when_not_empty() {
    User user = new User("test@example.com", "testuser", "password", "bio", "http://old.image");
    
    user.update(null, null, null, null, "http://new.image");
    
    assertThat(user.getImage(), is("http://new.image"));
  }

  @Test
  public void should_not_update_image_when_empty() {
    User user = new User("test@example.com", "testuser", "password", "bio", "http://old.image");
    
    user.update(null, null, null, null, "");
    
    assertThat(user.getImage(), is("http://old.image"));
  }

  @Test
  public void should_update_multiple_fields_at_once() {
    User user = new User("old@example.com", "olduser", "oldpass", "old bio", "http://old.image");
    
    user.update("new@example.com", "newuser", "newpass", "new bio", "http://new.image");
    
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("http://new.image"));
  }

  @Test
  public void should_preserve_id_after_update() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    String originalId = user.getId();
    
    user.update("new@example.com", "newuser", "newpass", "new bio", "http://new.image");
    
    assertThat(user.getId(), is(originalId));
  }

  @Test
  public void should_have_equal_users_with_same_id() {
    User user1 = new User("test@example.com", "testuser", "password", "bio", "image");
    User user2 = new User("other@example.com", "otheruser", "otherpass", "other bio", "other image");
    
    assertThat(user1.equals(user2), is(false));
  }

  @Test
  public void should_create_user_with_null_bio_and_image() {
    User user = new User("test@example.com", "testuser", "password", null, null);
    
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is((String) null));
    assertThat(user.getImage(), is((String) null));
  }
}
