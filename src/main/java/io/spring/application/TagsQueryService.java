package io.spring.application;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service for tag read operations.
 *
 * <p>This service provides access to all available tags in the system. Tags are used to categorize
 * articles and enable filtering.
 *
 * @see io.spring.core.article.Tag
 */
@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagReadService tagReadService;

  public List<String> allTags() {
    return tagReadService.all();
  }
}
