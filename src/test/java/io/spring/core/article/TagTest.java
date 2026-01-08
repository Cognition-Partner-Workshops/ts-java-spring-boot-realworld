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
    
    assertThat(tag.getId(), notNullValue());
    assertThat(tag.getName(), is("java"));
  }

  @Test
  public void should_generate_unique_id_for_each_tag() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    
    assertThat(tag1.getId(), not(tag2.getId()));
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
  public void should_have_hashcode_based_on_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    Tag tag3 = new Tag("spring");
    
    assertThat(tag1.hashCode(), is(tag2.hashCode()));
    assertThat(tag1.hashCode(), not(tag3.hashCode()));
  }

  @Test
  public void should_create_tag_with_no_arg_constructor() {
    Tag tag = new Tag();
    assertThat(tag.getId(), is((String) null));
    assertThat(tag.getName(), is((String) null));
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
  public void should_set_name_via_setter() {
    Tag tag = new Tag();
    tag.setName("newname");
    assertThat(tag.getName(), is("newname"));
  }

  @Test
  public void should_set_id_via_setter() {
    Tag tag = new Tag();
    tag.setId("custom-id");
    assertThat(tag.getId(), is("custom-id"));
  }

  @Test
  public void should_have_toString() {
    Tag tag = new Tag("java");
    String toString = tag.toString();
    assertThat(toString.contains("java"), is(true));
  }
}
