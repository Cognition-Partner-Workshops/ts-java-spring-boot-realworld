package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagDatafetcherTest {

  @Mock private TagsQueryService tagsQueryService;

  private TagDatafetcher tagDatafetcher;

  @BeforeEach
  void setUp() {
    tagDatafetcher = new TagDatafetcher(tagsQueryService);
  }

  @Test
  void getTags_success() {
    List<String> expectedTags = Arrays.asList("java", "spring", "graphql");
    when(tagsQueryService.allTags()).thenReturn(expectedTags);

    List<String> result = tagDatafetcher.getTags();

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.contains("java"));
    assertTrue(result.contains("spring"));
    assertTrue(result.contains("graphql"));
  }

  @Test
  void getTags_emptyList() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    List<String> result = tagDatafetcher.getTags();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
