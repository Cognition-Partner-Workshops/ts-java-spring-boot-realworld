package io.spring.regression.graphql;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.response.Response;
import io.spring.regression.RegressionIntegrationTestBase;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Contract tests for POST /graphql (DGS). Introspection snapshot + representative operations. */
class GraphqlContractTest extends RegressionIntegrationTestBase {

  private static final String ISO_UTC_MILLIS =
      "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static final List<String> QUERY_FIELDS =
      Arrays.asList("article", "articles", "me", "feed", "profile", "tags");
  private static final List<String> MUTATION_FIELDS =
      Arrays.asList(
          "createUser",
          "login",
          "updateUser",
          "followUser",
          "unfollowUser",
          "createArticle",
          "updateArticle",
          "favoriteArticle",
          "unfavoriteArticle",
          "deleteArticle",
          "addComment",
          "deleteComment");

  private Response graphql(String token, String query, Map<String, Object> variables) {
    Map<String, Object> body = new HashMap<>();
    body.put("query", query);
    if (variables != null) {
      body.put("variables", variables);
    }
    return (token == null ? json() : authed(token)).body(body).post("/graphql");
  }

  private Response graphql(String token, String query) {
    return graphql(token, query, null);
  }

  @Test
  void introspection_snapshot_of_query_and_mutation_fields() {
    graphql(null, "{ __schema { queryType { fields { name } } mutationType { fields { name } } } }")
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body(
            "data.__schema.queryType.fields.name.findAll { it != '_service' }",
            containsInAnyOrder(QUERY_FIELDS.toArray()))
        .body(
            "data.__schema.mutationType.fields.name.findAll { it != '_service' }",
            containsInAnyOrder(MUTATION_FIELDS.toArray()));
  }

  @Test
  void introspection_snapshot_of_article_type_fields() {
    graphql(
            null,
            "{ __type(name: \"Article\") { fields { name type { kind name ofType { name } } } } }")
        .then()
        .statusCode(200)
        .body(
            "data.__type.fields.name",
            containsInAnyOrder(
                "author",
                "body",
                "comments",
                "createdAt",
                "description",
                "favorited",
                "favoritesCount",
                "slug",
                "tagList",
                "title",
                "updatedAt"))
        .body(
            "data.__type.fields.find { it.name == 'favoritesCount' }.type.kind",
            equalTo("NON_NULL"))
        .body(
            "data.__type.fields.find { it.name == 'favoritesCount' }.type.ofType.name",
            equalTo("Int"));
    graphql(
            null,
            "{ __type(name: \"Mutation\") { fields { name args { name type { kind name ofType { name } } } } } }")
        .then()
        .body(
            "data.__type.fields.find { it.name == 'login' }.args.name",
            containsInAnyOrder("email", "password"))
        .body(
            "data.__type.fields.find { it.name == 'createUser' }.args[0].type.name",
            equalTo("CreateUserInput"));
  }

  @Test
  void tags_query_returns_seeded_tags() {
    graphql(null, "{ tags }")
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body("data.tags", hasItem("java"))
        .body("data.tags", hasSize(greaterThanOrEqualTo(7)));
  }

  @Test
  void article_query_returns_full_shape_including_author_and_comments() {
    graphql(
            null,
            "query($slug: String!) { article(slug: $slug) { slug title description body tagList createdAt updatedAt favorited favoritesCount author { username bio image following } comments(first: 10) { edges { cursor node { id body createdAt author { username } } } pageInfo { hasNextPage hasPreviousPage startCursor endCursor } } } }",
            map("slug", SEED_SLUG_SPRING_BOOT))
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body("data.article.slug", equalTo(SEED_SLUG_SPRING_BOOT))
        .body("data.article.title", equalTo("Getting Started with Spring Boot"))
        .body("data.article.tagList", hasItem("java"))
        .body("data.article.createdAt", matchesPattern(ISO_UTC_MILLIS))
        .body("data.article.updatedAt", matchesPattern(ISO_UTC_MILLIS))
        .body("data.article.favorited", is(false))
        .body("data.article.favoritesCount", instanceOf(Integer.class))
        .body("data.article.author.username", equalTo(SEED_USER_JOHN))
        .body("data.article.author.following", is(false))
        .body("data.article.comments.edges", hasSize(greaterThanOrEqualTo(2)))
        .body("data.article.comments.edges[0].node.id", notNullValue())
        .body("data.article.comments.edges[0].node.author.username", notNullValue())
        .body("data.article.comments.edges[0].cursor", notNullValue())
        .body("data.article.comments.pageInfo.hasNextPage", is(false))
        .body("data.article.comments.pageInfo.hasPreviousPage", is(false));
  }

  @Test
  void article_query_for_unknown_slug_returns_error() {
    graphql(null, "{ article(slug: \"no-such-" + uniqueName("") + "\") { slug } }")
        .then()
        .statusCode(200)
        .body("data.article", nullValue())
        .body("errors", hasSize(1))
        .body("errors[0].message", notNullValue())
        .body("errors[0].path", hasItem("article"));
  }

  @Test
  void articles_connection_supports_cursor_pagination_and_filters() {
    String pager = uniqueName("pager");
    String token = registerAndLogin(pager);
    for (int i = 0; i < 5; i++) {
      createArticleSlug(token, "Paged " + i + " " + pager, "paged-" + pager);
    }
    String byPager = "authoredBy: \"" + pager + "\"";
    Response first =
        graphql(
            null,
            "{ articles(first: 2, "
                + byPager
                + ") { edges { cursor node { slug author { username } } } pageInfo { hasNextPage hasPreviousPage startCursor endCursor } } }");
    first
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body("data.articles.edges", hasSize(2))
        .body("data.articles.pageInfo.hasNextPage", is(true))
        .body("data.articles.pageInfo.hasPreviousPage", is(false))
        .body("data.articles.pageInfo.endCursor", notNullValue())
        .body("data.articles.pageInfo.startCursor", notNullValue());
    String endCursor = first.jsonPath().getString("data.articles.pageInfo.endCursor");
    List<String> firstSlugs = first.jsonPath().getList("data.articles.edges.node.slug");

    Response next =
        graphql(
            null,
            "query($after: String) { articles(first: 2, after: $after, "
                + byPager
                + ") { edges { cursor node { slug } } pageInfo { hasPreviousPage hasNextPage } } }",
            map("after", endCursor));
    next.then()
        .statusCode(200)
        .body("data.articles.edges", hasSize(2))
        .body("data.articles.pageInfo.hasNextPage", is(true));
    List<String> nextSlugs = next.jsonPath().getList("data.articles.edges.node.slug");
    for (String slug : nextSlugs) {
      org.junit.jupiter.api.Assertions.assertFalse(firstSlugs.contains(slug), slug);
    }

    graphql(
            null,
            "query($c: String) { articles(last: 2, before: $c, "
                + byPager
                + ") { edges { node { slug } } pageInfo { hasPreviousPage hasNextPage } } }",
            map("c", next.jsonPath().getString("data.articles.edges[0].cursor")))
        .then()
        .statusCode(200)
        .body("data.articles.edges.node.slug", containsInAnyOrder(firstSlugs.toArray()))
        .body("data.articles.pageInfo.hasPreviousPage", is(false));
    graphql(
            null,
            "{ articles(first: 10, "
                + byPager
                + ") { edges { node { slug } } pageInfo { hasNextPage } } }")
        .then()
        .body("data.articles.edges", hasSize(5))
        .body("data.articles.pageInfo.hasNextPage", is(false));

    graphql(null, "{ articles(first: 10, withTag: \"java\") { edges { node { slug tagList } } } }")
        .then()
        .log()
        .ifValidationFails()
        .body("errors", nullValue())
        .body("data.articles.edges.node.slug", hasItem(SEED_SLUG_SPRING_BOOT));
    graphql(
            null,
            "{ articles(first: 10, authoredBy: \""
                + SEED_USER_JOHN
                + "\") { edges { node { author { username } } } } }")
        .then()
        .body("data.articles.edges.node.author.username", hasItem(SEED_USER_JOHN))
        .body("data.articles.edges.node.author.username", not(hasItem(SEED_USER_JANE)));
    graphql(
            null,
            "{ articles(first: 10, favoritedBy: \""
                + SEED_USER_JANE
                + "\") { edges { node { slug } } } }")
        .then()
        .body("data.articles.edges.node.slug", hasItem(SEED_SLUG_SPRING_BOOT));
  }

  @Test
  void profile_query_and_nested_connections() {
    graphql(
            johnToken(),
            "{ profile(username: \""
                + SEED_USER_JANE
                + "\") { profile { username bio image following articles(first: 5) { edges { node { slug } } } favorites(first: 5) { edges { node { slug } } } } } }")
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body("data.profile.profile.username", equalTo(SEED_USER_JANE))
        .body("data.profile.profile.following", is(true))
        .body("data.profile.profile.articles.edges.node.slug", hasItem(SEED_SLUG_REST))
        .body("data.profile.profile.favorites.edges.node.slug", hasItem(SEED_SLUG_SPRING_BOOT));
    graphql(
            null,
            "{ profile(username: \"ghost-" + uniqueName("") + "\") { profile { username } } }")
        .then()
        .statusCode(200)
        .body("errors", hasSize(1));
  }

  @Test
  void me_requires_authentication() {
    graphql(null, "{ me { email username token } }")
        .then()
        .statusCode(200)
        .body("data.me", nullValue())
        .body("errors", hasSize(1))
        .body("errors[0].path", hasItem("me"));
    graphql(johnToken(), "{ me { email username token profile { username following } } }")
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body("data.me.email", equalTo(SEED_EMAIL_JOHN))
        .body("data.me.username", equalTo(SEED_USER_JOHN))
        .body("data.me.token", instanceOf(String.class))
        .body("data.me.profile.username", equalTo(SEED_USER_JOHN));
  }

  @Test
  void feed_requires_authentication_and_returns_followed_authors() {
    graphql(null, "{ feed(first: 5) { edges { node { slug } } } }")
        .then()
        .body("data.feed", nullValue())
        .body("errors", hasSize(1));
    graphql(
            johnToken(),
            "{ feed(first: 5) { edges { node { slug author { username } } } pageInfo { hasNextPage } } }")
        .then()
        .body("errors", nullValue())
        .body("data.feed.edges.node.author.username", hasItem(SEED_USER_JANE))
        .body("data.feed.edges.node.author.username", not(hasItem(SEED_USER_JOHN)));
  }

  @Test
  void login_mutation_returns_user_payload_and_rejects_bad_credentials() {
    graphql(
            null,
            "mutation($email: String!, $password: String!) { login(email: $email, password: $password) { user { email username token profile { username bio image following } } } }",
            map("email", SEED_EMAIL_JOHN, "password", SEED_PASSWORD))
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body("data.login.user.email", equalTo(SEED_EMAIL_JOHN))
        .body("data.login.user.token", instanceOf(String.class))
        .body("data.login.user.profile.username", equalTo(SEED_USER_JOHN));

    graphql(
            null,
            "mutation { login(email: \""
                + SEED_EMAIL_JOHN
                + "\", password: \"wrong\") { user { email } } }")
        .then()
        .statusCode(200)
        .body("data.login", nullValue())
        .body("errors", hasSize(1))
        .body("errors[0].message", equalTo("invalid email or password"))
        .body("errors[0].extensions.errorType", equalTo("UNAUTHENTICATED"));
  }

  @Test
  void create_user_mutation_returns_union_payload_or_validation_error() {
    String name = uniqueName("gql");
    String mutation =
        "mutation($input: CreateUserInput) { createUser(input: $input) { __typename ... on UserPayload { user { email username token } } ... on Error { message errors { key value } } } }";
    graphql(
            null,
            mutation,
            map(
                "input",
                map("email", name + "@regression.test", "username", name, "password", "pw12345")))
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body("data.createUser.__typename", equalTo("UserPayload"))
        .body("data.createUser.user.username", equalTo(name))
        .body("data.createUser.user.token", notNullValue());

    graphql(
            null,
            mutation,
            map(
                "input",
                map("email", SEED_EMAIL_JOHN, "username", SEED_USER_JOHN, "password", "pw12345")))
        .then()
        .statusCode(200)
        .body("data.createUser.__typename", equalTo("Error"))
        .body("data.createUser.errors.key", containsInAnyOrder("email", "username"))
        .body(
            "data.createUser.errors.find { it.key == 'email' }.value", hasItem("duplicated email"));

    graphql(null, mutation, map("input", map("email", "", "username", "", "password", "")))
        .then()
        .statusCode(200)
        .body("data.createUser.__typename", equalTo("Error"))
        .body("data.createUser.errors.key", hasItem("email"))
        .body("data.createUser.errors.key", hasItem("username"))
        .body("data.createUser.errors.key", hasItem("password"));
  }

  @Test
  void article_lifecycle_through_mutations() {
    String token = registerAndLogin(uniqueName("gqlauthor"));
    String title = "GraphQL Article " + uniqueName("g");
    Response created =
        graphql(
            token,
            "mutation($input: CreateArticleInput!) { createArticle(input: $input) { article { slug title description body tagList favorited favoritesCount author { username following } } } }",
            map(
                "input",
                map(
                    "title",
                    title,
                    "description",
                    "d",
                    "body",
                    "b",
                    "tagList",
                    Arrays.asList("gql", "regression"))));
    created
        .then()
        .statusCode(200)
        .body("errors", nullValue())
        .body("data.createArticle.article.title", equalTo(title))
        .body("data.createArticle.article.tagList", containsInAnyOrder("gql", "regression"))
        .body("data.createArticle.article.favorited", is(false))
        .body("data.createArticle.article.favoritesCount", is(0));
    String slug = created.jsonPath().getString("data.createArticle.article.slug");

    graphql(
            token,
            "mutation($slug: String!, $changes: UpdateArticleInput!) { updateArticle(slug: $slug, changes: $changes) { article { slug title body description } } }",
            map("slug", slug, "changes", map("body", "updated body")))
        .then()
        .body("errors", nullValue())
        .body("data.updateArticle.article.body", equalTo("updated body"))
        .body("data.updateArticle.article.slug", equalTo(slug));

    graphql(
            janeToken(),
            "mutation { favoriteArticle(slug: \""
                + slug
                + "\") { article { favorited favoritesCount } } }")
        .then()
        .body("errors", nullValue())
        .body("data.favoriteArticle.article.favorited", is(true))
        .body("data.favoriteArticle.article.favoritesCount", is(1));
    graphql(
            janeToken(),
            "mutation { unfavoriteArticle(slug: \""
                + slug
                + "\") { article { favorited favoritesCount } } }")
        .then()
        .body("data.unfavoriteArticle.article.favorited", is(false))
        .body("data.unfavoriteArticle.article.favoritesCount", is(0));

    Response comment =
        graphql(
            janeToken(),
            "mutation { addComment(slug: \""
                + slug
                + "\", body: \"hello\") { comment { id body createdAt updatedAt author { username } } } }");
    comment
        .then()
        .body("errors", nullValue())
        .body("data.addComment.comment.body", equalTo("hello"))
        .body("data.addComment.comment.createdAt", matchesPattern(ISO_UTC_MILLIS))
        .body("data.addComment.comment.author.username", equalTo(SEED_USER_JANE));
    String commentId = comment.jsonPath().getString("data.addComment.comment.id");

    graphql(
            bobToken(),
            "mutation { deleteComment(slug: \""
                + slug
                + "\", id: \""
                + commentId
                + "\") { success } }")
        .then()
        .body("data.deleteComment", nullValue())
        .body("errors", hasSize(1));
    graphql(
            token,
            "mutation { deleteComment(slug: \""
                + slug
                + "\", id: \""
                + commentId
                + "\") { success } }")
        .then()
        .body("errors", nullValue())
        .body("data.deleteComment.success", is(true));

    graphql(janeToken(), "mutation { deleteArticle(slug: \"" + slug + "\") { success } }")
        .then()
        .body("data.deleteArticle", nullValue())
        .body("errors", hasSize(1));
    graphql(token, "mutation { deleteArticle(slug: \"" + slug + "\") { success } }")
        .then()
        .body("errors", nullValue())
        .body("data.deleteArticle.success", is(true));
    json().get("/articles/" + slug).then().statusCode(404);
  }

  @Test
  void mutations_require_authentication() {
    graphql(
            null,
            "mutation { createArticle(input: {title: \"t\", description: \"d\", body: \"b\"}) { article { slug } } }")
        .then()
        .statusCode(200)
        .body("data.createArticle", nullValue())
        .body("errors", hasSize(1));
    graphql(
            null,
            "mutation { followUser(username: \""
                + SEED_USER_JANE
                + "\") { profile { following } } }")
        .then()
        .body("data.followUser", nullValue())
        .body("errors", hasSize(1));
    graphql(
            null,
            "mutation { favoriteArticle(slug: \""
                + SEED_SLUG_REST
                + "\") { article { favorited } } }")
        .then()
        .body("data.favoriteArticle", nullValue())
        .body("errors", hasSize(1));
    graphql(
            "garbage.token.value",
            "mutation { addComment(slug: \""
                + SEED_SLUG_REST
                + "\", body: \"x\") { comment { id } } }")
        .then()
        .body("data.addComment", nullValue())
        .body("errors", hasSize(1));
    graphql(null, "mutation { updateUser(changes: {bio: \"x\"}) { user { username } } }")
        .then()
        .statusCode(200)
        .body("data.updateUser", nullValue());
  }

  @Test
  void follow_unfollow_and_update_user_mutations() {
    String name = uniqueName("gqlfollow");
    String token = registerAndLogin(name);
    graphql(
            token,
            "mutation { followUser(username: \""
                + SEED_USER_BOB
                + "\") { profile { username following } } }")
        .then()
        .body("errors", nullValue())
        .body("data.followUser.profile.username", equalTo(SEED_USER_BOB))
        .body("data.followUser.profile.following", is(true));
    graphql(
            token,
            "mutation { unfollowUser(username: \""
                + SEED_USER_BOB
                + "\") { profile { following } } }")
        .then()
        .body("errors", nullValue())
        .body("data.unfollowUser.profile.following", is(false));
    graphql(
            token,
            "mutation { updateUser(changes: {bio: \"gql bio\", image: \"https://img/x.png\"}) { user { email username profile { bio image } } } }")
        .then()
        .body("errors", nullValue())
        .body("data.updateUser.user.username", equalTo(name))
        .body("data.updateUser.user.profile.bio", equalTo("gql bio"))
        .body("data.updateUser.user.profile.image", equalTo("https://img/x.png"));
    graphql(
            token,
            "mutation { updateUser(changes: {email: \""
                + SEED_EMAIL_JOHN
                + "\"}) { user { email } } }")
        .then()
        .body("data.updateUser", nullValue())
        .body("errors", hasSize(1))
        .body("errors[0].extensions", hasKey("email"));
  }

  @Test
  void malformed_graphql_document_returns_validation_error() {
    graphql(null, "{ articles { edges { node { nope } } } }")
        .then()
        .statusCode(200)
        .body("errors", hasSize(greaterThanOrEqualTo(1)))
        .body("errors[0].extensions.classification", equalTo("ValidationError"));
    graphql(null, "{ this is not graphql")
        .then()
        .statusCode(200)
        .body("errors", hasSize(greaterThanOrEqualTo(1)));
  }
}
