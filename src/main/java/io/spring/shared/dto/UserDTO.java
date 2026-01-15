package io.spring.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared Data Transfer Object for User information. Used for inter-service communication between
 * microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

  private String id;
  private String username;
  private String email;
  private String bio;
  private String image;
}
