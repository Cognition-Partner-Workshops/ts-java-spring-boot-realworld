package io.spring.api;

import io.spring.api.adapter.RestToGraphQLAdapter;
import io.spring.application.user.UpdateUserParam;
import io.spring.core.user.User;
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

  private RestToGraphQLAdapter restToGraphQLAdapter;

  @GetMapping
  public ResponseEntity<Map<String, Object>> currentUser(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader(value = "Authorization") String authorization) {
    String token = authorization.split(" ")[1];
    Map<String, Object> response = restToGraphQLAdapter.getCurrentUser(currentUser, token);
    return ResponseEntity.ok(response);
  }

  @PutMapping
  public ResponseEntity<Map<String, Object>> updateProfile(
      @AuthenticationPrincipal User currentUser,
      @RequestHeader("Authorization") String authorization,
      @Valid @RequestBody UpdateUserParam updateUserParam) {
    String token = authorization.split(" ")[1];
    Map<String, Object> response =
        restToGraphQLAdapter.updateUser(currentUser, updateUserParam, token);
    return ResponseEntity.ok(response);
  }
}
