package io.spring.core.user;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a directional follow relationship between two users.
 *
 * <p>This entity captures the social following feature where one user (the follower) follows
 * another user (the target). The relationship is unidirectional, meaning user A following user B
 * does not imply user B follows user A.
 *
 * <p>Follow relationships enable the user feed feature, where users see articles from authors they
 * follow.
 *
 * @see User
 */
@NoArgsConstructor
@Data
public class FollowRelation {
  private String userId;
  private String targetId;

  public FollowRelation(String userId, String targetId) {

    this.userId = userId;
    this.targetId = targetId;
  }
}
