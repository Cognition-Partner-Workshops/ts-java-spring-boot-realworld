package io.spring.shared.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared Data Transfer Object for Article information. Used for inter-service communication
 * between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {

  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private String authorId;
  private List<String> tagList;
  private Instant createdAt;
  private Instant updatedAt;
  private int favoritesCount;
  private boolean favorited;
}
