package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation_with_constructor() {
    String userId = "user123";
    String targetId = "target456";

    FollowRelation relation = new FollowRelation(userId, targetId);

    assertThat(relation.getUserId(), is(userId));
    assertThat(relation.getTargetId(), is(targetId));
  }

  @Test
  public void should_create_follow_relation_with_no_args_constructor() {
    FollowRelation relation = new FollowRelation();

    assertThat(relation.getUserId(), is((String) null));
    assertThat(relation.getTargetId(), is((String) null));
  }

  @Test
  public void should_set_and_get_user_id() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user123");

    assertThat(relation.getUserId(), is("user123"));
  }

  @Test
  public void should_set_and_get_target_id() {
    FollowRelation relation = new FollowRelation();
    relation.setTargetId("target456");

    assertThat(relation.getTargetId(), is("target456"));
  }

  @Test
  public void should_have_equals_and_hashcode() {
    FollowRelation relation1 = new FollowRelation("user123", "target456");
    FollowRelation relation2 = new FollowRelation("user123", "target456");

    assertThat(relation1.equals(relation2), is(true));
    assertThat(relation1.hashCode(), is(relation2.hashCode()));
  }

  @Test
  public void should_not_be_equal_when_different_user_id() {
    FollowRelation relation1 = new FollowRelation("user123", "target456");
    FollowRelation relation2 = new FollowRelation("user789", "target456");

    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_not_be_equal_when_different_target_id() {
    FollowRelation relation1 = new FollowRelation("user123", "target456");
    FollowRelation relation2 = new FollowRelation("user123", "target789");

    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_have_to_string() {
    FollowRelation relation = new FollowRelation("user123", "target456");

    assertThat(relation.toString(), notNullValue());
  }

  @Test
  public void should_not_be_equal_to_null() {
    FollowRelation relation = new FollowRelation("user123", "target456");

    assertThat(relation.equals(null), is(false));
  }

  @Test
  public void should_not_be_equal_to_different_type() {
    FollowRelation relation = new FollowRelation("user123", "target456");

    assertThat(relation.equals("string"), is(false));
  }

  @Test
  public void should_be_equal_to_itself() {
    FollowRelation relation = new FollowRelation("user123", "target456");

    assertThat(relation.equals(relation), is(true));
  }
}
