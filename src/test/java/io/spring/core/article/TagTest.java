package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_name() {
    Tag tag = new Tag("java");
    assertThat(tag.getName(), is("java"));
    assertThat(tag.getId(), is(notNullValue()));
  }

  @Test
  public void should_have_unique_id() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertThat(tag1.getId().equals(tag2.getId()), is(false));
  }

  @Test
  public void should_have_equals_based_on_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    Tag tag3 = new Tag("spring");
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.equals(tag3), is(false));
  }

  @Test
  public void should_have_same_hashcode_for_same_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertThat(tag1.hashCode(), is(tag2.hashCode()));
  }

  @Test
  public void should_have_different_hashcode_for_different_names() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertThat(tag1.hashCode(), is(not(tag2.hashCode())));
  }

  @Test
  public void should_not_equal_null() {
    Tag tag = new Tag("java");
    assertThat(tag.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    Tag tag = new Tag("java");
    assertThat(tag.equals("java"), is(false));
  }

  @Test
  public void should_equal_itself() {
    Tag tag = new Tag("java");
    assertThat(tag.equals(tag), is(true));
  }

  @Test
  public void should_create_with_no_args_constructor() {
    Tag tag = new Tag();
    assertThat(tag, is(notNullValue()));
  }

  @Test
  public void should_set_name_via_setter() {
    Tag tag = new Tag();
    tag.setName("java");
    assertThat(tag.getName(), is("java"));
  }

  @Test
  public void should_set_id_via_setter() {
    Tag tag = new Tag();
    tag.setId("custom-id");
    assertThat(tag.getId(), is("custom-id"));
  }

  @Test
  public void should_have_toString_method() {
    Tag tag = new Tag("java");
    String toString = tag.toString();
    assertThat(toString, is(notNullValue()));
    assertThat(toString.contains("java"), is(true));
  }

  @Test
  public void should_equal_when_both_names_null() {
    Tag tag1 = new Tag();
    tag1.setName(null);
    Tag tag2 = new Tag();
    tag2.setName(null);
    assertThat(tag1.equals(tag2), is(true));
  }

  @Test
  public void should_not_equal_when_one_name_null() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag();
    tag2.setName(null);
    assertThat(tag1.equals(tag2), is(false));
  }

  @Test
  public void should_not_equal_when_other_name_null() {
    Tag tag1 = new Tag();
    tag1.setName(null);
    Tag tag2 = new Tag("java");
    assertThat(tag1.equals(tag2), is(false));
  }
}
