package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    
    assertThat(user.getId(), is(notNullValue()));
    assertThat(user.getEmail(), is("test@test.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_update_email() {
    User user = new User("old@test.com", "testuser", "password", "bio", "image");
    user.update("new@test.com", null, null, null, null);
    assertThat(user.getEmail(), is("new@test.com"));
  }

  @Test
  public void should_update_username() {
    User user = new User("test@test.com", "olduser", "password", "bio", "image");
    user.update(null, "newuser", null, null, null);
    assertThat(user.getUsername(), is("newuser"));
  }

  @Test
  public void should_update_password() {
    User user = new User("test@test.com", "testuser", "oldpassword", "bio", "image");
    user.update(null, null, "newpassword", null, null);
    assertThat(user.getPassword(), is("newpassword"));
  }

  @Test
  public void should_update_bio() {
    User user = new User("test@test.com", "testuser", "password", "old bio", "image");
    user.update(null, null, null, "new bio", null);
    assertThat(user.getBio(), is("new bio"));
  }

  @Test
  public void should_update_image() {
    User user = new User("test@test.com", "testuser", "password", "bio", "old image");
    user.update(null, null, null, null, "new image");
    assertThat(user.getImage(), is("new image"));
  }

  @Test
  public void should_update_all_fields() {
    User user = new User("old@test.com", "olduser", "oldpassword", "old bio", "old image");
    user.update("new@test.com", "newuser", "newpassword", "new bio", "new image");
    
    assertThat(user.getEmail(), is("new@test.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpassword"));
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("new image"));
  }

  @Test
  public void should_not_update_with_empty_values() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    user.update("", "", "", "", "");
    
    assertThat(user.getEmail(), is("test@test.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_not_update_with_null_values() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    user.update(null, null, null, null, null);
    
    assertThat(user.getEmail(), is("test@test.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_have_unique_id_for_each_user() {
    User user1 = new User("test1@test.com", "user1", "password", "bio", "image");
    User user2 = new User("test2@test.com", "user2", "password", "bio", "image");
    
    assertThat(user1.getId().equals(user2.getId()), is(false));
  }

  @Test
  public void should_have_equals_based_on_id() {
    User user1 = new User("test@test.com", "testuser", "password", "bio", "image");
    User user2 = new User("test@test.com", "testuser", "password", "bio", "image");
    
    assertThat(user1.equals(user1), is(true));
    assertThat(user1.equals(user2), is(false));
  }

  @Test
  public void should_not_equal_null() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    
    assertThat(user.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    
    assertThat(user.equals("string"), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_id() {
    User user1 = new User("test1@test.com", "user1", "password", "bio", "image");
    User user2 = new User("test2@test.com", "user2", "password", "bio", "image");
    
    assertThat(user1.hashCode() != user2.hashCode(), is(true));
  }

  @Test
  public void should_create_with_no_args_constructor() {
    User user = new User();
    
    assertThat(user, is(notNullValue()));
  }

  @Test
  public void should_have_consistent_hashcode() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    int hashCode1 = user.hashCode();
    int hashCode2 = user.hashCode();
    
    assertThat(hashCode1, is(hashCode2));
  }

  @Test
  public void should_handle_null_id_in_equals() {
    User user1 = new User();
    User user2 = new User();
    
    assertThat(user1.equals(user2), is(true));
  }

  @Test
  public void should_not_equal_when_one_id_null() {
    User user1 = new User("test@test.com", "testuser", "password", "bio", "image");
    User user2 = new User();
    
    assertThat(user1.equals(user2), is(false));
  }

  @Test
  public void should_not_equal_when_other_id_null() {
    User user1 = new User();
    User user2 = new User("test@test.com", "testuser", "password", "bio", "image");
    
    assertThat(user1.equals(user2), is(false));
  }
}
