package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagsQueryServiceTest {

  @Mock private TagReadService tagReadService;

  private TagsQueryService tagsQueryService;

  @BeforeEach
  public void setUp() {
    tagsQueryService = new TagsQueryService(tagReadService);
  }

  @Test
  public void should_return_all_tags() {
    List<String> expectedTags = Arrays.asList("java", "spring", "kotlin");
    when(tagReadService.all()).thenReturn(expectedTags);

    List<String> result = tagsQueryService.allTags();

    assertEquals(expectedTags, result);
    assertEquals(3, result.size());
    assertTrue(result.contains("java"));
    assertTrue(result.contains("spring"));
    assertTrue(result.contains("kotlin"));
    verify(tagReadService).all();
  }

  @Test
  public void should_return_empty_list_when_no_tags() {
    when(tagReadService.all()).thenReturn(Collections.emptyList());

    List<String> result = tagsQueryService.allTags();

    assertTrue(result.isEmpty());
    verify(tagReadService).all();
  }

  @Test
  public void should_return_single_tag() {
    List<String> expectedTags = Arrays.asList("java");
    when(tagReadService.all()).thenReturn(expectedTags);

    List<String> result = tagsQueryService.allTags();

    assertEquals(1, result.size());
    assertEquals("java", result.get(0));
  }
}
