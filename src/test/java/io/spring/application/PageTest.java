package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  public void should_create_with_default_values() {
    Page page = new Page();

    assertThat(page.getOffset(), is(0));
    assertThat(page.getLimit(), is(20));
  }

  @Test
  public void should_create_with_constructor_parameters() {
    Page page = new Page(10, 50);

    assertThat(page.getOffset(), is(10));
    assertThat(page.getLimit(), is(50));
  }

  @Test
  public void should_cap_limit_at_max_value() {
    Page page = new Page(0, 200);

    assertThat(page.getLimit(), is(100));
  }

  @Test
  public void should_not_set_negative_offset() {
    Page page = new Page(-5, 20);

    assertThat(page.getOffset(), is(0));
  }

  @Test
  public void should_not_set_negative_limit() {
    Page page = new Page(0, -5);

    assertThat(page.getLimit(), is(20));
  }

  @Test
  public void should_not_set_zero_limit() {
    Page page = new Page(0, 0);

    assertThat(page.getLimit(), is(20));
  }

  @Test
  public void should_accept_valid_offset_values() {
    Page page = new Page(100, 20);

    assertThat(page.getOffset(), is(100));
  }

  @Test
  public void should_accept_limit_at_max_boundary() {
    Page page = new Page(0, 100);

    assertThat(page.getLimit(), is(100));
  }

  @Test
  public void should_accept_limit_below_max() {
    Page page = new Page(0, 99);

    assertThat(page.getLimit(), is(99));
  }

  @Test
  public void should_accept_zero_offset() {
    Page page = new Page(0, 20);

    assertThat(page.getOffset(), is(0));
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    Page page1 = new Page(10, 50);
    Page page2 = new Page(10, 50);
    Page page3 = new Page(20, 50);

    assertThat(page1.equals(page2), is(true));
    assertThat(page1.equals(page3), is(false));
    assertThat(page1.hashCode(), is(page2.hashCode()));
    assertThat(page1.hashCode(), is(not(page3.hashCode())));
  }

  @Test
  public void should_implement_to_string() {
    Page page = new Page(10, 50);

    String toString = page.toString();

    assertThat(toString, is(notNullValue()));
    assertThat(toString.contains("10"), is(true));
    assertThat(toString.contains("50"), is(true));
  }

  @Test
  public void should_handle_equals_with_null_and_different_type() {
    Page page = new Page(10, 50);

    assertThat(page.equals(null), is(false));
    assertThat(page.equals("string"), is(false));
    assertThat(page.equals(page), is(true));
  }

  @Test
  public void should_handle_different_limit() {
    Page page1 = new Page(10, 50);
    Page page2 = new Page(10, 60);

    assertThat(page1.equals(page2), is(false));
  }

  @Test
  public void should_handle_limit_at_one() {
    Page page = new Page(0, 1);

    assertThat(page.getLimit(), is(1));
  }
}
