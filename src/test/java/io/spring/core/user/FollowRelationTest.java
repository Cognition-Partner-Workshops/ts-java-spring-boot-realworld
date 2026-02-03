package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user-1", "user-2");
    
    assertThat(relation.getUserId(), is("user-1"));
    assertThat(relation.getTargetId(), is("user-2"));
  }

  @Test
  public void should_have_equals_based_on_user_and_target() {
    FollowRelation relation1 = new FollowRelation("user-1", "user-2");
    FollowRelation relation2 = new FollowRelation("user-1", "user-2");
    FollowRelation relation3 = new FollowRelation("user-1", "user-3");
    
    assertThat(relation1.equals(relation2), is(true));
    assertThat(relation1.equals(relation3), is(false));
  }
}
