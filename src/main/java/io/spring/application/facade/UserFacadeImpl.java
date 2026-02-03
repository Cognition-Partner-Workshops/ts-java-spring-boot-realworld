package io.spring.application.facade;

import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserFacadeImpl implements UserFacade {

  private final UserRepository userRepository;
  private final UserQueryService userQueryService;
  private final UserService userService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserWithToken registerUser(RegisterParam registerParam) {
    User user = userService.createUser(registerParam);
    UserData userData =
        userQueryService
            .findById(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found after creation"));
    return new UserWithToken(userData, jwtService.toToken(user));
  }

  @Override
  public UserWithToken login(String email, String password) {
    User user =
        userRepository
            .findByEmail(email)
            .filter(u -> passwordEncoder.matches(password, u.getPassword()))
            .orElseThrow(InvalidAuthenticationException::new);

    UserData userData =
        userQueryService
            .findById(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
    return new UserWithToken(userData, jwtService.toToken(user));
  }

  @Override
  public UserWithToken getCurrentUser(User currentUser, String token) {
    UserData userData =
        userQueryService
            .findById(currentUser.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
    return new UserWithToken(userData, token);
  }

  @Override
  public UserWithToken updateUser(User currentUser, UpdateUserParam updateUserParam, String token) {
    userService.updateUser(new UpdateUserCommand(currentUser, updateUserParam));
    UserData userData =
        userQueryService
            .findById(currentUser.getId())
            .orElseThrow(() -> new RuntimeException("User not found after update"));
    return new UserWithToken(userData, token);
  }

  @Override
  public UserData findById(String userId) {
    return userQueryService.findById(userId).orElse(null);
  }
}
