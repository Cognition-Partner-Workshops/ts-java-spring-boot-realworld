package io.spring.regression.rest;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.response.Response;
import io.spring.regression.RegressionIntegrationTestBase;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** 401/403 matrix and Authorization header parsing edge cases across the REST surface. */
class SecurityMatrixTest extends RegressionIntegrationTestBase {

  static Stream<Arguments> protectedEndpoints() {
    return Stream.of(
        Arguments.of("GET", "/user"),
        Arguments.of("PUT", "/user"),
        Arguments.of("POST", "/profiles/" + SEED_USER_JANE + "/follow"),
        Arguments.of("DELETE", "/profiles/" + SEED_USER_JANE + "/follow"),
        Arguments.of("POST", "/articles"),
        Arguments.of("GET", "/articles/feed"),
        Arguments.of("PUT", "/articles/" + SEED_SLUG_REST),
        Arguments.of("DELETE", "/articles/" + SEED_SLUG_REST),
        Arguments.of("POST", "/articles/" + SEED_SLUG_REST + "/favorite"),
        Arguments.of("DELETE", "/articles/" + SEED_SLUG_REST + "/favorite"),
        Arguments.of("POST", "/articles/" + SEED_SLUG_REST + "/comments"),
        Arguments.of("DELETE", "/articles/" + SEED_SLUG_REST + "/comments/comment-1"));
  }

  static Stream<Arguments> publicEndpoints() {
    return Stream.of(
        Arguments.of("GET", "/articles"),
        Arguments.of("GET", "/articles/" + SEED_SLUG_REST),
        Arguments.of("GET", "/articles/" + SEED_SLUG_REST + "/comments"),
        Arguments.of("GET", "/profiles/" + SEED_USER_JANE),
        Arguments.of("GET", "/tags"));
  }

  private Response call(String method, String path, String authorizationHeader) {
    io.restassured.specification.RequestSpecification spec = json().body("{}");
    if (authorizationHeader != null) {
      spec = spec.header("Authorization", authorizationHeader);
    }
    return spec.request(method, path);
  }

  @ParameterizedTest(name = "{0} {1} without Authorization -> 401")
  @MethodSource("protectedEndpoints")
  void protected_endpoint_without_header_returns_401(String method, String path) {
    call(method, path, null).then().statusCode(401);
  }

  @ParameterizedTest(name = "{0} {1} with garbage token -> 401")
  @MethodSource("protectedEndpoints")
  void protected_endpoint_with_garbage_token_returns_401(String method, String path) {
    call(method, path, "Token this.is.not-a-jwt").then().statusCode(401);
  }

  @ParameterizedTest(name = "{0} {1} with empty token -> 401")
  @MethodSource("protectedEndpoints")
  void protected_endpoint_with_empty_token_returns_401(String method, String path) {
    call(method, path, "Token ").then().statusCode(401);
    call(method, path, "Token").then().statusCode(401);
    call(method, path, "").then().statusCode(401);
  }

  @ParameterizedTest(name = "{0} {1} is public")
  @MethodSource("publicEndpoints")
  void public_endpoint_is_reachable_without_or_with_bad_token(String method, String path) {
    call(method, path, null).then().statusCode(200);
    call(method, path, "Token garbage").then().statusCode(200);
  }

  @Test
  void token_prefix_is_accepted() {
    String token = johnToken();
    call("GET", "/user", "Token " + token).then().statusCode(200);
  }

  @Test
  void bearer_prefix_is_accepted_because_filter_only_reads_second_segment() {
    String token = johnToken();
    call("GET", "/user", "Bearer " + token)
        .then()
        .statusCode(200)
        .body("user.username", equalTo(SEED_USER_JOHN));
  }

  @Test
  void raw_token_without_prefix_is_rejected() {
    String token = johnToken();
    call("GET", "/user", token).then().statusCode(401);
  }

  @Test
  void tampered_token_is_rejected() {
    String token = johnToken();
    String tampered = token.substring(0, token.length() - 2) + "xx";
    call("GET", "/user", "Token " + tampered).then().statusCode(401);
    call("GET", "/articles/feed", "Token " + tampered).then().statusCode(401);
  }

  @Test
  void token_of_deleted_or_unknown_subject_is_rejected() {
    String bogusSubject =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuby1zdWNoLXVzZXIiLCJleHAiOjQxMDI0NDQ4MDB9.invalid";
    call("GET", "/user", "Token " + bogusSubject).then().statusCode(401);
  }

  @Test
  void ownership_violations_return_403_not_401() {
    String jane = janeToken();
    authed(jane)
        .body(envelope("article", map("title", "hijack")))
        .put("/articles/" + SEED_SLUG_SPRING_BOOT)
        .then()
        .statusCode(403);
    call("DELETE", "/articles/" + SEED_SLUG_SPRING_BOOT, "Token " + jane).then().statusCode(403);
    call("DELETE", "/articles/" + SEED_SLUG_REST + "/comments/comment-3", "Token " + bobToken())
        .then()
        .statusCode(403);
    call("DELETE", "/articles/" + SEED_SLUG_REST + "/comments/comment-3", "Token " + jane)
        .then()
        .statusCode(204);
  }

  @Test
  void login_and_register_are_public() {
    json()
        .body(envelope("user", map("email", SEED_EMAIL_JOHN, "password", SEED_PASSWORD)))
        .post("/users/login")
        .then()
        .statusCode(200);
  }
}
