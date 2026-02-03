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
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserFacade {

  private UserRepository userRepository;
  private UserQueryService userQueryService;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private UserService userService;

  @Getter
  @AllArgsConstructor
  public static class UserResult {
    private final UserWithToken userWithToken;
    private final User user;
  }

  public UserResult createUserWithEntity(RegisterParam registerParam) {
    User user = userService.createUser(registerParam);
    UserData userData = userQueryService.findById(user.getId()).get();
    UserWithToken userWithToken = new UserWithToken(userData, jwtService.toToken(user));
    return new UserResult(userWithToken, user);
  }

  public UserWithToken createUser(RegisterParam registerParam) {
    return createUserWithEntity(registerParam).getUserWithToken();
  }

  public UserResult loginWithEntity(String email, String password) {
    Optional<User> optional = userRepository.findByEmail(email);
    if (optional.isPresent()
        && passwordEncoder.matches(password, optional.get().getPassword())) {
      User user = optional.get();
      UserData userData = userQueryService.findById(user.getId()).get();
      UserWithToken userWithToken = new UserWithToken(userData, jwtService.toToken(user));
      return new UserResult(userWithToken, user);
    } else {
      throw new InvalidAuthenticationException();
    }
  }

  public UserWithToken login(String email, String password) {
    return loginWithEntity(email, password).getUserWithToken();
  }

  public UserWithToken getCurrentUser(User currentUser, String token) {
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return new UserWithToken(userData, token);
  }

  public UserWithToken updateUser(User currentUser, UpdateUserParam updateUserParam, String token) {
    userService.updateUser(new UpdateUserCommand(currentUser, updateUserParam));
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return new UserWithToken(userData, token);
  }

  public String generateToken(User user) {
    return jwtService.toToken(user);
  }
}
