package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation_with_user_and_target() {
    FollowRelation relation = new FollowRelation("user123", "target456");

    assertEquals("user123", relation.getUserId());
    assertEquals("target456", relation.getTargetId());
  }

  @Test
  public void should_create_follow_relation_with_no_args_constructor() {
    FollowRelation relation = new FollowRelation();

    assertNull(relation.getUserId());
    assertNull(relation.getTargetId());
  }

  @Test
  public void should_set_user_id() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user123");

    assertEquals("user123", relation.getUserId());
  }

  @Test
  public void should_set_target_id() {
    FollowRelation relation = new FollowRelation();
    relation.setTargetId("target456");

    assertEquals("target456", relation.getTargetId());
  }

  @Test
  public void should_have_equality_based_on_all_fields() {
    FollowRelation relation1 = new FollowRelation("user1", "target1");
    FollowRelation relation2 = new FollowRelation("user1", "target1");
    FollowRelation relation3 = new FollowRelation("user1", "target2");

    assertEquals(relation1, relation2);
    assertNotEquals(relation1, relation3);
  }

  @Test
  public void should_have_consistent_hash_code() {
    FollowRelation relation1 = new FollowRelation("user1", "target1");
    FollowRelation relation2 = new FollowRelation("user1", "target1");

    assertEquals(relation1.hashCode(), relation2.hashCode());
  }

  @Test
  public void should_have_to_string_representation() {
    FollowRelation relation = new FollowRelation("user123", "target456");

    String toString = relation.toString();
    assertTrue(toString.contains("user123"));
    assertTrue(toString.contains("target456"));
  }
}
