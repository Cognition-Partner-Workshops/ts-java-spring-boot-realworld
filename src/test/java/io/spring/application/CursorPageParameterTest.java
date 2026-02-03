package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void should_create_with_default_values() {
    CursorPageParameter<String> param = new CursorPageParameter<>();

    assertThat(param.getLimit(), is(20));
    assertThat(param.getCursor(), is(nullValue()));
    assertThat(param.getDirection(), is(nullValue()));
  }

  @Test
  public void should_create_with_constructor_parameters() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor-value", 50, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(50));
    assertThat(param.getCursor(), is("cursor-value"));
    assertThat(param.getDirection(), is(CursorPager.Direction.NEXT));
  }

  @Test
  public void should_cap_limit_at_max_value() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 2000, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(1000));
  }

  @Test
  public void should_not_set_negative_limit() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", -5, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(20));
  }

  @Test
  public void should_not_set_zero_limit() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 0, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(20));
  }

  @Test
  public void should_return_query_limit_as_limit_plus_one() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 50, CursorPager.Direction.NEXT);

    assertThat(param.getQueryLimit(), is(51));
  }

  @Test
  public void should_return_is_next_true_for_next_direction() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 20, CursorPager.Direction.NEXT);

    assertThat(param.isNext(), is(true));
  }

  @Test
  public void should_return_is_next_false_for_prev_direction() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 20, CursorPager.Direction.PREV);

    assertThat(param.isNext(), is(false));
  }

  @Test
  public void should_return_is_next_false_for_null_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>();

    assertThat(param.isNext(), is(false));
  }

  @Test
  public void should_set_direction_via_setter() {
    CursorPageParameter<String> param = new CursorPageParameter<>();
    param.setDirection(CursorPager.Direction.PREV);

    assertThat(param.getDirection(), is(CursorPager.Direction.PREV));
  }

  @Test
  public void should_accept_valid_limit_values() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 100, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(100));
  }

  @Test
  public void should_accept_limit_at_max_boundary() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 1000, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(1000));
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    CursorPageParameter<String> param1 =
        new CursorPageParameter<>("cursor", 50, CursorPager.Direction.NEXT);
    CursorPageParameter<String> param2 =
        new CursorPageParameter<>("cursor", 50, CursorPager.Direction.NEXT);
    CursorPageParameter<String> param3 =
        new CursorPageParameter<>("different", 50, CursorPager.Direction.NEXT);

    assertThat(param1.equals(param2), is(true));
    assertThat(param1.equals(param3), is(false));
    assertThat(param1.hashCode(), is(param2.hashCode()));
  }

  @Test
  public void should_implement_to_string() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 50, CursorPager.Direction.NEXT);

    String toString = param.toString();

    assertThat(toString.contains("cursor"), is(true));
    assertThat(toString.contains("50"), is(true));
  }

  @Test
  public void should_handle_equals_with_null_and_different_type() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 50, CursorPager.Direction.NEXT);

    assertThat(param.equals(null), is(false));
    assertThat(param.equals("string"), is(false));
    assertThat(param.equals(param), is(true));
  }

  @Test
  public void should_handle_equals_with_different_limit() {
    CursorPageParameter<String> param1 =
        new CursorPageParameter<>("cursor", 50, CursorPager.Direction.NEXT);
    CursorPageParameter<String> param2 =
        new CursorPageParameter<>("cursor", 100, CursorPager.Direction.NEXT);

    assertThat(param1.equals(param2), is(false));
  }

  @Test
  public void should_handle_equals_with_different_direction() {
    CursorPageParameter<String> param1 =
        new CursorPageParameter<>("cursor", 50, CursorPager.Direction.NEXT);
    CursorPageParameter<String> param2 =
        new CursorPageParameter<>("cursor", 50, CursorPager.Direction.PREV);

    assertThat(param1.equals(param2), is(false));
  }

  @Test
  public void should_handle_equals_with_null_cursor() {
    CursorPageParameter<String> param1 =
        new CursorPageParameter<>(null, 50, CursorPager.Direction.NEXT);
    CursorPageParameter<String> param2 =
        new CursorPageParameter<>(null, 50, CursorPager.Direction.NEXT);

    assertThat(param1.equals(param2), is(true));
  }

  @Test
  public void should_handle_limit_just_above_max() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 1001, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(1000));
  }

  @Test
  public void should_handle_limit_of_one() {
    CursorPageParameter<String> param =
        new CursorPageParameter<>("cursor", 1, CursorPager.Direction.NEXT);

    assertThat(param.getLimit(), is(1));
    assertThat(param.getQueryLimit(), is(2));
  }
}
