package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UpdateArticleParamTest {

  @Test
  void allArgsConstructor_createsWithAllFields() {
    UpdateArticleParam param = new UpdateArticleParam("Title", "Body", "Description");

    assertEquals("Title", param.getTitle());
    assertEquals("Body", param.getBody());
    assertEquals("Description", param.getDescription());
  }

  @Test
  void noArgsConstructor_createsWithEmptyStrings() {
    UpdateArticleParam param = new UpdateArticleParam();

    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }
}
