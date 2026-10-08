package io.spring.regression.rest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.response.Response;
import io.spring.regression.RegressionIntegrationTestBase;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArticlesContractTest extends RegressionIntegrationTestBase {

  static final String ISO_UTC_MILLIS = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  @Test
  void post_articles_creates_article_with_full_envelope() {
    String token = janeToken();
    String title = "Contract " + uniqueName("t");
    createArticle(token, title, "regression", "contract")
        .then()
        .statusCode(200)
        .body("$", hasKey("article"))
        .body("article.slug", equalTo(title.toLowerCase().replace(' ', '-')))
        .body("article.title", equalTo(title))
        .body("article.description", equalTo("description of " + title))
        .body("article.body", equalTo("body of " + title))
        .body("article.tagList", containsInAnyOrder("regression", "contract"))
        .body("article.favorited", is(false))
        .body("article.favoritesCount", is(0))
        .body("article.createdAt", matchesPattern(ISO_UTC_MILLIS))
        .body("article.updatedAt", matchesPattern(ISO_UTC_MILLIS))
        .body("article.author.username", equalTo(SEED_USER_JANE))
        .body("article.author.following", is(false))
        .body("article.author", hasKey("bio"))
        .body("article.author", hasKey("image"));
  }

  @Test
  void post_articles_requires_authentication() {
    json()
        .body(envelope("article", map("title", "x", "description", "y", "body", "z")))
        .post("/articles")
        .then()
        .statusCode(401);
  }

  @Test
  void post_articles_rejects_blank_fields_with_422() {
    authed(johnToken())
        .body(envelope("article", map("title", "", "description", "", "body", "")))
        .post("/articles")
        .then()
        .statusCode(422)
        .body("errors.title", hasItem("can't be empty"))
        .body("errors.description", hasItem("can't be empty"))
        .body("errors.body", hasItem("can't be empty"));
  }

  @Test
  void post_articles_rejects_duplicate_slug_with_422() {
    String token = johnToken();
    String title = "Duplicate " + uniqueName("d");
    createArticle(token, title).then().statusCode(200);
    createArticle(token, title)
        .then()
        .statusCode(422)
        .body("errors.title[0]", equalTo("article name exists"));
    createArticle(janeToken(), "Getting Started with Spring Boot").then().statusCode(422);
  }

  @Test
  void post_articles_with_malformed_json_returns_400() {
    authed(johnToken()).body("{\"article\": [").post("/articles").then().statusCode(400);
  }

  @Test
  void get_articles_lists_seeded_articles_with_count() {
    json()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("$", hasKey("articles"))
        .body("$", hasKey("articlesCount"))
        .body("articlesCount", instanceOf(Integer.class))
        .body("articlesCount", greaterThanOrEqualTo(5))
        .body("articles", hasSize(greaterThanOrEqualTo(5)))
        .body("articles.slug", hasItem(SEED_SLUG_SPRING_BOOT))
        .body("articles.favorited", everyItem(is(false)))
        .body("articles.createdAt", everyItem(matchesPattern(ISO_UTC_MILLIS)))
        .body("articles.author.username", everyItem(notNullValue()))
        .body("articles.tagList", everyItem(instanceOf(List.class)));
  }

  @Test
  void get_articles_supports_limit_and_offset() {
    Response page1 = json().queryParam("limit", 2).queryParam("offset", 0).get("/articles");
    Response page2 = json().queryParam("limit", 2).queryParam("offset", 2).get("/articles");
    page1.then().statusCode(200).body("articles", hasSize(2));
    page2.then().statusCode(200).body("articles", hasSize(2));
    List<String> slugs1 = page1.jsonPath().getList("articles.slug");
    List<String> slugs2 = page2.jsonPath().getList("articles.slug");
    assert !slugs1.contains(slugs2.get(0));
    assert !slugs1.contains(slugs2.get(1));
    assert page1.jsonPath().getInt("articlesCount") == page2.jsonPath().getInt("articlesCount");
    json()
        .queryParam("limit", 500)
        .get("/articles")
        .then()
        .body("articles.size()", lessThanOrEqualTo(100));
  }

  @Test
  void get_articles_filters_by_tag_author_and_favorited() {
    json()
        .queryParam("tag", "java")
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles.size()", greaterThanOrEqualTo(1))
        .body("articles.tagList", everyItem(hasItem("java")))
        .body("articlesCount", greaterThanOrEqualTo(1));
    json()
        .queryParam("author", SEED_USER_JOHN)
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles.author.username", everyItem(equalTo(SEED_USER_JOHN)))
        .body("articles.size()", greaterThanOrEqualTo(1));
    json()
        .queryParam("favorited", SEED_USER_JANE)
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles.slug", hasItem(SEED_SLUG_SPRING_BOOT));
    json()
        .queryParam("tag", "no-such-tag-" + uniqueName(""))
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0))
        .body("articlesCount", is(0));
    json()
        .queryParam("author", "ghost-" + uniqueName(""))
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0));
  }

  @Test
  void get_articles_marks_favorited_for_authenticated_user() {
    authed(janeToken())
        .queryParam("favorited", SEED_USER_JANE)
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles.favorited", everyItem(is(true)))
        .body("articles.favoritesCount", everyItem(greaterThanOrEqualTo(1)));
  }

  @Test
  void get_feed_returns_articles_of_followed_users_only() {
    authed(johnToken())
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("$", hasKey("articles"))
        .body("$", hasKey("articlesCount"))
        .body("articles.author.username", everyItem(equalTo(SEED_USER_JANE)))
        .body("articles.size()", greaterThanOrEqualTo(1));
    String lonely = registerAndLogin(uniqueName("lonely"));
    authed(lonely)
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0))
        .body("articlesCount", is(0));
    authed(johnToken())
        .queryParam("limit", 1)
        .queryParam("offset", 0)
        .get("/articles/feed")
        .then()
        .body("articles", hasSize(1));
  }

  @Test
  void get_feed_requires_authentication() {
    json().get("/articles/feed").then().statusCode(401);
  }

  @Test
  void get_article_by_slug_is_public() {
    json()
        .get("/articles/" + SEED_SLUG_SPRING_BOOT)
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(SEED_SLUG_SPRING_BOOT))
        .body("article.title", equalTo("Getting Started with Spring Boot"))
        .body("article.author.username", equalTo(SEED_USER_JOHN))
        .body("article.favoritesCount", instanceOf(Integer.class))
        .body("article.favorited", is(false))
        .body("article.tagList", hasItem("java"))
        .body("article.body", instanceOf(String.class));
    authed(janeToken())
        .get("/articles/" + SEED_SLUG_SPRING_BOOT)
        .then()
        .body("article.favorited", is(true))
        .body("article.favoritesCount", is(2))
        .body("article.author.following", is(true));
    json()
        .queryParam("author", SEED_USER_JOHN)
        .get("/articles")
        .then()
        .body("articles.find { it.slug == '" + SEED_SLUG_SPRING_BOOT + "' }.favoritesCount", is(2));
  }

  @Test
  void get_unknown_article_returns_404() {
    json().get("/articles/no-such-slug-" + uniqueName("")).then().statusCode(404);
  }

  @Test
  void put_article_updates_fields_and_regenerates_slug() {
    String token = johnToken();
    String slug = createArticleSlug(token, "Before Update " + uniqueName("u"), "x");
    String newTitle = "After Update " + uniqueName("u");
    authed(token)
        .body(envelope("article", map("title", newTitle, "body", "new body")))
        .put("/articles/" + slug)
        .then()
        .statusCode(200)
        .body("article.title", equalTo(newTitle))
        .body("article.slug", equalTo(newTitle.toLowerCase().replace(' ', '-')))
        .body("article.body", equalTo("new body"))
        .body("article.description", equalTo("description of Before Update " + slug.substring(14)))
        .body("article.tagList", contains("x"));
    json().get("/articles/" + slug).then().statusCode(404);
  }

  @Test
  void put_article_with_empty_envelope_keeps_values() {
    String token = johnToken();
    String title = "Unchanged " + uniqueName("u");
    String slug = createArticleSlug(token, title);
    authed(token)
        .body(envelope("article", map()))
        .put("/articles/" + slug)
        .then()
        .statusCode(200)
        .body("article.title", equalTo(title))
        .body("article.slug", equalTo(slug));
  }

  @Test
  void put_article_by_non_author_returns_403() {
    String slug = createArticleSlug(johnToken(), "Johns Article " + uniqueName("j"));
    authed(janeToken())
        .body(envelope("article", map("title", "hijack")))
        .put("/articles/" + slug)
        .then()
        .statusCode(403);
    json()
        .body(envelope("article", map("title", "x")))
        .put("/articles/" + slug)
        .then()
        .statusCode(401);
    authed(johnToken())
        .body(envelope("article", map("title", "x")))
        .put("/articles/ghost-" + uniqueName(""))
        .then()
        .statusCode(404);
  }

  @Test
  void delete_article_removes_it_and_enforces_ownership() {
    String token = johnToken();
    String slug = createArticleSlug(token, "Delete Me " + uniqueName("d"));
    authed(janeToken()).delete("/articles/" + slug).then().statusCode(403);
    json().delete("/articles/" + slug).then().statusCode(401);
    authed(token).delete("/articles/" + slug).then().statusCode(204);
    json().get("/articles/" + slug).then().statusCode(404);
    authed(token).delete("/articles/" + slug).then().statusCode(404);
  }

  @Test
  void get_tags_returns_seeded_tag_names() {
    json()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("$", hasKey("tags"))
        .body("tags", instanceOf(List.class))
        .body("tags", hasItem("java"))
        .body("tags", hasItem("spring-boot"))
        .body("tags.size()", greaterThanOrEqualTo(7))
        .body("tags", everyItem(instanceOf(String.class)))
        .body("tags", everyItem(not(nullValue())));
  }
}
