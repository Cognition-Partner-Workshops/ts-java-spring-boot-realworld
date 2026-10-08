package io.spring.regression.rest;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.response.Response;
import io.spring.regression.RegressionIntegrationTestBase;
import org.junit.jupiter.api.Test;

class UsersContractTest extends RegressionIntegrationTestBase {

  @Test
  void post_users_registers_and_returns_user_envelope_with_token() {
    String name = uniqueName("reg");
    register(name + "@regression.test", name, "pw12345")
        .then()
        .statusCode(201)
        .body("$", hasKey("user"))
        .body("user.email", equalTo(name + "@regression.test"))
        .body("user.username", equalTo(name))
        .body("user.token", instanceOf(String.class))
        .body("user.token", not(equalTo("")))
        .body("user.bio", equalTo(""))
        .body("user.image", containsString("http"))
        .body("user", not(hasKey("password")))
        .body("user", not(hasKey("id")));
  }

  @Test
  void post_users_rejects_blank_fields_with_422_field_errors() {
    register("", "", "")
        .then()
        .statusCode(422)
        .body("$", hasKey("errors"))
        .body("errors.email", notNullValue())
        .body("errors.username", notNullValue())
        .body("errors.password", notNullValue())
        .body("errors.email[0]", instanceOf(String.class));
  }

  @Test
  void post_users_rejects_invalid_email() {
    register("not-an-email", uniqueName("u"), "pw12345")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("should be an email"));
  }

  @Test
  void post_users_rejects_duplicate_email_and_username() {
    String name = uniqueName("dup");
    register(name + "@regression.test", name, "pw12345").then().statusCode(201);

    register(name + "@regression.test", uniqueName("other"), "pw12345")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("duplicated email"))
        .body("errors", not(hasKey("username")));

    register(uniqueName("other") + "@regression.test", name, "pw12345")
        .then()
        .statusCode(422)
        .body("errors.username[0]", equalTo("duplicated username"))
        .body("errors", not(hasKey("email")));

    register(SEED_EMAIL_JOHN, SEED_USER_JOHN, "pw12345")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("duplicated email"))
        .body("errors.username[0]", equalTo("duplicated username"));
  }

  @Test
  void post_users_with_malformed_json_returns_400() {
    json().body("{\"user\": {\"email\": ").post("/users").then().statusCode(400);
    json().body("{\"user\": {\"email\": ").post("/users/login").then().statusCode(400);
  }

  @Test
  void post_users_without_root_envelope_is_rejected() {
    json()
        .body(map("email", "a@b.com", "username", uniqueName("x"), "password", "pw12345"))
        .post("/users")
        .then()
        .statusCode(anyOf(is(400), is(422)));
  }

  @Test
  void post_users_login_returns_token_for_seeded_user() {
    json()
        .body(envelope("user", map("email", SEED_EMAIL_JOHN, "password", SEED_PASSWORD)))
        .post("/users/login")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(SEED_EMAIL_JOHN))
        .body("user.username", equalTo(SEED_USER_JOHN))
        .body("user.bio", equalTo("Full-stack developer and tech enthusiast"))
        .body("user.image", containsString("dicebear"))
        .body("user.token", instanceOf(String.class));
  }

  @Test
  void post_users_login_with_wrong_password_returns_422_message() {
    json()
        .body(envelope("user", map("email", SEED_EMAIL_JOHN, "password", "wrong")))
        .post("/users/login")
        .then()
        .statusCode(422)
        .body("message", equalTo("invalid email or password"));
  }

  @Test
  void post_users_login_with_unknown_email_returns_422() {
    json()
        .body(envelope("user", map("email", "nobody@regression.test", "password", "x")))
        .post("/users/login")
        .then()
        .statusCode(422)
        .body("message", equalTo("invalid email or password"));
  }

  @Test
  void post_users_login_with_blank_fields_returns_422_field_errors() {
    json()
        .body(envelope("user", map("email", "", "password", "")))
        .post("/users/login")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("can't be empty"))
        .body("errors.password[0]", equalTo("can't be empty"));
  }

  @Test
  void get_user_returns_current_user_and_echoes_token() {
    String token = johnToken();
    authed(token)
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(SEED_EMAIL_JOHN))
        .body("user.username", equalTo(SEED_USER_JOHN))
        .body("user.token", equalTo(token))
        .body("user.bio", instanceOf(String.class))
        .body("user.image", instanceOf(String.class));
  }

  @Test
  void get_user_without_token_returns_401() {
    json().get("/user").then().statusCode(401);
  }

  @Test
  void put_user_updates_profile_fields_and_returns_user_envelope() {
    String name = uniqueName("upd");
    String token = registerAndLogin(name);
    Response response =
        authed(token)
            .body(
                envelope(
                    "user",
                    map(
                        "bio",
                        "new bio",
                        "image",
                        "https://img.example/x.png",
                        "username",
                        name + "x",
                        "email",
                        name + "x@regression.test")))
            .put("/user");
    response
        .then()
        .statusCode(200)
        .body("user.bio", equalTo("new bio"))
        .body("user.image", equalTo("https://img.example/x.png"))
        .body("user.username", equalTo(name + "x"))
        .body("user.email", equalTo(name + "x@regression.test"))
        .body("user.token", equalTo(token));

    authed(token).get("/user").then().statusCode(200).body("user.bio", equalTo("new bio"));
  }

  @Test
  void put_user_with_empty_envelope_keeps_existing_values() {
    String name = uniqueName("keep");
    String token = registerAndLogin(name);
    authed(token)
        .body(envelope("user", map()))
        .put("/user")
        .then()
        .statusCode(200)
        .body("user.username", equalTo(name));
  }

  @Test
  void put_user_rejects_duplicate_email_username_and_invalid_email() {
    String token = registerAndLogin(uniqueName("own"));
    authed(token)
        .body(envelope("user", map("email", SEED_EMAIL_JANE)))
        .put("/user")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("email already exist"));
    authed(token)
        .body(envelope("user", map("username", SEED_USER_JANE)))
        .put("/user")
        .then()
        .statusCode(422)
        .body("errors.username[0]", equalTo("username already exist"));
    authed(token)
        .body(envelope("user", map("email", "nope")))
        .put("/user")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("should be an email"));
  }

  @Test
  void put_user_without_token_returns_401() {
    json().body(envelope("user", map("bio", "x"))).put("/user").then().statusCode(401);
  }

  @Test
  void user_payload_never_leaks_null_bio_as_missing_key() {
    String token = johnToken();
    authed(token).get("/user").then().body("user", hasKey("bio")).body("user", hasKey("image"));
    json()
        .get("/profiles/" + SEED_USER_JOHN)
        .then()
        .body("profile", hasKey("bio"))
        .body("profile", hasKey("image"))
        .body("profile.following", is(false))
        .body("profile.id", nullValue());
  }
}
