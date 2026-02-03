package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FollowRelationTest {

  @Test
  void constructor_createsWithAllFields() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");

    assertEquals("user-id", relation.getUserId());
    assertEquals("target-id", relation.getTargetId());
  }

  @Test
  void noArgsConstructor_createsEmptyObject() {
    FollowRelation relation = new FollowRelation();

    assertNull(relation.getUserId());
    assertNull(relation.getTargetId());
  }

  @Test
  void setters_updateFields() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user-id");
    relation.setTargetId("target-id");

    assertEquals("user-id", relation.getUserId());
    assertEquals("target-id", relation.getTargetId());
  }

  @Test
  void equals_withSameData() {
    FollowRelation relation1 = new FollowRelation("user-id", "target-id");
    FollowRelation relation2 = new FollowRelation("user-id", "target-id");

    assertEquals(relation1, relation2);
    assertEquals(relation1.hashCode(), relation2.hashCode());
  }

  @Test
  void equals_withDifferentUserId() {
    FollowRelation relation1 = new FollowRelation("user-id-1", "target-id");
    FollowRelation relation2 = new FollowRelation("user-id-2", "target-id");

    assertNotEquals(relation1, relation2);
  }

  @Test
  void equals_withDifferentTargetId() {
    FollowRelation relation1 = new FollowRelation("user-id", "target-id-1");
    FollowRelation relation2 = new FollowRelation("user-id", "target-id-2");

    assertNotEquals(relation1, relation2);
  }

  @Test
  void equals_withNull() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");

    assertNotEquals(relation, null);
  }

  @Test
  void equals_withSameObject() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");

    assertEquals(relation, relation);
  }

  @Test
  void toString_returnsString() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");

    String str = relation.toString();
    assertNotNull(str);
    assertTrue(str.contains("user-id"));
    assertTrue(str.contains("target-id"));
  }
}
