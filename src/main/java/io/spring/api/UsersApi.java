package io.spring.api;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.adapter.RestToGraphQLAdapter;
import io.spring.application.user.RegisterParam;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UsersApi {
  private RestToGraphQLAdapter restToGraphQLAdapter;

  @RequestMapping(path = "/users", method = POST)
  public ResponseEntity<Map<String, Object>> createUser(
      @Valid @RequestBody RegisterParam registerParam) {
    Map<String, Object> response = restToGraphQLAdapter.createUser(registerParam);
    return ResponseEntity.status(201).body(response);
  }

  @RequestMapping(path = "/users/login", method = POST)
  public ResponseEntity<Map<String, Object>> userLogin(@Valid @RequestBody LoginParam loginParam) {
    Map<String, Object> response =
        restToGraphQLAdapter.login(loginParam.getEmail(), loginParam.getPassword());
    return ResponseEntity.ok(response);
  }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class LoginParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  private String email;

  @NotBlank(message = "can't be empty")
  private String password;
}
