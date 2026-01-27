package io.spring.api;

import io.spring.api.adapter.RestToGraphQLAdapter;
import io.spring.core.user.User;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "profiles/{username}")
@AllArgsConstructor
public class ProfileApi {
  private RestToGraphQLAdapter restToGraphQLAdapter;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    Map<String, Object> response = restToGraphQLAdapter.getProfile(username, user);
    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "follow")
  public ResponseEntity<Map<String, Object>> follow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    Map<String, Object> response = restToGraphQLAdapter.followUser(username);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity<Map<String, Object>> unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    Map<String, Object> response = restToGraphQLAdapter.unfollowUser(username);
    return ResponseEntity.ok(response);
  }
}
