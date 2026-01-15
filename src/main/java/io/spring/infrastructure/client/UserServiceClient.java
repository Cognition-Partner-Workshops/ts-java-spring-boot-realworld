package io.spring.infrastructure.client;

import io.spring.shared.dto.ProfileDTO;
import io.spring.shared.dto.UserDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for communicating with the User Service. This client will be used when the
 * application is decomposed into microservices to fetch user information from the User Service.
 *
 * <p>The fallback class provides resilient behavior when the User Service is unavailable.
 */
@FeignClient(
    name = "user-service",
    url = "${user-service.url:http://localhost:8081}",
    fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

  @GetMapping("/api/users/{id}")
  Optional<UserDTO> getUserById(@PathVariable("id") String id);

  @GetMapping("/api/users/username/{username}")
  Optional<UserDTO> getUserByUsername(@PathVariable("username") String username);

  @GetMapping("/api/users/email/{email}")
  Optional<UserDTO> getUserByEmail(@PathVariable("email") String email);

  @GetMapping("/api/profiles/{username}")
  Optional<ProfileDTO> getProfile(
      @PathVariable("username") String username, @RequestParam("currentUserId") String currentUserId);

  @GetMapping("/api/users/{userId}/following")
  List<String> getFollowingIds(@PathVariable("userId") String userId);

  @GetMapping("/api/users/{userId}/followers")
  List<String> getFollowerIds(@PathVariable("userId") String userId);

  @GetMapping("/api/users/{userId}/is-following/{targetUserId}")
  boolean isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetUserId") String targetUserId);
}
