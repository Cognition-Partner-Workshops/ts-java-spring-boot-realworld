package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.TagsQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CorsAndHeadersRegressionTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockitoBean private TagsQueryService tagsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_answer_cors_preflight_with_configured_origin_methods_and_headers()
      throws Exception {
    given()
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", "POST")
        .header("Access-Control-Request-Headers", "Authorization, Content-Type")
        .when()
        .options("/articles")
        .then()
        .statusCode(200)
        .header("Access-Control-Allow-Origin", equalTo("*"))
        .header("Access-Control-Allow-Methods", containsString("POST"))
        .header("Access-Control-Allow-Methods", containsString("DELETE"))
        .header("Access-Control-Allow-Headers", containsString("Authorization"))
        .header("Access-Control-Allow-Headers", containsString("Content-Type"))
        .header("Access-Control-Allow-Credentials", nullValue());
  }

  @Test
  public void should_reject_cors_preflight_for_disallowed_method() throws Exception {
    given()
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", "TRACE")
        .when()
        .options("/articles")
        .then()
        .statusCode(403);
  }

  @Test
  public void should_reject_cors_preflight_for_disallowed_request_header() throws Exception {
    given()
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", "GET")
        .header("Access-Control-Request-Headers", "X-Custom-Header")
        .when()
        .options("/tags")
        .then()
        .statusCode(403);
  }

  @Test
  public void should_expose_allow_origin_on_cross_origin_requests() throws Exception {
    given()
        .header("Origin", "http://localhost:3000")
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .header("Access-Control-Allow-Origin", equalTo("*"));
  }

  @Test
  public void should_send_security_headers_on_api_responses() throws Exception {
    given()
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .header("X-Content-Type-Options", equalTo("nosniff"))
        .header("X-Frame-Options", equalTo("DENY"))
        .header("Cache-Control", equalTo("no-cache, no-store, max-age=0, must-revalidate"))
        .header("Pragma", equalTo("no-cache"))
        .header("Expires", equalTo("0"))
        .header("X-XSS-Protection", equalTo("0"));
  }

  @Test
  public void should_send_security_headers_on_401_responses() throws Exception {
    given()
        .when()
        .get("/user")
        .then()
        .statusCode(401)
        .header("X-Content-Type-Options", equalTo("nosniff"))
        .header("X-Frame-Options", equalTo("DENY"))
        .header("Cache-Control", containsString("no-store"));
  }
}
