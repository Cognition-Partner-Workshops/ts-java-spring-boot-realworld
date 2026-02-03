package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
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

  @Test
  public void should_create_with_no_args_constructor() {
    FollowRelation relation = new FollowRelation();
    
    assertThat(relation, is(notNullValue()));
  }

  @Test
  public void should_set_user_id() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user-1");
    
    assertThat(relation.getUserId(), is("user-1"));
  }

  @Test
  public void should_set_target_id() {
    FollowRelation relation = new FollowRelation();
    relation.setTargetId("user-2");
    
    assertThat(relation.getTargetId(), is("user-2"));
  }

  @Test
  public void should_implement_hashcode() {
    FollowRelation relation1 = new FollowRelation("user-1", "user-2");
    FollowRelation relation2 = new FollowRelation("user-1", "user-2");
    FollowRelation relation3 = new FollowRelation("user-1", "user-3");
    
    assertThat(relation1.hashCode(), is(relation2.hashCode()));
    assertThat(relation1.hashCode(), is(not(relation3.hashCode())));
  }

  @Test
  public void should_implement_to_string() {
    FollowRelation relation = new FollowRelation("user-1", "user-2");
    
    String toString = relation.toString();
    
    assertThat(toString, is(notNullValue()));
    assertThat(toString.contains("user-1"), is(true));
    assertThat(toString.contains("user-2"), is(true));
  }

  @Test
  public void should_handle_equals_with_null_and_different_type() {
    FollowRelation relation = new FollowRelation("user-1", "user-2");
    
    assertThat(relation.equals(null), is(false));
    assertThat(relation.equals("string"), is(false));
    assertThat(relation.equals(relation), is(true));
  }

  @Test
  public void should_handle_different_user_id() {
    FollowRelation relation1 = new FollowRelation("user-1", "user-2");
    FollowRelation relation2 = new FollowRelation("user-3", "user-2");
    
    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_handle_null_user_id() {
    FollowRelation relation1 = new FollowRelation(null, "user-2");
    FollowRelation relation2 = new FollowRelation(null, "user-2");
    
    assertThat(relation1.equals(relation2), is(true));
  }

  @Test
  public void should_handle_null_target_id() {
    FollowRelation relation1 = new FollowRelation("user-1", null);
    FollowRelation relation2 = new FollowRelation("user-1", null);
    
    assertThat(relation1.equals(relation2), is(true));
  }

  @Test
  public void should_not_equal_when_one_user_id_null() {
    FollowRelation relation1 = new FollowRelation("user-1", "user-2");
    FollowRelation relation2 = new FollowRelation(null, "user-2");
    
    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_not_equal_when_one_target_id_null() {
    FollowRelation relation1 = new FollowRelation("user-1", "user-2");
    FollowRelation relation2 = new FollowRelation("user-1", null);
    
    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_not_equal_when_other_user_id_null() {
    FollowRelation relation1 = new FollowRelation(null, "user-2");
    FollowRelation relation2 = new FollowRelation("user-1", "user-2");
    
    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_not_equal_when_other_target_id_null() {
    FollowRelation relation1 = new FollowRelation("user-1", null);
    FollowRelation relation2 = new FollowRelation("user-1", "user-2");
    
    assertThat(relation1.equals(relation2), is(false));
  }

  @Test
  public void should_handle_both_fields_null() {
    FollowRelation relation1 = new FollowRelation(null, null);
    FollowRelation relation2 = new FollowRelation(null, null);
    
    assertThat(relation1.equals(relation2), is(true));
  }
}
