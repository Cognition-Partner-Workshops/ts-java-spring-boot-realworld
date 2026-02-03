package io.spring.application.facade;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfileFacadeImpl implements ProfileFacade {

  private final ProfileQueryService profileQueryService;
  private final UserRepository userRepository;

  @Override
  public ProfileData getProfile(String username, User currentUser) {
    return profileQueryService
        .findByUsername(username, currentUser)
        .orElseThrow(ResourceNotFoundException::new);
  }

  @Override
  public ProfileData followUser(String username, User currentUser) {
    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

    FollowRelation followRelation = new FollowRelation(currentUser.getId(), target.getId());
    userRepository.saveRelation(followRelation);

    return profileQueryService
        .findByUsername(username, currentUser)
        .orElseThrow(() -> new RuntimeException("Profile not found after following"));
  }

  @Override
  public ProfileData unfollowUser(String username, User currentUser) {
    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

    userRepository
        .findRelation(currentUser.getId(), target.getId())
        .ifPresent(userRepository::removeRelation);

    return profileQueryService
        .findByUsername(username, currentUser)
        .orElseThrow(() -> new RuntimeException("Profile not found after unfollowing"));
  }
}
