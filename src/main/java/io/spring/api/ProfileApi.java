package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "profiles/{username}")
@AllArgsConstructor
public class ProfileApi {
  private ProfileQueryService profileQueryService;
  private UserRepository userRepository;

  @GetMapping
  public ResponseEntity getProfile(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    log.info(
        "Entering getProfile with parameters: username={}, userId={}",
        username,
        user != null ? user.getId() : "anonymous");
    ResponseEntity response =
        profileQueryService
            .findByUsername(username, user)
            .map(this::profileResponse)
            .orElseThrow(ResourceNotFoundException::new);
    log.info("Exiting getProfile with status: {}", response.getStatusCode());
    return response;
  }

  @PostMapping(path = "follow")
  public ResponseEntity follow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    log.info(
        "Entering follow with parameters: username={}, userId={}",
        username,
        user != null ? user.getId() : "anonymous");
    ResponseEntity response =
        userRepository
            .findByUsername(username)
            .map(
                target -> {
                  FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
                  userRepository.saveRelation(followRelation);
                  return profileResponse(profileQueryService.findByUsername(username, user).get());
                })
            .orElseThrow(ResourceNotFoundException::new);
    log.info("Exiting follow with status: {}", response.getStatusCode());
    return response;
  }

  @DeleteMapping(path = "follow")
  public ResponseEntity unfollow(
      @PathVariable("username") String username, @AuthenticationPrincipal User user) {
    log.info(
        "Entering unfollow with parameters: username={}, userId={}",
        username,
        user != null ? user.getId() : "anonymous");
    Optional<User> userOptional = userRepository.findByUsername(username);
    if (userOptional.isPresent()) {
      User target = userOptional.get();
      ResponseEntity response =
          userRepository
              .findRelation(user.getId(), target.getId())
              .map(
                  relation -> {
                    userRepository.removeRelation(relation);
                    return profileResponse(profileQueryService.findByUsername(username, user).get());
                  })
              .orElseThrow(ResourceNotFoundException::new);
      log.info("Exiting unfollow with status: {}", response.getStatusCode());
      return response;
    } else {
      log.info("Exiting unfollow with resource not found");
      throw new ResourceNotFoundException();
    }
  }

  private ResponseEntity profileResponse(ProfileData profile) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("profile", profile);
          }
        });
  }
}
