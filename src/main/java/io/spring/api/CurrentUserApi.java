package io.spring.api;

import io.spring.application.data.UserWithToken;
import io.spring.application.facade.UserApiFacade;
import io.spring.application.user.UpdateUserParam;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class CurrentUserApi {

  private UserApiFacade userApiFacade;

  @GetMapping
  public ResponseEntity currentUser(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader(value = "Authorization") String authorization) {
    UserWithToken userWithToken =
        userApiFacade.getCurrentUser(currentUser, authorization.split(" ")[1]);
    return ResponseEntity.ok(userResponse(userWithToken));
  }

  @PutMapping
  public ResponseEntity updateProfile(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader("Authorization") String token,
      @Valid @RequestBody UpdateUserParam updateUserParam) {
    UserWithToken userWithToken =
        userApiFacade.updateUser(
            currentUser,
            updateUserParam.getEmail(),
            updateUserParam.getUsername(),
            updateUserParam.getPassword(),
            updateUserParam.getBio(),
            updateUserParam.getImage(),
            token.split(" ")[1]);
    return ResponseEntity.ok(userResponse(userWithToken));
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }
}
