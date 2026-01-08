package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation_with_constructor() {
    FollowRelation relation = new FollowRelation("user123", "target456");
    
    assertThat(relation.getUserId(), is("user123"));
    assertThat(relation.getTargetId(), is("target456"));
  }

  @Test
  public void should_create_follow_relation_with_no_arg_constructor() {
    FollowRelation relation = new FollowRelation();
    assertThat(relation.getUserId(), is((String) null));
    assertThat(relation.getTargetId(), is((String) null));
  }

  @Test
  public void should_set_user_id_via_setter() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user123");
    assertThat(relation.getUserId(), is("user123"));
  }

  @Test
  public void should_set_target_id_via_setter() {
    FollowRelation relation = new FollowRelation();
    relation.setTargetId("target456");
    assertThat(relation.getTargetId(), is("target456"));
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    FollowRelation relation1 = new FollowRelation("user1", "target1");
    FollowRelation relation2 = new FollowRelation("user1", "target1");
    FollowRelation relation3 = new FollowRelation("user1", "target2");
    FollowRelation relation4 = new FollowRelation("user2", "target1");
    
    assertThat(relation1.equals(relation2), is(true));
    assertThat(relation1.equals(relation3), is(false));
    assertThat(relation1.equals(relation4), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_all_fields() {
    FollowRelation relation1 = new FollowRelation("user1", "target1");
    FollowRelation relation2 = new FollowRelation("user1", "target1");
    FollowRelation relation3 = new FollowRelation("user1", "target2");
    
    assertThat(relation1.hashCode(), is(relation2.hashCode()));
    assertThat(relation1.hashCode() == relation3.hashCode(), is(false));
  }

  @Test
  public void should_not_equal_null() {
    FollowRelation relation = new FollowRelation("user1", "target1");
    assertThat(relation.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    FollowRelation relation = new FollowRelation("user1", "target1");
    assertThat(relation.equals("string"), is(false));
  }

  @Test
  public void should_have_toString() {
    FollowRelation relation = new FollowRelation("user123", "target456");
    String toString = relation.toString();
    assertThat(toString.contains("user123"), is(true));
    assertThat(toString.contains("target456"), is(true));
  }

  @Test
  public void should_equal_itself() {
    FollowRelation relation = new FollowRelation("user1", "target1");
    assertThat(relation.equals(relation), is(true));
  }
}
