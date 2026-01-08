package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.application.CursorPager.Direction;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void should_create_cursor_page_parameter_with_default_values() {
    CursorPageParameter<String> param = new CursorPageParameter<>();
    
    assertThat(param.getLimit(), is(20));
    assertThat(param.getCursor(), nullValue());
    assertThat(param.getDirection(), nullValue());
  }

  @Test
  public void should_create_cursor_page_parameter_with_custom_values() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor123", 50, Direction.NEXT);
    
    assertThat(param.getCursor(), is("cursor123"));
    assertThat(param.getLimit(), is(50));
    assertThat(param.getDirection(), is(Direction.NEXT));
  }

  @Test
  public void should_return_true_for_next_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 20, Direction.NEXT);
    
    assertThat(param.isNext(), is(true));
  }

  @Test
  public void should_return_false_for_prev_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 20, Direction.PREV);
    
    assertThat(param.isNext(), is(false));
  }

  @Test
  public void should_return_query_limit_plus_one() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 20, Direction.NEXT);
    
    assertThat(param.getQueryLimit(), is(21));
  }

  @Test
  public void should_cap_limit_at_max_value() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 2000, Direction.NEXT);
    
    assertThat(param.getLimit(), is(1000));
  }

  @Test
  public void should_not_set_negative_limit() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", -5, Direction.NEXT);
    
    assertThat(param.getLimit(), is(20));
  }

  @Test
  public void should_not_set_zero_limit() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 0, Direction.NEXT);
    
    assertThat(param.getLimit(), is(20));
  }

  @Test
  public void should_set_limit_at_max_boundary() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 1000, Direction.NEXT);
    
    assertThat(param.getLimit(), is(1000));
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    CursorPageParameter<String> param1 = new CursorPageParameter<>("cursor", 20, Direction.NEXT);
    CursorPageParameter<String> param2 = new CursorPageParameter<>("cursor", 20, Direction.NEXT);
    CursorPageParameter<String> param3 = new CursorPageParameter<>("cursor", 30, Direction.NEXT);
    
    assertThat(param1.equals(param2), is(true));
    assertThat(param1.equals(param3), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_all_fields() {
    CursorPageParameter<String> param1 = new CursorPageParameter<>("cursor", 20, Direction.NEXT);
    CursorPageParameter<String> param2 = new CursorPageParameter<>("cursor", 20, Direction.NEXT);
    
    assertThat(param1.hashCode(), is(param2.hashCode()));
  }

  @Test
  public void should_have_toString() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor123", 20, Direction.NEXT);
    String toString = param.toString();
    
    assertThat(toString.contains("cursor123"), is(true));
    assertThat(toString.contains("20"), is(true));
  }

  @Test
  public void should_return_false_for_null_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>();
    
    assertThat(param.isNext(), is(false));
  }
}
