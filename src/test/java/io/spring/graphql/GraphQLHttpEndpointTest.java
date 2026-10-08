package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

/** Exercises the HTTP surface (/graphql + /graphiql) through the real JWT filter chain. */
class GraphQLHttpEndpointTest extends GraphQLTestBase {

  @Test
  void graphql_endpoint_answers_anonymous_tags_query() throws Exception {
    String tag = unique("http-tag");
    newArticle(newUser(), tag);

    mockMvc
        .perform(graphql("{ tags }", Map.of(), null))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").doesNotExist())
        .andExpect(jsonPath("$.data.tags", Matchers.hasItem(tag)));
  }

  @Test
  void me_is_null_without_authorization_header() throws Exception {
    mockMvc
        .perform(graphql("{ me { username } }", Map.of(), null))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").doesNotExist())
        .andExpect(jsonPath("$.data.me").value(Matchers.nullValue()));
  }

  @Test
  void me_is_null_with_invalid_token() throws Exception {
    mockMvc
        .perform(graphql("{ me { username } }", Map.of(), "Token not.a.jwt"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.me").value(Matchers.nullValue()));
  }

  @Test
  void valid_token_header_authenticates_me_and_echoes_token() throws Exception {
    User user = newUser();
    String header = tokenHeader(user);
    mockMvc
        .perform(graphql("{ me { username email token } }", Map.of(), header))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors").doesNotExist())
        .andExpect(jsonPath("$.data.me.username").value(user.getUsername()))
        .andExpect(jsonPath("$.data.me.email").value(user.getEmail()))
        .andExpect(jsonPath("$.data.me.token").value(header.substring("Token ".length())));
  }

  @Test
  void mutation_without_token_is_unauthenticated_over_http() throws Exception {
    mockMvc
        .perform(
            graphql(
                "mutation($input: CreateArticleInput!) { createArticle(input: $input) { article { slug } } }",
                Map.of("input", Map.of("title", unique("http"), "description", "d", "body", "b")),
                null))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.createArticle").value(Matchers.nullValue()))
        .andExpect(jsonPath("$.errors[0].extensions.errorType").value("UNAUTHENTICATED"))
        .andExpect(jsonPath("$.errors[0].path[0]").value("createArticle"));
  }

  @Test
  void mutation_with_token_header_succeeds_over_http() throws Exception {
    User user = newUser();
    String title = unique("http article");
    MvcResult result =
        mockMvc
            .perform(
                graphql(
                    "mutation($input: CreateArticleInput!) { createArticle(input: $input) {"
                        + " article { slug title author { username } } } }",
                    Map.of("input", Map.of("title", title, "description", "d", "body", "b")),
                    tokenHeader(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").doesNotExist())
            .andExpect(jsonPath("$.data.createArticle.article.title").value(title))
            .andExpect(
                jsonPath("$.data.createArticle.article.author.username").value(user.getUsername()))
            .andReturn();

    JsonNode body =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(result.getResponse().getContentAsString());
    String slug = body.at("/data/createArticle/article/slug").asText();
    assertThat(articleRepository.findBySlug(slug)).isPresent();
  }

  @Test
  void not_found_and_bad_request_errors_are_typed_over_http() throws Exception {
    User author = newUser();
    Article existing = newArticle(author);

    mockMvc
        .perform(
            graphql(
                "query($slug: String!) { article(slug: $slug) { slug } }",
                Map.of("slug", unique("missing")),
                null))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors[0].extensions.errorType").value("NOT_FOUND"))
        .andExpect(jsonPath("$.data.article").value(Matchers.nullValue()));

    mockMvc
        .perform(
            graphql(
                "mutation($input: CreateArticleInput!) { createArticle(input: $input) { article { slug } } }",
                Map.of(
                    "input", Map.of("title", existing.getTitle(), "description", "d", "body", "b")),
                tokenHeader(author)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errors[0].extensions.errorType").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.errors[0].extensions.title").isArray());
  }

  @Test
  void graphiql_ui_is_served() throws Exception {
    MockHttpServletResponse redirectOrPage =
        mockMvc.perform(get("/graphiql")).andReturn().getResponse();
    assertThat(redirectOrPage.getStatus()).isIn(200, 302, 303, 307, 308);

    MockHttpServletResponse page =
        mockMvc
            .perform(get("/graphiql").param("path", "/graphql"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertThat(page.getContentType()).contains("text/html");
    assertThat(page.getContentAsString().toLowerCase()).contains("graphiql");
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder graphql(
      String query, Map<String, Object> variables, String authorization) throws Exception {
    var builder =
        post("/graphql")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(
                objectMapper.writeValueAsString(Map.of("query", query, "variables", variables)));
    if (authorization != null) {
      builder = builder.header(HttpHeaders.AUTHORIZATION, authorization);
    }
    return builder;
  }
}
