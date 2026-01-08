package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  public void should_create_profile_data_with_all_args_constructor() {
    ProfileData profileData = new ProfileData("id123", "testuser", "bio text", "http://image.url", true);
    
    assertThat(profileData.getId(), is("id123"));
    assertThat(profileData.getUsername(), is("testuser"));
    assertThat(profileData.getBio(), is("bio text"));
    assertThat(profileData.getImage(), is("http://image.url"));
    assertThat(profileData.isFollowing(), is(true));
  }

  @Test
  public void should_create_profile_data_with_no_args_constructor() {
    ProfileData profileData = new ProfileData();
    
    assertThat(profileData.getId(), nullValue());
    assertThat(profileData.getUsername(), nullValue());
    assertThat(profileData.getBio(), nullValue());
    assertThat(profileData.getImage(), nullValue());
    assertThat(profileData.isFollowing(), is(false));
  }

  @Test
  public void should_set_fields_via_setters() {
    ProfileData profileData = new ProfileData();
    profileData.setId("id123");
    profileData.setUsername("testuser");
    profileData.setBio("bio text");
    profileData.setImage("http://image.url");
    profileData.setFollowing(true);
    
    assertThat(profileData.getId(), is("id123"));
    assertThat(profileData.getUsername(), is("testuser"));
    assertThat(profileData.getBio(), is("bio text"));
    assertThat(profileData.getImage(), is("http://image.url"));
    assertThat(profileData.isFollowing(), is(true));
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    ProfileData profileData1 = new ProfileData("id1", "user1", "bio1", "image1", true);
    ProfileData profileData2 = new ProfileData("id1", "user1", "bio1", "image1", true);
    ProfileData profileData3 = new ProfileData("id2", "user1", "bio1", "image1", true);
    
    assertThat(profileData1.equals(profileData2), is(true));
    assertThat(profileData1.equals(profileData3), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_all_fields() {
    ProfileData profileData1 = new ProfileData("id1", "user1", "bio1", "image1", true);
    ProfileData profileData2 = new ProfileData("id1", "user1", "bio1", "image1", true);
    
    assertThat(profileData1.hashCode(), is(profileData2.hashCode()));
  }

  @Test
  public void should_have_toString() {
    ProfileData profileData = new ProfileData("id123", "testuser", "bio", "image", true);
    String toString = profileData.toString();
    
    assertThat(toString.contains("testuser"), is(true));
  }

  @Test
  public void should_not_equal_null() {
    ProfileData profileData = new ProfileData("id1", "user1", "bio1", "image1", true);
    assertThat(profileData.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    ProfileData profileData = new ProfileData("id1", "user1", "bio1", "image1", true);
    assertThat(profileData.equals("string"), is(false));
  }

  @Test
  public void should_handle_following_false() {
    ProfileData profileData = new ProfileData("id1", "user1", "bio1", "image1", false);
    assertThat(profileData.isFollowing(), is(false));
  }
}
