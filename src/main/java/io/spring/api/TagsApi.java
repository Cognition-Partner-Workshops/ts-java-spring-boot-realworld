package io.spring.api;

import io.spring.graphql.TagDatafetcher;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagDatafetcher tagDatafetcher;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getTags() {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("tags", tagDatafetcher.getTags());
          }
        });
  }
}
