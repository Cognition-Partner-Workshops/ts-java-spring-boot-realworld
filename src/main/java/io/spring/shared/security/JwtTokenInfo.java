package io.spring.shared.security;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the information extracted from a JWT token. This class is used for inter-service
 * communication to pass user authentication information without requiring database lookups.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenInfo {

  private String userId;
  private Date issuedAt;
  private Date expiration;
  private boolean valid;

  public boolean isExpired() {
    return expiration != null && expiration.before(new Date());
  }
}
