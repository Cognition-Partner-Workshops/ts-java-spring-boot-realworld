package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_name() {
    String name = "java";

    Tag tag = new Tag(name);

    assertNotNull(tag.getId());
    assertEquals(name, tag.getName());
  }

  @Test
  public void should_generate_unique_id_for_each_tag() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");

    assertNotNull(tag1.getId());
    assertNotNull(tag2.getId());
    assertNotEquals(tag1.getId(), tag2.getId());
  }

  @Test
  public void should_have_equals_based_on_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    Tag tag3 = new Tag("spring");

    assertEquals(tag1, tag2);
    assertNotEquals(tag1, tag3);
  }

  @Test
  public void should_have_consistent_hashcode_based_on_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_have_different_hashcode_for_different_names() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");

    assertNotEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_allow_setting_name() {
    Tag tag = new Tag("java");
    tag.setName("spring");

    assertEquals("spring", tag.getName());
  }

  @Test
  public void should_allow_setting_id() {
    Tag tag = new Tag("java");
    String newId = "new-id-12345";
    tag.setId(newId);

    assertEquals(newId, tag.getId());
  }

  @Test
  public void should_create_tag_with_empty_name() {
    Tag tag = new Tag("");

    assertEquals("", tag.getName());
    assertNotNull(tag.getId());
  }

  @Test
  public void should_not_equal_null() {
    Tag tag = new Tag("java");

    assertNotEquals(null, tag);
  }

  @Test
  public void should_equal_itself() {
    Tag tag = new Tag("java");

    assertEquals(tag, tag);
  }

  @Test
  public void should_have_to_string() {
    Tag tag = new Tag("java");

    String toString = tag.toString();
    assertNotNull(toString);
    assertTrue(toString.contains("java"));
  }
}
