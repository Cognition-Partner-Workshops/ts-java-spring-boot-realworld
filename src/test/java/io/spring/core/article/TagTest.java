package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_name() {
    Tag tag = new Tag("java");

    assertNotNull(tag.getId());
    assertEquals("java", tag.getName());
  }

  @Test
  public void should_generate_unique_id_for_each_tag() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");

    assertNotEquals(tag1.getId(), tag2.getId());
  }

  @Test
  public void should_have_equality_based_on_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    Tag tag3 = new Tag("spring");

    assertEquals(tag1, tag2);
    assertNotEquals(tag1, tag3);
  }

  @Test
  public void should_have_consistent_hash_code_based_on_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_set_name() {
    Tag tag = new Tag("java");
    tag.setName("spring");

    assertEquals("spring", tag.getName());
  }

  @Test
  public void should_set_id() {
    Tag tag = new Tag("java");
    String originalId = tag.getId();
    tag.setId("custom-id");

    assertEquals("custom-id", tag.getId());
    assertNotEquals(originalId, tag.getId());
  }

  @Test
  public void should_create_tag_with_no_args_constructor() {
    Tag tag = new Tag();

    assertNull(tag.getId());
    assertNull(tag.getName());
  }

  @Test
  public void should_create_tag_with_empty_name() {
    Tag tag = new Tag("");

    assertNotNull(tag.getId());
    assertEquals("", tag.getName());
  }

  @Test
  public void should_have_to_string_representation() {
    Tag tag = new Tag("java");

    String toString = tag.toString();
    assertTrue(toString.contains("java"));
  }
}
