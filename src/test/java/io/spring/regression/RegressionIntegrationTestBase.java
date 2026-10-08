package io.spring.regression;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Boots the full application on a random port against a fresh, file based SQLite database (so every
 * HikariCP connection sees the same data) with all Flyway migrations (V1 schema + V2 seed) applied.
 * Contract tests drive the real HTTP stack with REST-Assured, so they survive the Boot 2 -> 3
 * migration untouched as long as the external contract is preserved.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class RegressionIntegrationTestBase {

  public static final String SEED_PASSWORD = "password123";
  public static final String SEED_USER_JOHN = "johndoe";
  public static final String SEED_EMAIL_JOHN = "john@example.com";
  public static final String SEED_USER_JANE = "janedoe";
  public static final String SEED_EMAIL_JANE = "jane@example.com";
  public static final String SEED_USER_BOB = "bobsmith";
  public static final String SEED_EMAIL_BOB = "bob@example.com";
  public static final String SEED_SLUG_SPRING_BOOT = "getting-started-with-spring-boot";
  public static final String SEED_SLUG_REST = "rest-api-best-practices";

  @DynamicPropertySource
  static void freshSqliteDatabase(DynamicPropertyRegistry registry) {
    try {
      Path dir = Paths.get("build", "regression-db");
      Files.createDirectories(dir);
      Path db = dir.resolve("regression-" + UUID.randomUUID() + ".db");
      db.toFile().deleteOnExit();
      registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db.toAbsolutePath());
      registry.add("spring.flyway.target", () -> "latest");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Value("${local.server.port}")
  protected int port;

  @BeforeEach
  void configureRestAssured() {
    RestAssured.port = port;
    RestAssured.basePath = "";
  }

  protected RequestSpecification json() {
    return given().contentType(ContentType.JSON).accept(ContentType.JSON);
  }

  protected RequestSpecification authed(String token) {
    return json().header("Authorization", "Token " + token);
  }

  protected String login(String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("password", password);
    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    Response response = json().body(body).post("/users/login");
    response.then().statusCode(200);
    return response.jsonPath().getString("user.token");
  }

  protected String johnToken() {
    return login(SEED_EMAIL_JOHN, SEED_PASSWORD);
  }

  protected String bobToken() {
    return login(SEED_EMAIL_BOB, SEED_PASSWORD);
  }

  protected String janeToken() {
    return login(SEED_EMAIL_JANE, SEED_PASSWORD);
  }

  protected String uniqueName(String prefix) {
    return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
  }

  protected Response register(String email, String username, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);
    return json().body(envelope("user", user)).post("/users");
  }

  protected String registerAndLogin(String username) {
    register(username + "@regression.test", username, SEED_PASSWORD).then().statusCode(201);
    return login(username + "@regression.test", SEED_PASSWORD);
  }

  protected Response createArticle(String token, String title, String... tags) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", "description of " + title);
    article.put("body", "body of " + title);
    article.put("tagList", java.util.Arrays.asList(tags));
    return authed(token).body(envelope("article", article)).post("/articles");
  }

  protected String createArticleSlug(String token, String title, String... tags) {
    Response response = createArticle(token, title, tags);
    response.then().statusCode(200);
    return response.jsonPath().getString("article.slug");
  }

  protected static Map<String, Object> envelope(String root, Object value) {
    Map<String, Object> body = new HashMap<>();
    body.put(root, value);
    return body;
  }

  protected static Map<String, Object> map(Object... kv) {
    Map<String, Object> m = new HashMap<>();
    for (int i = 0; i < kv.length; i += 2) {
      m.put((String) kv[i], kv[i + 1]);
    }
    return m;
  }
}
