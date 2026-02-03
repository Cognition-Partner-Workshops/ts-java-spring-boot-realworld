package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  void constructor_withParams() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");

    assertNotNull(user.getId());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  void noArgsConstructor() {
    User user = new User();

    assertNull(user.getId());
    assertNull(user.getEmail());
    assertNull(user.getUsername());
    assertNull(user.getPassword());
    assertNull(user.getBio());
    assertNull(user.getImage());
  }

  @Test
  void update_allFields() {
    User user = new User("old@example.com", "olduser", "oldpass", "oldbio", "oldimage");

    user.update("new@example.com", "newuser", "newpass", "newbio", "newimage");

    assertEquals("new@example.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("newimage", user.getImage());
  }

  @Test
  void update_partialFields() {
    User user = new User("old@example.com", "olduser", "oldpass", "oldbio", "oldimage");

    user.update("new@example.com", null, "", "newbio", "");

    assertEquals("new@example.com", user.getEmail());
    assertEquals("olduser", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("oldimage", user.getImage());
  }

  @Test
  void update_noFields() {
    User user = new User("old@example.com", "olduser", "oldpass", "oldbio", "oldimage");

    user.update(null, null, null, null, null);

    assertEquals("old@example.com", user.getEmail());
    assertEquals("olduser", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("oldbio", user.getBio());
    assertEquals("oldimage", user.getImage());
  }

  @Test
  void update_emptyStrings() {
    User user = new User("old@example.com", "olduser", "oldpass", "oldbio", "oldimage");

    user.update("", "", "", "", "");

    assertEquals("old@example.com", user.getEmail());
    assertEquals("olduser", user.getUsername());
    assertEquals("oldpass", user.getPassword());
    assertEquals("oldbio", user.getBio());
    assertEquals("oldimage", user.getImage());
  }

  @Test
  void equals_sameId() {
    User user1 = new User("email1@test.com", "user1", "pass1", "bio1", "image1");
    User user2 = new User("email2@test.com", "user2", "pass2", "bio2", "image2");

    assertNotEquals(user1, user2);
  }

  @Test
  void equals_sameUser() {
    User user = new User("email@test.com", "user", "pass", "bio", "image");

    assertEquals(user, user);
  }

  @Test
  void hashCode_consistent() {
    User user = new User("email@test.com", "user", "pass", "bio", "image");
    int hash1 = user.hashCode();
    int hash2 = user.hashCode();

    assertEquals(hash1, hash2);
  }
}
