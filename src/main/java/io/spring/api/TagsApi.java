package io.spring.api;

import io.spring.application.TagsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
@Tag(name = "Tags", description = "Tag endpoints")
public class TagsApi {
  private TagsQueryService tagsQueryService;

  @Operation(summary = "Get tags", description = "Get all tags used in articles")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Tags retrieved successfully")})
  @GetMapping
  public ResponseEntity getTags() {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("tags", tagsQueryService.allTags());
          }
        });
  }
}
