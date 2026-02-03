package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewArticleParamTest {

  @Test
  void builder_createsWithAllFields() {
    List<String> tags = Arrays.asList("java", "spring");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(tags)
            .build();

    assertEquals("Test Title", param.getTitle());
    assertEquals("Test Description", param.getDescription());
    assertEquals("Test Body", param.getBody());
    assertEquals(tags, param.getTagList());
  }

  @Test
  void noArgsConstructor_createsEmptyObject() {
    NewArticleParam param = new NewArticleParam();

    assertNull(param.getTitle());
    assertNull(param.getDescription());
    assertNull(param.getBody());
    assertNull(param.getTagList());
  }

  @Test
  void allArgsConstructor_createsWithAllFields() {
    List<String> tags = Arrays.asList("java", "spring");
    NewArticleParam param = new NewArticleParam("Title", "Description", "Body", tags);

    assertEquals("Title", param.getTitle());
    assertEquals("Description", param.getDescription());
    assertEquals("Body", param.getBody());
    assertEquals(tags, param.getTagList());
  }

  @Test
  void builder_withNullTagList() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(null)
            .build();

    assertNull(param.getTagList());
  }

  @Test
  void builder_withEmptyTagList() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList())
            .build();

    assertTrue(param.getTagList().isEmpty());
  }
}
