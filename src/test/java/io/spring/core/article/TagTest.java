package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TagTest {

  @Test
  void constructor_createsWithName() {
    Tag tag = new Tag("java");

    assertNotNull(tag.getId());
    assertEquals("java", tag.getName());
  }

  @Test
  void noArgsConstructor_createsEmptyTag() {
    Tag tag = new Tag();

    assertNull(tag.getId());
    assertNull(tag.getName());
  }

  @Test
  void setters_updateFields() {
    Tag tag = new Tag();

    tag.setId("tag-id");
    tag.setName("spring");

    assertEquals("tag-id", tag.getId());
    assertEquals("spring", tag.getName());
  }

  @Test
  void equals_basedOnName() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1, tag2);
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  void equals_differentNames() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");

    assertNotEquals(tag1, tag2);
  }

  @Test
  void equals_withNull() {
    Tag tag = new Tag("java");

    assertNotEquals(tag, null);
  }

  @Test
  void equals_withDifferentClass() {
    Tag tag = new Tag("java");

    assertNotEquals(tag, "java");
  }

  @Test
  void equals_withSameObject() {
    Tag tag = new Tag("java");

    assertEquals(tag, tag);
  }
}
