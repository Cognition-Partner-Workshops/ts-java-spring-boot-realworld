package io.spring.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared fixture for GraphQL regression tests: boots the full application (DGS + Spring GraphQL +
 * MyBatis/SQLite) and executes operations in-process through {@link DgsQueryExecutor}.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class GraphQLTestBase {

  protected static final String PASSWORD = "password123";

  @Autowired protected DgsQueryExecutor dgsQueryExecutor;
  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;
  @Autowired protected UserRepository userRepository;
  @Autowired protected ArticleRepository articleRepository;
  @Autowired protected JwtService jwtService;
  @Autowired protected PasswordEncoder passwordEncoder;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  protected static String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  protected User newUser() {
    return newUser("Test bio", "https://example.com/avatar.png");
  }

  protected User newUser(String bio, String image) {
    String username = unique("gql-user");
    User user =
        new User(username + "@example.com", username, passwordEncoder.encode(PASSWORD), bio, image);
    userRepository.save(user);
    return user;
  }

  protected Article newArticle(User author, String... tags) {
    String title = unique("gql-article");
    Article article =
        new Article(title, "desc " + title, "body " + title, List.of(tags), author.getId());
    articleRepository.save(article);
    return article;
  }

  protected void authenticateAs(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
  }

  protected void anonymous() {
    SecurityContextHolder.clearContext();
  }

  protected String tokenHeader(User user) {
    return "Token " + jwtService.toToken(user);
  }

  protected ExecutionResult execute(String query) {
    return execute(query, Collections.emptyMap());
  }

  protected ExecutionResult execute(String query, Map<String, Object> variables) {
    return dgsQueryExecutor.execute(query, variables);
  }

  @SuppressWarnings("unchecked")
  protected <T> T data(ExecutionResult result, String... path) {
    if (!result.getErrors().isEmpty()) {
      throw new AssertionError("unexpected GraphQL errors: " + result.getErrors());
    }
    Object current = result.getData();
    for (String segment : path) {
      if (current == null) {
        return null;
      }
      current = ((Map<String, Object>) current).get(segment);
    }
    return (T) current;
  }

  protected GraphQLError singleError(ExecutionResult result) {
    List<GraphQLError> errors = result.getErrors();
    if (errors.size() != 1) {
      throw new AssertionError("expected exactly one GraphQL error but got: " + errors);
    }
    return errors.get(0);
  }

  protected String errorType(GraphQLError error) {
    Map<String, Object> extensions = error.getExtensions();
    return extensions == null ? null : String.valueOf(extensions.get("errorType"));
  }

  protected Map<String, Object> vars(Object... keyValues) {
    Map<String, Object> map = new HashMap<>();
    for (int i = 0; i < keyValues.length; i += 2) {
      map.put((String) keyValues[i], keyValues[i + 1]);
    }
    return map;
  }
}
