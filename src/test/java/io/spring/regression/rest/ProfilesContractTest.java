package io.spring.regression.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.spring.regression.RegressionIntegrationTestBase;
import org.junit.jupiter.api.Test;

class ProfilesContractTest extends RegressionIntegrationTestBase {

  @Test
  void get_profile_is_public_and_returns_profile_envelope() {
    json()
        .get("/profiles/" + SEED_USER_JANE)
        .then()
        .statusCode(200)
        .body("$", hasKey("profile"))
        .body("profile.username", equalTo(SEED_USER_JANE))
        .body("profile.bio", instanceOf(String.class))
        .body("profile.image", instanceOf(String.class))
        .body("profile.following", is(false))
        .body("profile", not(hasKey("email")))
        .body("profile", not(hasKey("password")));
  }

  @Test
  void get_profile_reflects_seeded_follow_relation_for_authenticated_user() {
    authed(johnToken())
        .get("/profiles/" + SEED_USER_JANE)
        .then()
        .statusCode(200)
        .body("profile.following", is(true));
    authed(johnToken())
        .get("/profiles/" + SEED_USER_BOB)
        .then()
        .statusCode(200)
        .body("profile.following", is(false));
  }

  @Test
  void get_unknown_profile_returns_404() {
    json().get("/profiles/no-such-user-" + uniqueName("")).then().statusCode(404);
  }

  @Test
  void follow_and_unfollow_toggle_following_flag() {
    String token = registerAndLogin(uniqueName("follower"));
    authed(token)
        .post("/profiles/" + SEED_USER_BOB + "/follow")
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(SEED_USER_BOB))
        .body("profile.following", is(true));
    authed(token).get("/profiles/" + SEED_USER_BOB).then().body("profile.following", is(true));
    authed(token)
        .delete("/profiles/" + SEED_USER_BOB + "/follow")
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(SEED_USER_BOB))
        .body("profile.following", is(false));
    authed(token).get("/profiles/" + SEED_USER_BOB).then().body("profile.following", is(false));
  }

  @Test
  void follow_requires_authentication() {
    json().post("/profiles/" + SEED_USER_BOB + "/follow").then().statusCode(401);
    json().delete("/profiles/" + SEED_USER_BOB + "/follow").then().statusCode(401);
  }

  @Test
  void follow_unknown_user_returns_404() {
    authed(johnToken())
        .post("/profiles/ghost-" + uniqueName("") + "/follow")
        .then()
        .statusCode(404);
    authed(johnToken())
        .delete("/profiles/ghost-" + uniqueName("") + "/follow")
        .then()
        .statusCode(404);
  }
}
