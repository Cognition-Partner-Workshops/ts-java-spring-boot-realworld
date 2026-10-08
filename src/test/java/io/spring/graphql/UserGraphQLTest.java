package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import graphql.ExecutionResult;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

class UserGraphQLTest extends GraphQLTestBase {

  private static final String CREATE_USER =
      "mutation($input: CreateUserInput) { createUser(input: $input) {"
          + " __typename"
          + " ... on UserPayload { user { email username token profile { username bio image following } } }"
          + " ... on Error { message errors { key value } } } }";

  private static final String LOGIN =
      "mutation($email: String!, $password: String!) { login(email: $email, password: $password) {"
          + " user { email username token profile { username } } } }";

  private static final String UPDATE_USER =
      "mutation($changes: UpdateUserInput!) { updateUser(changes: $changes) {"
          + " user { email username token profile { username bio image } } } }";

  private static final String PROFILE =
      "query($username: String!) { profile(username: $username) {"
          + " profile { username bio image following } } }";

  private static final String FOLLOW =
      "mutation($username: String!) { followUser(username: $username) { profile { username following } } }";

  private static final String UNFOLLOW =
      "mutation($username: String!) { unfollowUser(username: $username) { profile { username following } } }";

  @Value("${image.default}")
  private String defaultImage;

  @Test
  void create_user_returns_user_payload_with_token_and_default_profile() {
    String username = unique("newbie");
    Map<String, Object> payload =
        data(
            execute(
                CREATE_USER,
                vars(
                    "input",
                    Map.of(
                        "email", username + "@example.com",
                        "username", username,
                        "password", PASSWORD))),
            "createUser");

    assertThat(payload.get("__typename")).isEqualTo("UserPayload");
    Map<String, Object> user = (Map<String, Object>) payload.get("user");
    assertThat(user.get("username")).isEqualTo(username);
    assertThat(user.get("email")).isEqualTo(username + "@example.com");
    assertThat((String) user.get("token")).isNotBlank();

    User saved = userRepository.findByUsername(username).orElseThrow();
    assertThat(jwtService.getSubFromToken((String) user.get("token"))).contains(saved.getId());

    Map<String, Object> profile = (Map<String, Object>) user.get("profile");
    assertThat(profile.get("username")).isEqualTo(username);
    assertThat(profile.get("bio")).isEqualTo("");
    assertThat(profile.get("image")).isEqualTo(defaultImage);
    assertThat(profile.get("following")).isEqualTo(false);
  }

  @Test
  void create_user_with_duplicate_email_and_username_returns_error_payload() {
    User existing = newUser();
    Map<String, Object> payload =
        data(
            execute(
                CREATE_USER,
                vars(
                    "input",
                    Map.of(
                        "email", existing.getEmail(),
                        "username", existing.getUsername(),
                        "password", PASSWORD))),
            "createUser");

    assertThat(payload.get("__typename")).isEqualTo("Error");
    assertThat(payload.get("message")).isEqualTo("BAD_REQUEST");
    List<Map<String, Object>> errors = (List<Map<String, Object>>) payload.get("errors");
    assertThat(errors).extracting(e -> e.get("key")).containsExactlyInAnyOrder("email", "username");
    errors.forEach(e -> assertThat((List<Object>) e.get("value")).isNotEmpty());
  }

  @Test
  void create_user_with_invalid_email_returns_error_payload() {
    Map<String, Object> payload =
        data(
            execute(
                CREATE_USER,
                vars(
                    "input",
                    Map.of(
                        "email", "not-an-email", "username", unique("u"), "password", PASSWORD))),
            "createUser");
    assertThat(payload.get("__typename")).isEqualTo("Error");
    List<Map<String, Object>> errors = (List<Map<String, Object>>) payload.get("errors");
    assertThat(errors).extracting(e -> e.get("key")).containsExactly("email");
    assertThat((List<Object>) errors.get(0).get("value")).containsExactly("should be an email");
  }

  @Test
  void login_with_valid_credentials_returns_token() {
    User user = newUser();
    Map<String, Object> result =
        data(execute(LOGIN, vars("email", user.getEmail(), "password", PASSWORD)), "login", "user");
    assertThat(result.get("username")).isEqualTo(user.getUsername());
    assertThat(jwtService.getSubFromToken((String) result.get("token"))).contains(user.getId());
  }

  @Test
  void login_with_wrong_password_or_unknown_email_is_unauthenticated() {
    User user = newUser();
    ExecutionResult wrongPassword =
        execute(LOGIN, vars("email", user.getEmail(), "password", "wrong"));
    assertThat(errorType(singleError(wrongPassword))).isEqualTo("UNAUTHENTICATED");
    assertThat(((Map<?, ?>) wrongPassword.getData()).get("login")).isNull();

    ExecutionResult unknown =
        execute(LOGIN, vars("email", unique("nobody") + "@example.com", "password", PASSWORD));
    assertThat(errorType(singleError(unknown))).isEqualTo("UNAUTHENTICATED");
  }

  @Test
  void update_user_changes_profile_fields() {
    User user = newUser();
    authenticateAs(user);
    Map<String, Object> result =
        data(
            execute(UPDATE_USER, vars("changes", Map.of("bio", "updated bio", "image", "img.png"))),
            "updateUser",
            "user");
    Map<String, Object> profile = (Map<String, Object>) result.get("profile");
    assertThat(profile.get("bio")).isEqualTo("updated bio");
    assertThat(profile.get("image")).isEqualTo("img.png");
    assertThat(userRepository.findById(user.getId()).orElseThrow().getBio())
        .isEqualTo("updated bio");
  }

  @Test
  void update_user_anonymous_is_unauthenticated() {
    anonymous();
    ExecutionResult result = execute(UPDATE_USER, vars("changes", Map.of("bio", "x")));
    assertThat(errorType(singleError(result))).isEqualTo("UNAUTHENTICATED");
  }

  @Test
  void profile_query_reports_following_relative_to_current_user() {
    User target = newUser();
    User follower = newUser();
    userRepository.saveRelation(new FollowRelation(follower.getId(), target.getId()));

    anonymous();
    Map<String, Object> anonymousView =
        data(execute(PROFILE, vars("username", target.getUsername())), "profile", "profile");
    assertThat(anonymousView.get("username")).isEqualTo(target.getUsername());
    assertThat(anonymousView.get("bio")).isEqualTo(target.getBio());
    assertThat(anonymousView.get("image")).isEqualTo(target.getImage());
    assertThat(anonymousView.get("following")).isEqualTo(false);

    authenticateAs(follower);
    Map<String, Object> followerView =
        data(execute(PROFILE, vars("username", target.getUsername())), "profile", "profile");
    assertThat(followerView.get("following")).isEqualTo(true);
  }

  @Test
  void profile_optional_fields_are_null_when_unset_and_empty_when_blank() {
    User nullish = newUser(null, null);
    Map<String, Object> nullProfile =
        data(execute(PROFILE, vars("username", nullish.getUsername())), "profile", "profile");
    assertThat(nullProfile).containsEntry("bio", null).containsEntry("image", null);

    User blank = newUser("", "");
    Map<String, Object> blankProfile =
        data(execute(PROFILE, vars("username", blank.getUsername())), "profile", "profile");
    assertThat(blankProfile.get("bio")).isEqualTo("");
    assertThat(blankProfile.get("image")).isEqualTo("");
  }

  @Test
  void profile_of_unknown_user_is_not_found() {
    ExecutionResult result = execute(PROFILE, vars("username", unique("ghost")));
    assertThat(errorType(singleError(result))).isEqualTo("NOT_FOUND");
    assertThat(((Map<?, ?>) result.getData()).get("profile")).isNull();
  }

  @Test
  void follow_and_unfollow_user() {
    User target = newUser();
    User follower = newUser();

    anonymous();
    assertThat(errorType(singleError(execute(FOLLOW, vars("username", target.getUsername())))))
        .isEqualTo("UNAUTHENTICATED");

    authenticateAs(follower);
    Map<String, Object> followed =
        data(execute(FOLLOW, vars("username", target.getUsername())), "followUser", "profile");
    assertThat(followed.get("username")).isEqualTo(target.getUsername());
    assertThat(followed.get("following")).isEqualTo(true);
    assertThat(userRepository.findRelation(follower.getId(), target.getId())).isPresent();

    Map<String, Object> unfollowed =
        data(execute(UNFOLLOW, vars("username", target.getUsername())), "unfollowUser", "profile");
    assertThat(unfollowed.get("following")).isEqualTo(false);
    assertThat(userRepository.findRelation(follower.getId(), target.getId())).isEmpty();

    assertThat(errorType(singleError(execute(UNFOLLOW, vars("username", target.getUsername())))))
        .isEqualTo("NOT_FOUND");
    assertThat(errorType(singleError(execute(FOLLOW, vars("username", unique("ghost"))))))
        .isEqualTo("NOT_FOUND");
  }

  @Test
  void me_exposes_profile_of_current_user() {
    User user = newUser();
    authenticateAs(user);
    Map<String, Object> me =
        data(execute("{ me { email username token profile { username bio image } } }"), "me");
    assertThat(me.get("email")).isEqualTo(user.getEmail());
    assertThat(jwtService.getSubFromToken((String) me.get("token"))).contains(user.getId());
    assertThat(((Map<?, ?>) me.get("profile")).get("bio")).isEqualTo(user.getBio());
  }
}
