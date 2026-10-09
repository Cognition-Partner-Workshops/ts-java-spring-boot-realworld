package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import graphql.ExecutionResult;
import io.spring.core.article.Article;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ArticleQueryTest extends GraphQLTestBase {

  static final String ISO_UTC_MILLIS = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static final String ARTICLES_QUERY =
      "query($first: Int, $after: String, $last: Int, $before: String,"
          + " $authoredBy: String, $favoritedBy: String, $withTag: String) {"
          + " articles(first: $first, after: $after, last: $last, before: $before,"
          + " authoredBy: $authoredBy, favoritedBy: $favoritedBy, withTag: $withTag) {"
          + " edges { cursor node { slug title favorited favoritesCount tagList"
          + " author { username following } } }"
          + " pageInfo { startCursor endCursor hasNextPage hasPreviousPage } } }";

  private static final String ARTICLE_QUERY =
      "query($slug: String!) { article(slug: $slug) { slug title description body tagList"
          + " favorited favoritesCount readingTimeMinutes createdAt updatedAt"
          + " author { username bio image following } } }";

  private static final String FEED_QUERY =
      "query($first: Int) { feed(first: $first) { edges { node { slug author { username } } }"
          + " pageInfo { hasNextPage hasPreviousPage } } }";

  @Test
  void article_by_slug_returns_full_article_with_iso_timestamps() {
    User author = newUser();
    Article article = newArticle(author, "gql-tag-a", "gql-tag-b");
    anonymous();

    Map<String, Object> result =
        data(execute(ARTICLE_QUERY, vars("slug", article.getSlug())), "article");

    assertThat(result.get("slug")).isEqualTo(article.getSlug());
    assertThat(result.get("title")).isEqualTo(article.getTitle());
    assertThat(result.get("description")).isEqualTo(article.getDescription());
    assertThat(result.get("body")).isEqualTo(article.getBody());
    assertThat((List<Object>) result.get("tagList"))
        .containsExactlyInAnyOrder("gql-tag-a", "gql-tag-b");
    assertThat(result.get("favorited")).isEqualTo(false);
    assertThat(result.get("favoritesCount")).isEqualTo(0);
    assertThat(result.get("readingTimeMinutes")).isEqualTo(1);
    assertThat((String) result.get("createdAt")).matches(ISO_UTC_MILLIS);
    assertThat((String) result.get("updatedAt")).matches(ISO_UTC_MILLIS);
    assertThat(result.get("createdAt")).isEqualTo(result.get("updatedAt"));

    Map<String, Object> profile = (Map<String, Object>) result.get("author");
    assertThat(profile.get("username")).isEqualTo(author.getUsername());
    assertThat(profile.get("bio")).isEqualTo(author.getBio());
    assertThat(profile.get("image")).isEqualTo(author.getImage());
    assertThat(profile.get("following")).isEqualTo(false);
  }

  @Test
  void article_reading_time_uses_the_article_body() {
    User author = newUser();
    String title = unique("gql-reading-time");
    String body = String.join(" ", Collections.nCopies(450, "word"));
    Article article = new Article(title, "description", body, List.of(), author.getId());
    articleRepository.save(article);
    anonymous();

    Map<String, Object> result =
        data(execute(ARTICLE_QUERY, vars("slug", article.getSlug())), "article");

    assertThat(result.get("readingTimeMinutes")).isEqualTo(3);
  }

  @Test
  void article_by_unknown_slug_maps_to_not_found() {
    ExecutionResult result = execute(ARTICLE_QUERY, vars("slug", unique("missing")));
    assertThat(errorType(singleError(result))).isEqualTo("NOT_FOUND");
    assertThat(((Map<?, ?>) result.getData()).get("article")).isNull();
  }

  @Test
  void articles_can_be_filtered_by_author_tag_and_favorited_by() {
    User author = newUser();
    User other = newUser();
    User fan = newUser();
    String tag = unique("gql-tag");
    Article tagged = newArticle(author, tag);
    Article plain = newArticle(author);
    newArticle(other, tag);

    authenticateAs(fan);
    execute(
        "mutation($slug: String!) { favoriteArticle(slug: $slug) { article { slug } } }",
        vars("slug", tagged.getSlug()));

    assertThat(slugs(articles(vars("first", 10, "authoredBy", author.getUsername()))))
        .containsExactlyInAnyOrder(tagged.getSlug(), plain.getSlug());
    assertThat(slugs(articles(vars("first", 10, "withTag", tag)))).hasSize(2);
    assertThat(
            slugs(articles(vars("first", 10, "authoredBy", author.getUsername(), "withTag", tag))))
        .containsExactly(tagged.getSlug());

    Map<String, Object> favorites = articles(vars("first", 10, "favoritedBy", fan.getUsername()));
    assertThat(slugs(favorites)).containsExactly(tagged.getSlug());
    Map<String, Object> node = firstNode(favorites);
    assertThat(node.get("favorited")).isEqualTo(true);
    assertThat(node.get("favoritesCount")).isEqualTo(1);

    anonymous();
    assertThat(
            firstNode(articles(vars("first", 10, "favoritedBy", fan.getUsername())))
                .get("favorited"))
        .isEqualTo(false);
  }

  @Test
  void articles_paginate_forward_with_first_after() throws Exception {
    User author = newUser();
    Article oldest = newArticle(author);
    Thread.sleep(5);
    Article middle = newArticle(author);
    Thread.sleep(5);
    Article newest = newArticle(author);
    anonymous();

    Map<String, Object> page1 = articles(vars("first", 2, "authoredBy", author.getUsername()));
    assertThat(slugs(page1)).containsExactly(newest.getSlug(), middle.getSlug());
    Map<String, Object> pageInfo1 = pageInfo(page1);
    assertThat(pageInfo1.get("hasNextPage")).isEqualTo(true);
    assertThat(pageInfo1.get("hasPreviousPage")).isEqualTo(false);
    assertThat(pageInfo1.get("startCursor")).isEqualTo(cursors(page1).get(0));
    assertThat(pageInfo1.get("endCursor")).isEqualTo(cursors(page1).get(1));

    Map<String, Object> page2 =
        articles(
            vars(
                "first",
                2,
                "after",
                pageInfo1.get("endCursor"),
                "authoredBy",
                author.getUsername()));
    assertThat(slugs(page2)).containsExactly(oldest.getSlug());
    Map<String, Object> pageInfo2 = pageInfo(page2);
    assertThat(pageInfo2.get("hasNextPage")).isEqualTo(false);
    assertThat(pageInfo2.get("hasPreviousPage")).isEqualTo(false);
    assertThat(pageInfo2.get("startCursor")).isEqualTo(pageInfo2.get("endCursor"));

    Map<String, Object> empty =
        articles(
            vars(
                "first",
                2,
                "after",
                pageInfo2.get("endCursor"),
                "authoredBy",
                author.getUsername()));
    assertThat(slugs(empty)).isEmpty();
    assertThat(pageInfo(empty).get("startCursor")).isNull();
    assertThat(pageInfo(empty).get("endCursor")).isNull();
    assertThat(pageInfo(empty).get("hasNextPage")).isEqualTo(false);
  }

  @Test
  void articles_paginate_backward_with_last_before() throws Exception {
    User author = newUser();
    Article oldest = newArticle(author);
    Thread.sleep(5);
    Article middle = newArticle(author);
    Thread.sleep(5);
    Article newest = newArticle(author);
    anonymous();

    Map<String, Object> all = articles(vars("first", 10, "authoredBy", author.getUsername()));
    String oldestCursor = cursors(all).get(2);

    Map<String, Object> page =
        articles(vars("last", 1, "before", oldestCursor, "authoredBy", author.getUsername()));
    assertThat(slugs(page)).containsExactly(middle.getSlug());
    assertThat(pageInfo(page).get("hasPreviousPage")).isEqualTo(true);
    assertThat(pageInfo(page).get("hasNextPage")).isEqualTo(false);

    Map<String, Object> rest =
        articles(vars("last", 5, "before", oldestCursor, "authoredBy", author.getUsername()));
    assertThat(slugs(rest)).containsExactlyInAnyOrder(newest.getSlug(), middle.getSlug());
    assertThat(slugs(rest)).doesNotContain(oldest.getSlug());
    assertThat(pageInfo(rest).get("hasPreviousPage")).isEqualTo(false);
    assertThat(pageInfo(rest).get("hasNextPage")).isEqualTo(false);
  }

  @Test
  void articles_without_first_or_last_is_rejected() {
    ExecutionResult result = execute(ARTICLES_QUERY, vars());
    assertThat(result.getErrors()).isNotEmpty();
    assertThat(((Map<?, ?>) result.getData()).get("articles")).isNull();
  }

  @Test
  void feed_requires_authentication() {
    anonymous();
    ExecutionResult result = execute(FEED_QUERY, vars("first", 10));
    assertThat(errorType(singleError(result))).isEqualTo("UNAUTHENTICATED");
    assertThat(((Map<?, ?>) result.getData()).get("feed")).isNull();
  }

  @Test
  void feed_lists_articles_from_followed_authors_only() {
    User author = newUser();
    User stranger = newUser();
    User reader = newUser();
    Article followed = newArticle(author);
    newArticle(stranger);
    userRepository.saveRelation(new FollowRelation(reader.getId(), author.getId()));

    authenticateAs(reader);
    Map<String, Object> feed = data(execute(FEED_QUERY, vars("first", 10)), "feed");
    assertThat(slugs(feed)).containsExactly(followed.getSlug());
    assertThat(pageInfo(feed).get("hasNextPage")).isEqualTo(false);
    assertThat(pageInfo(feed).get("hasPreviousPage")).isEqualTo(false);
  }

  @Test
  void profile_articles_and_favorites_connections_work() {
    User author = newUser();
    User fan = newUser();
    Article article = newArticle(author);
    authenticateAs(fan);
    execute(
        "mutation($slug: String!) { favoriteArticle(slug: $slug) { article { slug } } }",
        vars("slug", article.getSlug()));

    String query =
        "query($username: String!) { profile(username: $username) { profile { username"
            + " articles(first: 5) { edges { node { slug } } pageInfo { hasNextPage } }"
            + " favorites(first: 5) { edges { node { slug favorited } } } } } }";
    Map<String, Object> authorProfile =
        data(execute(query, vars("username", author.getUsername())), "profile", "profile");
    assertThat(slugs((Map<String, Object>) authorProfile.get("articles")))
        .containsExactly(article.getSlug());
    assertThat(slugs((Map<String, Object>) authorProfile.get("favorites"))).isEmpty();

    Map<String, Object> fanProfile =
        data(execute(query, vars("username", fan.getUsername())), "profile", "profile");
    assertThat(slugs((Map<String, Object>) fanProfile.get("favorites")))
        .containsExactly(article.getSlug());
    assertThat(firstNode((Map<String, Object>) fanProfile.get("favorites")).get("favorited"))
        .isEqualTo(true);
  }

  @Test
  void tags_include_tags_of_created_articles() {
    String tag = unique("gql-tag");
    newArticle(newUser(), tag);
    List<String> tags = data(execute("{ tags }"), "tags");
    assertThat(tags).contains(tag);
  }

  private Map<String, Object> articles(Map<String, Object> variables) {
    return data(execute(ARTICLES_QUERY, variables), "articles");
  }

  @SuppressWarnings("unchecked")
  static List<Map<String, Object>> edges(Map<String, Object> connection) {
    return (List<Map<String, Object>>) connection.get("edges");
  }

  @SuppressWarnings("unchecked")
  static List<String> slugs(Map<String, Object> connection) {
    return edges(connection).stream()
        .map(e -> (String) ((Map<String, Object>) e.get("node")).get("slug"))
        .collect(Collectors.toList());
  }

  static List<String> cursors(Map<String, Object> connection) {
    return edges(connection).stream()
        .map(e -> (String) e.get("cursor"))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> firstNode(Map<String, Object> connection) {
    return (Map<String, Object>) edges(connection).get(0).get("node");
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> pageInfo(Map<String, Object> connection) {
    return (Map<String, Object>) connection.get("pageInfo");
  }
}
