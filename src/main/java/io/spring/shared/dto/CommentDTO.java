package io.spring.shared.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared Data Transfer Object for Comment information. Used for inter-service communication
 * between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

  private String id;
  private String body;
  private String articleId;
  private String authorId;
  private Instant createdAt;
  private Instant updatedAt;
}
