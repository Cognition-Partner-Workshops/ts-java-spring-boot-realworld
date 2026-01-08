package io.spring.application;

import io.spring.infrastructure.mybatis.readservice.TagReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TagsQueryService {
  private TagReadService tagReadService;

  public List<String> allTags() {
    log.info("Entering allTags()");
    List<String> tags = tagReadService.all();
    log.info("Exiting allTags()");
    return tags;
  }
}
