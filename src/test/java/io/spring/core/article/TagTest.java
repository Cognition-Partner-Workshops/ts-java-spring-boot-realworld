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
  public void should_have_unique_ids() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertNotEquals(tag1.getId(), tag2.getId());
  }

  @Test
  public void should_be_equal_when_same_id() {
    Tag tag1 = new Tag("java");
    Tag tag2 = tag1;

    assertEquals(tag1, tag2);
  }

  @Test
  public void should_be_equal_when_same_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1, tag2);
  }

  @Test
  public void should_not_be_equal_when_different_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");

    assertNotEquals(tag1, tag2);
  }

  @Test
  public void should_have_same_hashcode_when_equal() {
    Tag tag = new Tag("java");

    assertEquals(tag.hashCode(), tag.hashCode());
  }
}
