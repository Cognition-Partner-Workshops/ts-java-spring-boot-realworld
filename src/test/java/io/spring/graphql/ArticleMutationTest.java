package io.spring.graphql;

import static io.spring.graphql.ArticleQueryTest.ISO_UTC_MILLIS;
import static io.spring.graphql.ArticleQueryTest.edges;
import static io.spring.graphql.ArticleQueryTest.pageInfo;
import static org.assertj.core.api.Assertions.assertThat;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArticleMutationTest extends GraphQLTestBase {

  private static final String CREATE_ARTICLE =
      "mutation($input: CreateArticleInput!) { createArticle(input: $input) { article {"
          + " slug title description body tagList favorited favoritesCount createdAt"
          + " author { username following } } } }";

  private static final String UPDATE_ARTICLE =
      "mutation($slug: String!, $changes: UpdateArticleInput!) {"
          + " updateArticle(slug: $slug, changes: $changes) { article { slug title body description } } }";

  private static final String DELETE_ARTICLE =
      "mutation($slug: String!) { deleteArticle(slug: $slug) { success } }";

  private static final String FAVORITE =
      "mutation($slug: String!) { favoriteArticle(slug: $slug) { article { slug favorited favoritesCount } } }";

  private static final String UNFAVORITE =
      "mutation($slug: String!) { unfavoriteArticle(slug: $slug) { article { slug favorited favoritesCount } } }";

  private static final String ADD_COMMENT =
      "mutation($slug: String!, $body: String!) { addComment(slug: $slug, body: $body) {"
          + " comment { id body createdAt updatedAt author { username } article { slug } } } }";

  private static final String DELETE_COMMENT =
      "mutation($slug: String!, $id: ID!) { deleteComment(slug: $slug, id: $id) { success } }";

  private static final String ARTICLE_COMMENTS =
      "query($slug: String!) { article(slug: $slug) { comments(first: 10) {"
          + " edges { cursor node { id body author { username } } }"
          + " pageInfo { startCursor endCursor hasNextPage hasPreviousPage } } } }";

  @Test
  void create_article_as_authenticated_user() {
    User author = newUser();
    authenticateAs(author);
    String title = unique("Created via GraphQL");

    Map<String, Object> article =
        data(
            execute(
                CREATE_ARTICLE,
                vars(
                    "input",
                    Map.of(
                        "title",
                        title,
                        "description",
                        "desc",
                        "body",
                        "body",
                        "tagList",
                        List.of("gql", "dgs")))),
            "createArticle",
            "article");

    assertThat(article.get("title")).isEqualTo(title);
    assertThat((String) article.get("slug")).isEqualTo(title.toLowerCase().replace(' ', '-'));
    assertThat((List<Object>) article.get("tagList")).containsExactlyInAnyOrder("gql", "dgs");
    assertThat(article.get("favorited")).isEqualTo(false);
    assertThat(article.get("favoritesCount")).isEqualTo(0);
    assertThat((String) article.get("createdAt")).matches(ISO_UTC_MILLIS);
    assertThat(((Map<?, ?>) article.get("author")).get("username")).isEqualTo(author.getUsername());
    assertThat(articleRepository.findBySlug((String) article.get("slug"))).isPresent();
  }

  @Test
  void create_article_without_tag_list_defaults_to_empty() {
    authenticateAs(newUser());
    Map<String, Object> article =
        data(
            execute(
                CREATE_ARTICLE,
                vars("input", Map.of("title", unique("no tags"), "description", "d", "body", "b"))),
            "createArticle",
            "article");
    assertThat((List<Object>) article.get("tagList")).isEmpty();
  }

  @Test
  void create_article_anonymous_is_unauthenticated() {
    anonymous();
    ExecutionResult result =
        execute(
            CREATE_ARTICLE,
            vars("input", Map.of("title", unique("anon"), "description", "d", "body", "b")));
    assertThat(errorType(singleError(result))).isEqualTo("UNAUTHENTICATED");
    assertThat(((Map<?, ?>) result.getData()).get("createArticle")).isNull();
  }

  @Test
  void create_article_with_duplicate_title_is_bad_request_with_field_errors() {
    User author = newUser();
    Article existing = newArticle(author);
    authenticateAs(author);

    ExecutionResult result =
        execute(
            CREATE_ARTICLE,
            vars("input", Map.of("title", existing.getTitle(), "description", "d", "body", "b")));

    GraphQLError error = singleError(result);
    assertThat(errorType(error)).isEqualTo("BAD_REQUEST");
    assertThat(error.getExtensions()).containsKey("title");
    assertThat((List<Object>) error.getExtensions().get("title")).isNotEmpty();
    assertThat(error.getPath()).containsExactly("createArticle");
  }

  @Test
  void update_article_by_author_changes_fields_and_keeps_slug_when_title_unchanged() {
    User author = newUser();
    Article article = newArticle(author);
    authenticateAs(author);

    Map<String, Object> updated =
        data(
            execute(
                UPDATE_ARTICLE,
                vars("slug", article.getSlug(), "changes", Map.of("body", "new body"))),
            "updateArticle",
            "article");

    assertThat(updated.get("slug")).isEqualTo(article.getSlug());
    assertThat(updated.get("body")).isEqualTo("new body");
    assertThat(updated.get("title")).isEqualTo(article.getTitle());
  }

  @Test
  void update_article_by_someone_else_is_permission_denied() {
    Article article = newArticle(newUser());
    authenticateAs(newUser());

    ExecutionResult result =
        execute(UPDATE_ARTICLE, vars("slug", article.getSlug(), "changes", Map.of("body", "x")));
    assertThat(errorType(singleError(result))).isEqualTo("PERMISSION_DENIED");
  }

  @Test
  void update_unknown_article_is_not_found() {
    authenticateAs(newUser());
    ExecutionResult result =
        execute(UPDATE_ARTICLE, vars("slug", unique("missing"), "changes", Map.of("body", "x")));
    assertThat(errorType(singleError(result))).isEqualTo("NOT_FOUND");
  }

  @Test
  void delete_article_only_by_its_author() {
    User author = newUser();
    Article article = newArticle(author);

    anonymous();
    assertThat(errorType(singleError(execute(DELETE_ARTICLE, vars("slug", article.getSlug())))))
        .isEqualTo("UNAUTHENTICATED");

    authenticateAs(newUser());
    assertThat(errorType(singleError(execute(DELETE_ARTICLE, vars("slug", article.getSlug())))))
        .isEqualTo("PERMISSION_DENIED");
    assertThat(articleRepository.findBySlug(article.getSlug())).isPresent();

    authenticateAs(author);
    Boolean success =
        data(execute(DELETE_ARTICLE, vars("slug", article.getSlug())), "deleteArticle", "success");
    assertThat(success).isTrue();
    assertThat(articleRepository.findBySlug(article.getSlug())).isEmpty();

    assertThat(errorType(singleError(execute(DELETE_ARTICLE, vars("slug", article.getSlug())))))
        .isEqualTo("NOT_FOUND");
  }

  @Test
  void favorite_and_unfavorite_article() {
    Article article = newArticle(newUser());
    User fan = newUser();
    authenticateAs(fan);

    Map<String, Object> favorited =
        data(execute(FAVORITE, vars("slug", article.getSlug())), "favoriteArticle", "article");
    assertThat(favorited.get("favorited")).isEqualTo(true);
    assertThat(favorited.get("favoritesCount")).isEqualTo(1);

    Map<String, Object> unfavorited =
        data(execute(UNFAVORITE, vars("slug", article.getSlug())), "unfavoriteArticle", "article");
    assertThat(unfavorited.get("favorited")).isEqualTo(false);
    assertThat(unfavorited.get("favoritesCount")).isEqualTo(0);

    anonymous();
    assertThat(errorType(singleError(execute(FAVORITE, vars("slug", article.getSlug())))))
        .isEqualTo("UNAUTHENTICATED");
    authenticateAs(fan);
    assertThat(errorType(singleError(execute(FAVORITE, vars("slug", unique("missing"))))))
        .isEqualTo("NOT_FOUND");
  }

  @Test
  void add_and_delete_comment_with_authorization_rules() {
    User author = newUser();
    User commenter = newUser();
    User stranger = newUser();
    Article article = newArticle(author);

    anonymous();
    assertThat(
            errorType(
                singleError(execute(ADD_COMMENT, vars("slug", article.getSlug(), "body", "hi")))))
        .isEqualTo("UNAUTHENTICATED");

    authenticateAs(commenter);
    assertThat(
            errorType(
                singleError(execute(ADD_COMMENT, vars("slug", unique("missing"), "body", "hi")))))
        .isEqualTo("NOT_FOUND");

    Map<String, Object> comment =
        data(
            execute(ADD_COMMENT, vars("slug", article.getSlug(), "body", "nice article")),
            "addComment",
            "comment");
    String commentId = (String) comment.get("id");
    assertThat(commentId).isNotBlank();
    assertThat(comment.get("body")).isEqualTo("nice article");
    assertThat((String) comment.get("createdAt")).matches(ISO_UTC_MILLIS);
    assertThat((String) comment.get("updatedAt")).matches(ISO_UTC_MILLIS);
    assertThat(((Map<?, ?>) comment.get("author")).get("username"))
        .isEqualTo(commenter.getUsername());
    assertThat(((Map<?, ?>) comment.get("article")).get("slug")).isEqualTo(article.getSlug());

    Map<String, Object> comments =
        data(execute(ARTICLE_COMMENTS, vars("slug", article.getSlug())), "article", "comments");
    assertThat(edges(comments)).hasSize(1);
    assertThat(((Map<?, ?>) edges(comments).get(0).get("node")).get("id")).isEqualTo(commentId);
    assertThat(pageInfo(comments).get("hasNextPage")).isEqualTo(false);
    assertThat(pageInfo(comments).get("hasPreviousPage")).isEqualTo(false);
    assertThat(pageInfo(comments).get("startCursor"))
        .isEqualTo(edges(comments).get(0).get("cursor"));

    authenticateAs(stranger);
    assertThat(
            errorType(
                singleError(
                    execute(DELETE_COMMENT, vars("slug", article.getSlug(), "id", commentId)))))
        .isEqualTo("PERMISSION_DENIED");

    authenticateAs(author);
    assertThat(
            errorType(
                singleError(
                    execute(
                        DELETE_COMMENT, vars("slug", article.getSlug(), "id", unique("missing"))))))
        .isEqualTo("NOT_FOUND");

    authenticateAs(commenter);
    Boolean success =
        data(
            execute(DELETE_COMMENT, vars("slug", article.getSlug(), "id", commentId)),
            "deleteComment",
            "success");
    assertThat(success).isTrue();

    Map<String, Object> afterDelete =
        data(execute(ARTICLE_COMMENTS, vars("slug", article.getSlug())), "article", "comments");
    assertThat(edges(afterDelete)).isEmpty();
    assertThat(pageInfo(afterDelete).get("startCursor")).isNull();
  }

  @Test
  void article_author_can_delete_any_comment_on_own_article() {
    User author = newUser();
    Article article = newArticle(author);
    authenticateAs(newUser());
    String commentId =
        data(
            execute(ADD_COMMENT, vars("slug", article.getSlug(), "body", "from a reader")),
            "addComment",
            "comment",
            "id");

    authenticateAs(author);
    Boolean success =
        data(
            execute(DELETE_COMMENT, vars("slug", article.getSlug(), "id", commentId)),
            "deleteComment",
            "success");
    assertThat(success).isTrue();
  }
}
