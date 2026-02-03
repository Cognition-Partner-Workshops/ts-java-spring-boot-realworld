package io.spring.application.facade;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfileFacade {

  private ProfileQueryService profileQueryService;
  private UserRepository userRepository;

  public ProfileData getProfile(String username, User currentUser) {
    return profileQueryService
        .findByUsername(username, currentUser)
        .orElseThrow(ResourceNotFoundException::new);
  }

  public ProfileData follow(String username, User currentUser) {
    User target =
        userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    FollowRelation followRelation = new FollowRelation(currentUser.getId(), target.getId());
    userRepository.saveRelation(followRelation);
    return profileQueryService.findByUsername(username, currentUser).get();
  }

  public ProfileData unfollow(String username, User currentUser) {
    Optional<User> userOptional = userRepository.findByUsername(username);
    if (userOptional.isPresent()) {
      User target = userOptional.get();
      Optional<FollowRelation> relationOptional =
          userRepository.findRelation(currentUser.getId(), target.getId());
      if (relationOptional.isPresent()) {
        userRepository.removeRelation(relationOptional.get());
        return profileQueryService.findByUsername(username, currentUser).get();
      } else {
        throw new ResourceNotFoundException();
      }
    } else {
      throw new ResourceNotFoundException();
    }
  }
}
