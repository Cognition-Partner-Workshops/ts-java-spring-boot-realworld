package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

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

  @Mock
  private TagReadService tagReadService;

  private TagsQueryService tagsQueryService;

  @BeforeEach
  public void setUp() {
    tagsQueryService = new TagsQueryService(tagReadService);
  }

  @Test
  public void should_return_all_tags() {
    List<String> tags = Arrays.asList("java", "spring", "kotlin");
    when(tagReadService.all()).thenReturn(tags);

    List<String> result = tagsQueryService.allTags();

    assertThat(result.size(), is(3));
    assertThat(result.contains("java"), is(true));
    assertThat(result.contains("spring"), is(true));
    assertThat(result.contains("kotlin"), is(true));
  }

  @Test
  public void should_return_empty_list_when_no_tags() {
    when(tagReadService.all()).thenReturn(Collections.emptyList());

    List<String> result = tagsQueryService.allTags();

    assertThat(result.isEmpty(), is(true));
  }
}
