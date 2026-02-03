package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user1", "user2");

    assertEquals("user1", relation.getUserId());
    assertEquals("user2", relation.getTargetId());
  }

  @Test
  public void should_be_equal_when_same_user_and_follow_ids() {
    FollowRelation relation1 = new FollowRelation("user1", "user2");
    FollowRelation relation2 = new FollowRelation("user1", "user2");

    assertEquals(relation1, relation2);
  }

  @Test
  public void should_not_be_equal_when_different_user_ids() {
    FollowRelation relation1 = new FollowRelation("user1", "user2");
    FollowRelation relation2 = new FollowRelation("user3", "user2");

    assertNotEquals(relation1, relation2);
  }

  @Test
  public void should_not_be_equal_when_different_follow_ids() {
    FollowRelation relation1 = new FollowRelation("user1", "user2");
    FollowRelation relation2 = new FollowRelation("user1", "user3");

    assertNotEquals(relation1, relation2);
  }

  @Test
  public void should_have_same_hashcode_when_equal() {
    FollowRelation relation1 = new FollowRelation("user1", "user2");
    FollowRelation relation2 = new FollowRelation("user1", "user2");

    assertEquals(relation1.hashCode(), relation2.hashCode());
  }
}
