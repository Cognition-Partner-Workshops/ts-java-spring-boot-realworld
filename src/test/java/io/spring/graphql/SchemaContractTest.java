package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import graphql.ExecutionResult;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Guards the GraphQL contract: the SDL must parse and the executable schema must expose it.
 * Framework-added members (the federation {@code _service} field / {@code _Service} type that DGS
 * registers automatically) are ignored because they are not part of the application contract.
 */
class SchemaContractTest extends GraphQLTestBase {

  private static final List<String> QUERY_FIELDS =
      List.of("article", "articles", "feed", "me", "profile", "tags");

  private static final List<String> MUTATION_FIELDS =
      List.of(
          "addComment",
          "createArticle",
          "createUser",
          "deleteArticle",
          "deleteComment",
          "favoriteArticle",
          "followUser",
          "login",
          "unfavoriteArticle",
          "unfollowUser",
          "updateArticle",
          "updateUser");

  private static final List<String> TYPE_FIELD_SNAPSHOT =
      List.of(
          "Article.author",
          "Article.body",
          "Article.comments",
          "Article.createdAt",
          "Article.description",
          "Article.favorited",
          "Article.favoritesCount",
          "Article.readingTimeMinutes",
          "Article.slug",
          "Article.tagList",
          "Article.title",
          "Article.updatedAt",
          "ArticleEdge.cursor",
          "ArticleEdge.node",
          "ArticlePayload.article",
          "ArticlesConnection.edges",
          "ArticlesConnection.pageInfo",
          "Comment.article",
          "Comment.author",
          "Comment.body",
          "Comment.createdAt",
          "Comment.id",
          "Comment.updatedAt",
          "CommentEdge.cursor",
          "CommentEdge.node",
          "CommentPayload.comment",
          "CommentsConnection.edges",
          "CommentsConnection.pageInfo",
          "DeletionStatus.success",
          "Error.errors",
          "Error.message",
          "ErrorItem.key",
          "ErrorItem.value",
          "PageInfo.endCursor",
          "PageInfo.hasNextPage",
          "PageInfo.hasPreviousPage",
          "PageInfo.startCursor",
          "Profile.articles",
          "Profile.bio",
          "Profile.favorites",
          "Profile.feed",
          "Profile.following",
          "Profile.image",
          "Profile.username",
          "ProfilePayload.profile",
          "User.email",
          "User.profile",
          "User.token",
          "User.username",
          "UserPayload.user");

  private static final String INTROSPECTION =
      "{ __schema { queryType { name } mutationType { name }"
          + " types { name kind fields { name } possibleTypes { name } inputFields { name } } } }";

  @Test
  void sdl_parses_and_declares_every_query_and_mutation_field() throws Exception {
    TypeDefinitionRegistry registry;
    try (InputStreamReader reader =
        new InputStreamReader(
            new ClassPathResource("schema/schema.graphqls").getInputStream(),
            StandardCharsets.UTF_8)) {
      registry = new SchemaParser().parse(reader);
    }

    assertThat(fieldNames(registry, "Query")).containsExactlyElementsOf(QUERY_FIELDS);
    assertThat(fieldNames(registry, "Mutation")).containsExactlyElementsOf(MUTATION_FIELDS);
    assertThat(registry.getType("UserResult")).isPresent();
  }

  @Test
  void executable_schema_exposes_every_query_and_mutation_field() {
    ExecutionResult result = execute(INTROSPECTION);
    Map<String, Object> schema = data(result, "__schema");
    List<Map<String, Object>> types = typesOf(schema);

    assertThat(((Map<?, ?>) schema.get("queryType")).get("name")).isEqualTo("Query");
    assertThat(((Map<?, ?>) schema.get("mutationType")).get("name")).isEqualTo("Mutation");
    assertThat(fieldNames(types, "Query")).containsExactlyElementsOf(QUERY_FIELDS);
    assertThat(fieldNames(types, "Mutation")).containsExactlyElementsOf(MUTATION_FIELDS);
  }

  @Test
  void executable_schema_matches_type_field_snapshot() {
    ExecutionResult result = execute(INTROSPECTION);
    List<Map<String, Object>> types = typesOf(data(result, "__schema"));

    List<String> snapshot = new ArrayList<>();
    for (Map<String, Object> type : types) {
      String name = (String) type.get("name");
      if (name.startsWith("_")
          || !"OBJECT".equals(type.get("kind"))
          || name.equals("Query")
          || name.equals("Mutation")) {
        continue;
      }
      for (String field : names(type.get("fields"))) {
        snapshot.add(name + "." + field);
      }
    }
    assertThat(snapshot).containsExactlyElementsOf(TYPE_FIELD_SNAPSHOT);
  }

  @Test
  void executable_schema_keeps_union_and_input_types() {
    ExecutionResult result = execute(INTROSPECTION);
    List<Map<String, Object>> types = typesOf(data(result, "__schema"));

    Map<String, Object> userResult = typeNamed(types, "UserResult");
    assertThat(userResult.get("kind")).isEqualTo("UNION");
    assertThat(names(userResult.get("possibleTypes"))).containsExactly("Error", "UserPayload");

    assertThat(names(typeNamed(types, "CreateUserInput").get("inputFields")))
        .containsExactly("email", "password", "username");
    assertThat(names(typeNamed(types, "UpdateUserInput").get("inputFields")))
        .containsExactly("bio", "email", "image", "password", "username");
    assertThat(names(typeNamed(types, "CreateArticleInput").get("inputFields")))
        .containsExactly("body", "description", "tagList", "title");
    assertThat(names(typeNamed(types, "UpdateArticleInput").get("inputFields")))
        .containsExactly("body", "description", "title");
  }

  private static List<String> fieldNames(TypeDefinitionRegistry registry, String typeName) {
    ObjectTypeDefinition type =
        registry
            .getType(typeName, ObjectTypeDefinition.class)
            .orElseThrow(() -> new AssertionError("missing type " + typeName));
    return type.getFieldDefinitions().stream()
        .map(FieldDefinition::getName)
        .sorted()
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> typesOf(Map<String, Object> schema) {
    return (List<Map<String, Object>>) schema.get("types");
  }

  private static Map<String, Object> typeNamed(List<Map<String, Object>> types, String name) {
    return types.stream()
        .filter(t -> name.equals(t.get("name")))
        .findFirst()
        .orElseThrow(() -> new AssertionError("missing type " + name));
  }

  private static List<String> fieldNames(List<Map<String, Object>> types, String typeName) {
    return names(typeNamed(types, typeName).get("fields"));
  }

  @SuppressWarnings("unchecked")
  private static List<String> names(Object fields) {
    if (fields == null) {
      return List.of();
    }
    return ((List<Map<String, Object>>) fields)
        .stream()
            .map(f -> (String) f.get("name"))
            .filter(n -> !n.startsWith("_"))
            .sorted()
            .collect(Collectors.toList());
  }
}
