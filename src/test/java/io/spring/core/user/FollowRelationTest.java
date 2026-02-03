package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation_with_all_fields() {
    String userId = "user-123";
    String targetId = "target-456";

    FollowRelation relation = new FollowRelation(userId, targetId);

    assertEquals(userId, relation.getUserId());
    assertEquals(targetId, relation.getTargetId());
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    FollowRelation relation1 = new FollowRelation("user-1", "target-1");
    FollowRelation relation2 = new FollowRelation("user-1", "target-1");
    FollowRelation relation3 = new FollowRelation("user-2", "target-1");
    FollowRelation relation4 = new FollowRelation("user-1", "target-2");

    assertEquals(relation1, relation2);
    assertNotEquals(relation1, relation3);
    assertNotEquals(relation1, relation4);
  }

  @Test
  public void should_have_consistent_hashcode() {
    FollowRelation relation1 = new FollowRelation("user-1", "target-1");
    FollowRelation relation2 = new FollowRelation("user-1", "target-1");

    assertEquals(relation1.hashCode(), relation2.hashCode());
  }

  @Test
  public void should_store_user_id_correctly() {
    String userId = "specific-user-id-12345";
    FollowRelation relation = new FollowRelation(userId, "target-1");

    assertEquals(userId, relation.getUserId());
  }

  @Test
  public void should_store_target_id_correctly() {
    String targetId = "specific-target-id-67890";
    FollowRelation relation = new FollowRelation("user-1", targetId);

    assertEquals(targetId, relation.getTargetId());
  }

  @Test
  public void should_allow_setting_user_id() {
    FollowRelation relation = new FollowRelation("user-1", "target-1");
    relation.setUserId("new-user-id");

    assertEquals("new-user-id", relation.getUserId());
  }

  @Test
  public void should_allow_setting_target_id() {
    FollowRelation relation = new FollowRelation("user-1", "target-1");
    relation.setTargetId("new-target-id");

    assertEquals("new-target-id", relation.getTargetId());
  }

  @Test
  public void should_not_equal_null() {
    FollowRelation relation = new FollowRelation("user-1", "target-1");

    assertNotEquals(null, relation);
  }

  @Test
  public void should_equal_itself() {
    FollowRelation relation = new FollowRelation("user-1", "target-1");

    assertEquals(relation, relation);
  }

  @Test
  public void should_have_to_string() {
    FollowRelation relation = new FollowRelation("user-1", "target-1");

    String toString = relation.toString();
    assertNotNull(toString);
    assertTrue(toString.contains("user-1"));
    assertTrue(toString.contains("target-1"));
  }
}
