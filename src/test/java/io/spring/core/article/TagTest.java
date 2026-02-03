package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  void constructor_withName() {
    Tag tag = new Tag("java");

    assertNotNull(tag.getId());
    assertEquals("java", tag.getName());
  }

  @Test
  void noArgsConstructor() {
    Tag tag = new Tag();

    assertNull(tag.getId());
    assertNull(tag.getName());
  }

  @Test
  void setters() {
    Tag tag = new Tag();
    tag.setId("custom-id");
    tag.setName("spring");

    assertEquals("custom-id", tag.getId());
    assertEquals("spring", tag.getName());
  }

  @Test
  void equals_sameName() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1, tag2);
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  void notEquals_differentName() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");

    assertNotEquals(tag1, tag2);
  }

  @Test
  void toString_notNull() {
    Tag tag = new Tag("java");
    assertNotNull(tag.toString());
    assertTrue(tag.toString().contains("java"));
  }
}
