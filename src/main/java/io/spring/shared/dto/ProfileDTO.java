package io.spring.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared Data Transfer Object for Profile information. Used for inter-service communication
 * between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
