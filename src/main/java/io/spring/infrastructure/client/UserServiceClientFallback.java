package io.spring.infrastructure.client;

import io.spring.shared.dto.ProfileDTO;
import io.spring.shared.dto.UserDTO;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for UserServiceClient. This class provides default responses when the
 * User Service is unavailable, ensuring graceful degradation of the application.
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceClientFallback.class);

  @Override
  public Optional<UserDTO> getUserById(String id) {
    logger.warn("User Service unavailable. Fallback triggered for getUserById: {}", id);
    return Optional.empty();
  }

  @Override
  public Optional<UserDTO> getUserByUsername(String username) {
    logger.warn("User Service unavailable. Fallback triggered for getUserByUsername: {}", username);
    return Optional.empty();
  }

  @Override
  public Optional<UserDTO> getUserByEmail(String email) {
    logger.warn("User Service unavailable. Fallback triggered for getUserByEmail: {}", email);
    return Optional.empty();
  }

  @Override
  public Optional<ProfileDTO> getProfile(String username, String currentUserId) {
    logger.warn("User Service unavailable. Fallback triggered for getProfile: {}", username);
    return Optional.empty();
  }

  @Override
  public List<String> getFollowingIds(String userId) {
    logger.warn("User Service unavailable. Fallback triggered for getFollowingIds: {}", userId);
    return Collections.emptyList();
  }

  @Override
  public List<String> getFollowerIds(String userId) {
    logger.warn("User Service unavailable. Fallback triggered for getFollowerIds: {}", userId);
    return Collections.emptyList();
  }

  @Override
  public boolean isFollowing(String userId, String targetUserId) {
    logger.warn(
        "User Service unavailable. Fallback triggered for isFollowing: {} -> {}",
        userId,
        targetUserId);
    return false;
  }
}
