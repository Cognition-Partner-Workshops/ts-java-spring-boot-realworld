package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  void constructor_and_getters() {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image", true);

    assertEquals("user-id", profileData.getId());
    assertEquals("testuser", profileData.getUsername());
    assertEquals("bio", profileData.getBio());
    assertEquals("image", profileData.getImage());
    assertTrue(profileData.isFollowing());
  }

  @Test
  void setters() {
    ProfileData profileData = new ProfileData();

    profileData.setId("new-id");
    profileData.setUsername("newuser");
    profileData.setBio("new bio");
    profileData.setImage("new image");
    profileData.setFollowing(true);

    assertEquals("new-id", profileData.getId());
    assertEquals("newuser", profileData.getUsername());
    assertEquals("new bio", profileData.getBio());
    assertEquals("new image", profileData.getImage());
    assertTrue(profileData.isFollowing());
  }

  @Test
  void equals_and_hashCode() {
    ProfileData profileData1 = new ProfileData("id", "user", "bio", "image", false);
    ProfileData profileData2 = new ProfileData("id", "user", "bio", "image", false);

    assertEquals(profileData1, profileData2);
    assertEquals(profileData1.hashCode(), profileData2.hashCode());
  }

  @Test
  void notEquals_differentId() {
    ProfileData profileData1 = new ProfileData("id1", "user", "bio", "image", false);
    ProfileData profileData2 = new ProfileData("id2", "user", "bio", "image", false);

    assertNotEquals(profileData1, profileData2);
  }

  @Test
  void toString_notNull() {
    ProfileData profileData = new ProfileData("id", "user", "bio", "image", false);
    assertNotNull(profileData.toString());
  }

  @Test
  void noArgsConstructor() {
    ProfileData profileData = new ProfileData();
    assertNull(profileData.getId());
    assertNull(profileData.getUsername());
    assertNull(profileData.getBio());
    assertNull(profileData.getImage());
    assertFalse(profileData.isFollowing());
  }
}
