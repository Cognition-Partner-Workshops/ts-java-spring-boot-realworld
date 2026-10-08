package io.spring.regression.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.response.Response;
import io.spring.regression.RegressionIntegrationTestBase;
import org.junit.jupiter.api.Test;

class CommentsAndFavoritesContractTest extends RegressionIntegrationTestBase {

  @Test
  void favorite_and_unfavorite_update_flag_and_count() {
    String token = registerAndLogin(uniqueName("fav"));
    Response before = authed(token).get("/articles/" + SEED_SLUG_REST);
    int count = before.jsonPath().getInt("article.favoritesCount");

    authed(token)
        .post("/articles/" + SEED_SLUG_REST + "/favorite")
        .then()
        .statusCode(200)
        .body("$", hasKey("article"))
        .body("article.slug", equalTo(SEED_SLUG_REST))
        .body("article.favorited", is(true))
        .body("article.favoritesCount", is(count + 1));
    authed(token)
        .get("/articles/" + SEED_SLUG_REST)
        .then()
        .body("article.favorited", is(true))
        .body("article.favoritesCount", is(count + 1));
    authed(token)
        .delete("/articles/" + SEED_SLUG_REST + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", is(false))
        .body("article.favoritesCount", is(count));
    authed(token)
        .get("/articles/" + SEED_SLUG_REST)
        .then()
        .body("article.favoritesCount", is(count))
        .body("article.favorited", is(false));
  }

  @Test
  void favorite_requires_authentication_and_existing_article() {
    json().post("/articles/" + SEED_SLUG_REST + "/favorite").then().statusCode(401);
    json().delete("/articles/" + SEED_SLUG_REST + "/favorite").then().statusCode(401);
    authed(johnToken())
        .post("/articles/ghost-" + uniqueName("") + "/favorite")
        .then()
        .statusCode(404);
    authed(johnToken())
        .delete("/articles/ghost-" + uniqueName("") + "/favorite")
        .then()
        .statusCode(404);
  }

  @Test
  void get_comments_is_public_and_returns_comments_envelope() {
    json()
        .get("/articles/" + SEED_SLUG_SPRING_BOOT + "/comments")
        .then()
        .statusCode(200)
        .body("$", hasKey("comments"))
        .body("comments", hasSize(greaterThanOrEqualTo(2)))
        .body("comments.id", everyItem(instanceOf(String.class)))
        .body("comments.body", everyItem(notNullValue()))
        .body("comments.createdAt", everyItem(matchesPattern(ArticlesContractTest.ISO_UTC_MILLIS)))
        .body("comments.updatedAt", everyItem(matchesPattern(ArticlesContractTest.ISO_UTC_MILLIS)))
        .body("comments.author.username", everyItem(notNullValue()))
        .body("comments.author.following", everyItem(is(false)))
        .body("comments[0]", not(hasKey("articleId")));
  }

  @Test
  void get_comments_for_unknown_article_returns_404() {
    json().get("/articles/ghost-" + uniqueName("") + "/comments").then().statusCode(404);
  }

  @Test
  void post_comment_creates_and_lists_comment() {
    String token = janeToken();
    String slug = createArticleSlug(johnToken(), "Commented " + uniqueName("c"));
    Response created =
        authed(token)
            .body(envelope("comment", map("body", "nice article")))
            .post("/articles/" + slug + "/comments");
    created
        .then()
        .statusCode(201)
        .body("$", hasKey("comment"))
        .body("comment.id", instanceOf(String.class))
        .body("comment.body", equalTo("nice article"))
        .body("comment.createdAt", matchesPattern(ArticlesContractTest.ISO_UTC_MILLIS))
        .body("comment.updatedAt", matchesPattern(ArticlesContractTest.ISO_UTC_MILLIS))
        .body("comment.author.username", equalTo(SEED_USER_JANE))
        .body("comment.author.following", is(false))
        .body("comment", not(hasKey("articleId")));
    String id = created.jsonPath().getString("comment.id");
    json()
        .get("/articles/" + slug + "/comments")
        .then()
        .statusCode(200)
        .body("comments", hasSize(1))
        .body("comments.id", hasItem(id))
        .body("comments[0].author.following", is(false));
    authed(johnToken())
        .get("/articles/" + slug + "/comments")
        .then()
        .body("comments[0].author.following", is(true));
  }

  @Test
  void post_comment_validation_and_auth() {
    String slug = SEED_SLUG_SPRING_BOOT;
    json()
        .body(envelope("comment", map("body", "x")))
        .post("/articles/" + slug + "/comments")
        .then()
        .statusCode(401);
    authed(johnToken())
        .body(envelope("comment", map("body", "")))
        .post("/articles/" + slug + "/comments")
        .then()
        .statusCode(422)
        .body("errors.body", hasItem("can't be empty"));
    authed(johnToken())
        .body(envelope("comment", map("body", "x")))
        .post("/articles/ghost-" + uniqueName("") + "/comments")
        .then()
        .statusCode(404);
    authed(johnToken())
        .body("{\"comment\": {")
        .post("/articles/" + slug + "/comments")
        .then()
        .statusCode(400);
  }

  @Test
  void delete_comment_allowed_for_comment_author_and_article_author_only() {
    String author = johnToken();
    String commenter = janeToken();
    String stranger = registerAndLogin(uniqueName("stranger"));
    String slug = createArticleSlug(author, "Moderated " + uniqueName("m"));

    String byCommenter =
        authed(commenter)
            .body(envelope("comment", map("body", "one")))
            .post("/articles/" + slug + "/comments")
            .jsonPath()
            .getString("comment.id");
    String byCommenter2 =
        authed(commenter)
            .body(envelope("comment", map("body", "two")))
            .post("/articles/" + slug + "/comments")
            .jsonPath()
            .getString("comment.id");

    json().delete("/articles/" + slug + "/comments/" + byCommenter).then().statusCode(401);
    authed(stranger)
        .delete("/articles/" + slug + "/comments/" + byCommenter)
        .then()
        .statusCode(403);
    authed(commenter)
        .delete("/articles/" + slug + "/comments/" + byCommenter)
        .then()
        .statusCode(204);
    authed(author).delete("/articles/" + slug + "/comments/" + byCommenter2).then().statusCode(204);
    authed(author).delete("/articles/" + slug + "/comments/" + byCommenter2).then().statusCode(404);
    authed(author)
        .delete("/articles/ghost-" + uniqueName("") + "/comments/" + byCommenter)
        .then()
        .statusCode(404);
    json().get("/articles/" + slug + "/comments").then().body("comments", hasSize(0));
  }
}
