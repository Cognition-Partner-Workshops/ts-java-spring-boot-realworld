package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  public void should_create_profile_data_with_all_args_constructor() {
    ProfileData profileData = new ProfileData("id", "username", "bio", "image", true);

    assertThat(profileData.getId(), is("id"));
    assertThat(profileData.getUsername(), is("username"));
    assertThat(profileData.getBio(), is("bio"));
    assertThat(profileData.getImage(), is("image"));
    assertThat(profileData.isFollowing(), is(true));
  }

  @Test
  public void should_create_profile_data_with_no_args_constructor() {
    ProfileData profileData = new ProfileData();
    
    assertThat(profileData.getId(), is((String) null));
    assertThat(profileData.getUsername(), is((String) null));
  }

  @Test
  public void should_set_and_get_properties() {
    ProfileData profileData = new ProfileData();
    
    profileData.setId("id");
    profileData.setUsername("username");
    profileData.setBio("bio");
    profileData.setImage("image");
    profileData.setFollowing(true);

    assertThat(profileData.getId(), is("id"));
    assertThat(profileData.getUsername(), is("username"));
    assertThat(profileData.getBio(), is("bio"));
    assertThat(profileData.getImage(), is("image"));
    assertThat(profileData.isFollowing(), is(true));
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    ProfileData profileData1 = new ProfileData("id", "username", "bio", "image", true);
    ProfileData profileData2 = new ProfileData("id", "username", "bio", "image", true);
    ProfileData profileData3 = new ProfileData("different-id", "username", "bio", "image", true);

    assertThat(profileData1.equals(profileData2), is(true));
    assertThat(profileData1.equals(profileData3), is(false));
    assertThat(profileData1.hashCode(), is(profileData2.hashCode()));
    assertThat(profileData1.hashCode(), is(not(profileData3.hashCode())));
  }

  @Test
  public void should_implement_to_string() {
    ProfileData profileData = new ProfileData();
    profileData.setId("id");
    profileData.setUsername("username");

    String toString = profileData.toString();

    assertThat(toString, is(notNullValue()));
    assertThat(toString.contains("id"), is(true));
    assertThat(toString.contains("username"), is(true));
  }

  @Test
  public void should_handle_equals_with_null_and_different_type() {
    ProfileData profileData = new ProfileData();
    profileData.setId("id");

    assertThat(profileData.equals(null), is(false));
    assertThat(profileData.equals("string"), is(false));
    assertThat(profileData.equals(profileData), is(true));
  }

  @Test
  public void should_handle_equals_with_null_fields() {
    ProfileData profileData1 = new ProfileData();
    ProfileData profileData2 = new ProfileData();

    assertThat(profileData1.equals(profileData2), is(true));
    assertThat(profileData1.hashCode(), is(profileData2.hashCode()));
  }

  @Test
  public void should_handle_different_following_status() {
    ProfileData profileData1 = new ProfileData("id", "username", "bio", "image", true);
    ProfileData profileData2 = new ProfileData("id", "username", "bio", "image", false);

    assertThat(profileData1.equals(profileData2), is(false));
  }
}
