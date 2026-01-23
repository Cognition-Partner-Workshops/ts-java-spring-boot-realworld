package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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

    assertThat(user.getId(), notNullValue());
    assertThat(user.getEmail(), is(email));
    assertThat(user.getUsername(), is(username));
    assertThat(user.getPassword(), is(password));
    assertThat(user.getBio(), is(bio));
    assertThat(user.getImage(), is(image));
  }

  @Test
  public void should_generate_unique_id_for_each_user() {
    User user1 = new User("test1@example.com", "user1", "pass1", "", "");
    User user2 = new User("test2@example.com", "user2", "pass2", "", "");

    assertThat(user1.getId(), not(user2.getId()));
  }

  @Test
  public void should_update_email_when_not_empty() {
    User user = new User("old@example.com", "username", "password", "bio", "image");
    String newEmail = "new@example.com";

    user.update(newEmail, "", "", "", "");

    assertThat(user.getEmail(), is(newEmail));
    assertThat(user.getUsername(), is("username"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_not_update_email_when_empty() {
    User user = new User("old@example.com", "username", "password", "bio", "image");

    user.update("", "", "", "", "");

    assertThat(user.getEmail(), is("old@example.com"));
  }

  @Test
  public void should_not_update_email_when_null() {
    User user = new User("old@example.com", "username", "password", "bio", "image");

    user.update(null, "", "", "", "");

    assertThat(user.getEmail(), is("old@example.com"));
  }

  @Test
  public void should_update_username_when_not_empty() {
    User user = new User("email@example.com", "oldusername", "password", "bio", "image");
    String newUsername = "newusername";

    user.update("", newUsername, "", "", "");

    assertThat(user.getUsername(), is(newUsername));
    assertThat(user.getEmail(), is("email@example.com"));
  }

  @Test
  public void should_not_update_username_when_empty() {
    User user = new User("email@example.com", "oldusername", "password", "bio", "image");

    user.update("", "", "", "", "");

    assertThat(user.getUsername(), is("oldusername"));
  }

  @Test
  public void should_not_update_username_when_null() {
    User user = new User("email@example.com", "oldusername", "password", "bio", "image");

    user.update("", null, "", "", "");

    assertThat(user.getUsername(), is("oldusername"));
  }

  @Test
  public void should_update_password_when_not_empty() {
    User user = new User("email@example.com", "username", "oldpassword", "bio", "image");
    String newPassword = "newpassword";

    user.update("", "", newPassword, "", "");

    assertThat(user.getPassword(), is(newPassword));
  }

  @Test
  public void should_not_update_password_when_empty() {
    User user = new User("email@example.com", "username", "oldpassword", "bio", "image");

    user.update("", "", "", "", "");

    assertThat(user.getPassword(), is("oldpassword"));
  }

  @Test
  public void should_not_update_password_when_null() {
    User user = new User("email@example.com", "username", "oldpassword", "bio", "image");

    user.update("", "", null, "", "");

    assertThat(user.getPassword(), is("oldpassword"));
  }

  @Test
  public void should_update_bio_when_not_empty() {
    User user = new User("email@example.com", "username", "password", "oldbio", "image");
    String newBio = "newbio";

    user.update("", "", "", newBio, "");

    assertThat(user.getBio(), is(newBio));
  }

  @Test
  public void should_not_update_bio_when_empty() {
    User user = new User("email@example.com", "username", "password", "oldbio", "image");

    user.update("", "", "", "", "");

    assertThat(user.getBio(), is("oldbio"));
  }

  @Test
  public void should_not_update_bio_when_null() {
    User user = new User("email@example.com", "username", "password", "oldbio", "image");

    user.update("", "", "", null, "");

    assertThat(user.getBio(), is("oldbio"));
  }

  @Test
  public void should_update_image_when_not_empty() {
    User user = new User("email@example.com", "username", "password", "bio", "oldimage");
    String newImage = "newimage";

    user.update("", "", "", "", newImage);

    assertThat(user.getImage(), is(newImage));
  }

  @Test
  public void should_not_update_image_when_empty() {
    User user = new User("email@example.com", "username", "password", "bio", "oldimage");

    user.update("", "", "", "", "");

    assertThat(user.getImage(), is("oldimage"));
  }

  @Test
  public void should_not_update_image_when_null() {
    User user = new User("email@example.com", "username", "password", "bio", "oldimage");

    user.update("", "", "", "", null);

    assertThat(user.getImage(), is("oldimage"));
  }

  @Test
  public void should_update_all_fields_at_once() {
    User user = new User("old@example.com", "olduser", "oldpass", "oldbio", "oldimage");

    user.update("new@example.com", "newuser", "newpass", "newbio", "newimage");

    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("newbio"));
    assertThat(user.getImage(), is("newimage"));
  }

  @Test
  public void should_be_equal_when_same_id() {
    User user1 = new User("test@example.com", "user", "pass", "", "");
    User user2 = user1;

    assertThat(user1.equals(user2), is(true));
    assertThat(user1.hashCode(), is(user2.hashCode()));
  }

  @Test
  public void should_not_be_equal_when_different_id() {
    User user1 = new User("test@example.com", "user", "pass", "", "");
    User user2 = new User("test@example.com", "user", "pass", "", "");

    assertThat(user1.equals(user2), is(false));
  }
}
