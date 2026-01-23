package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  public void should_create_user_with_token_from_user_data() {
    UserData userData =
        new UserData("123", "test@example.com", "testuser", "test bio", "https://example.com/image.jpg");
    String token = "jwt-token-123";

    UserWithToken userWithToken = new UserWithToken(userData, token);

    assertThat(userWithToken.getEmail(), is("test@example.com"));
    assertThat(userWithToken.getUsername(), is("testuser"));
    assertThat(userWithToken.getBio(), is("test bio"));
    assertThat(userWithToken.getImage(), is("https://example.com/image.jpg"));
    assertThat(userWithToken.getToken(), is(token));
  }

  @Test
  public void should_handle_empty_bio_and_image() {
    UserData userData = new UserData("123", "test@example.com", "testuser", "", "");
    String token = "jwt-token-456";

    UserWithToken userWithToken = new UserWithToken(userData, token);

    assertThat(userWithToken.getEmail(), is("test@example.com"));
    assertThat(userWithToken.getUsername(), is("testuser"));
    assertThat(userWithToken.getBio(), is(""));
    assertThat(userWithToken.getImage(), is(""));
    assertThat(userWithToken.getToken(), is(token));
  }

  @Test
  public void should_handle_null_bio_and_image() {
    UserData userData = new UserData("123", "test@example.com", "testuser", null, null);
    String token = "jwt-token-789";

    UserWithToken userWithToken = new UserWithToken(userData, token);

    assertThat(userWithToken.getEmail(), is("test@example.com"));
    assertThat(userWithToken.getUsername(), is("testuser"));
    assertThat(userWithToken.getBio(), is((String) null));
    assertThat(userWithToken.getImage(), is((String) null));
    assertThat(userWithToken.getToken(), is(token));
  }
}
