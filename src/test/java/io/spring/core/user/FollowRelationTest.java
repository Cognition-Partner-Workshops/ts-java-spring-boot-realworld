package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  void constructor_withParams() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");

    assertEquals("user-id", relation.getUserId());
    assertEquals("target-id", relation.getTargetId());
  }

  @Test
  void noArgsConstructor() {
    FollowRelation relation = new FollowRelation();

    assertNull(relation.getUserId());
    assertNull(relation.getTargetId());
  }

  @Test
  void setters() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user-id");
    relation.setTargetId("target-id");

    assertEquals("user-id", relation.getUserId());
    assertEquals("target-id", relation.getTargetId());
  }

  @Test
  void equals_sameValues() {
    FollowRelation relation1 = new FollowRelation("user", "target");
    FollowRelation relation2 = new FollowRelation("user", "target");

    assertEquals(relation1, relation2);
    assertEquals(relation1.hashCode(), relation2.hashCode());
  }

  @Test
  void notEquals_differentUserId() {
    FollowRelation relation1 = new FollowRelation("user1", "target");
    FollowRelation relation2 = new FollowRelation("user2", "target");

    assertNotEquals(relation1, relation2);
  }

  @Test
  void notEquals_differentTargetId() {
    FollowRelation relation1 = new FollowRelation("user", "target1");
    FollowRelation relation2 = new FollowRelation("user", "target2");

    assertNotEquals(relation1, relation2);
  }

  @Test
  void toString_notNull() {
    FollowRelation relation = new FollowRelation("user", "target");
    assertNotNull(relation.toString());
  }
}
