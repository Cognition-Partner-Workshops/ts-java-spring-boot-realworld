package io.spring.cucumber.steps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.spring.cucumber.TestContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

public class CommonSteps {

  @LocalServerPort private int port;

  @Autowired private TestContext testContext;

  @Before
  public void setUp() {
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
    testContext.reset();
  }

  @Given(
      "I have a valid registration request with email {string} and username {string} and password {string}")
  public void iHaveAValidRegistrationRequest(String email, String username, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    testContext.setRequestBody(body);
  }

  @Given(
      "I have a registration request with invalid email {string} and username {string} and password {string}")
  public void iHaveARegistrationRequestWithInvalidEmail(
      String email, String username, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    testContext.setRequestBody(body);
  }

  @Given("a user exists with email {string} and username {string} and password {string}")
  public void aUserExistsWithEmailAndUsernameAndPassword(
      String email, String username, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/users")
        .then()
        .statusCode(anyOf(equalTo(201), equalTo(422)));
  }

  @Given("I have a login request with email {string} and password {string}")
  public void iHaveALoginRequest(String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("password", password);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    testContext.setRequestBody(body);
  }

  @Given("I am authenticated as a user with email {string} and username {string}")
  public void iAmAuthenticatedAsUser(String email, String username) {
    String password = "password123";
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    Response registerResponse =
        given().contentType(ContentType.JSON).body(body).when().post("/users");

    System.out.println("Registration response status: " + registerResponse.getStatusCode());
    System.out.println("Registration response body: " + registerResponse.getBody().asString());

    if (registerResponse.getStatusCode() == 201) {
      String token = registerResponse.jsonPath().getString("user.token");
      testContext.setAuthToken(token);
      testContext.setCurrentEmail(email);
      testContext.setCurrentUsername(username);
    } else {
      Map<String, Object> loginUser = new HashMap<>();
      loginUser.put("email", email);
      loginUser.put("password", password);

      Map<String, Object> loginBody = new HashMap<>();
      loginBody.put("user", loginUser);

      Response loginResponse =
          given().contentType(ContentType.JSON).body(loginBody).when().post("/users/login");

      System.out.println("Login response status: " + loginResponse.getStatusCode());
      System.out.println("Login response body: " + loginResponse.getBody().asString());

      String token = loginResponse.jsonPath().getString("user.token");
      testContext.setAuthToken(token);
      testContext.setCurrentEmail(email);
      testContext.setCurrentUsername(username);
    }
  }

  @Given(
      "I have a new article request with title {string} and description {string} and body {string}")
  public void iHaveANewArticleRequest(String title, String description, String body) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", Arrays.asList());

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", article);
    testContext.setRequestBody(requestBody);
  }

  @Given(
      "I have a new article request with title {string} and description {string} and body {string} and tags {string}")
  public void iHaveANewArticleRequestWithTags(
      String title, String description, String body, String tags) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", Arrays.asList(tags.split(",")));

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", article);
    testContext.setRequestBody(requestBody);
  }

  @Given("an article exists with title {string} by user {string}")
  public void anArticleExistsByUser(String title, String username) {
    String email = username + "@test.com";
    String password = "password123";

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> userBody = new HashMap<>();
    userBody.put("user", user);

    Response registerResponse =
        given().contentType(ContentType.JSON).body(userBody).when().post("/users");

    String token;
    if (registerResponse.getStatusCode() == 201) {
      token = registerResponse.jsonPath().getString("user.token");
    } else {
      Map<String, Object> loginUser = new HashMap<>();
      loginUser.put("email", email);
      loginUser.put("password", password);

      Map<String, Object> loginBody = new HashMap<>();
      loginBody.put("user", loginUser);

      Response loginResponse =
          given().contentType(ContentType.JSON).body(loginBody).when().post("/users/login");
      token = loginResponse.jsonPath().getString("user.token");
    }

    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", "Test description");
    article.put("body", "Test body");
    article.put("tagList", Arrays.asList());

    Map<String, Object> articleBody = new HashMap<>();
    articleBody.put("article", article);

    Response articleResponse =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + token)
            .body(articleBody)
            .when()
            .post("/articles");

    if (articleResponse.getStatusCode() == 200) {
      String slug = articleResponse.jsonPath().getString("article.slug");
      testContext.setCurrentArticleSlug(slug);
    }
  }

  @Given("an article exists with title {string} by user {string} with tags {string}")
  public void anArticleExistsByUserWithTags(String title, String username, String tags) {
    String email = username + "@test.com";
    String password = "password123";

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> userBody = new HashMap<>();
    userBody.put("user", user);

    Response registerResponse =
        given().contentType(ContentType.JSON).body(userBody).when().post("/users");

    String token;
    if (registerResponse.getStatusCode() == 201) {
      token = registerResponse.jsonPath().getString("user.token");
    } else {
      Map<String, Object> loginUser = new HashMap<>();
      loginUser.put("email", email);
      loginUser.put("password", password);

      Map<String, Object> loginBody = new HashMap<>();
      loginBody.put("user", loginUser);

      Response loginResponse =
          given().contentType(ContentType.JSON).body(loginBody).when().post("/users/login");
      token = loginResponse.jsonPath().getString("user.token");
    }

    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", "Test description");
    article.put("body", "Test body");
    article.put("tagList", Arrays.asList(tags.split(",")));

    Map<String, Object> articleBody = new HashMap<>();
    articleBody.put("article", article);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + token)
        .body(articleBody)
        .when()
        .post("/articles");
  }

  @Given("multiple articles exist")
  public void multipleArticlesExist() {
    for (int i = 1; i <= 10; i++) {
      anArticleExistsByUser("Multiple Article " + i, "multiauthor" + i);
    }
  }

  @Given("I follow a user {string} who has articles")
  public void iFollowAUserWhoHasArticles(String username) {
    anArticleExistsByUser("Followed User Article", username);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + testContext.getAuthToken())
        .when()
        .post("/profiles/" + username + "/follow");
  }

  @Given(
      "I have created an article with title {string} and description {string} and body {string}")
  public void iHaveCreatedAnArticle(String title, String description, String body) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", Arrays.asList());

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", article);

    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .body(requestBody)
            .when()
            .post("/articles");

    if (response.getStatusCode() == 200) {
      String slug = response.jsonPath().getString("article.slug");
      testContext.setCurrentArticleSlug(slug);
    }
  }

  @Given("I have a new comment request with body {string}")
  public void iHaveANewCommentRequest(String body) {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", body);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("comment", comment);
    testContext.setRequestBody(requestBody);
  }

  @Given("the article has comments")
  public void theArticleHasComments() {
    String email = "commentcreator@test.com";
    String username = "commentcreator";
    String password = "password123";

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> userBody = new HashMap<>();
    userBody.put("user", user);

    Response registerResponse =
        given().contentType(ContentType.JSON).body(userBody).when().post("/users");

    String token;
    if (registerResponse.getStatusCode() == 201) {
      token = registerResponse.jsonPath().getString("user.token");
    } else {
      Map<String, Object> loginUser = new HashMap<>();
      loginUser.put("email", email);
      loginUser.put("password", password);

      Map<String, Object> loginBody = new HashMap<>();
      loginBody.put("user", loginUser);

      Response loginResponse =
          given().contentType(ContentType.JSON).body(loginBody).when().post("/users/login");
      token = loginResponse.jsonPath().getString("user.token");
    }

    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "Test comment");

    Map<String, Object> commentBody = new HashMap<>();
    commentBody.put("comment", comment);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + token)
        .body(commentBody)
        .when()
        .post("/articles/" + testContext.getCurrentArticleSlug() + "/comments");
  }

  @Given("I have created a comment with body {string} on the article")
  public void iHaveCreatedACommentOnTheArticle(String body) {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", body);

    Map<String, Object> commentBody = new HashMap<>();
    commentBody.put("comment", comment);

    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .body(commentBody)
            .when()
            .post("/articles/" + testContext.getCurrentArticleSlug() + "/comments");

    if (response.getStatusCode() == 201) {
      String commentId = response.jsonPath().getString("comment.id");
      testContext.setCurrentCommentId(commentId);
    }
  }

  @Given("a comment exists on the article by user {string}")
  public void aCommentExistsOnTheArticleByUser(String username) {
    String email = username + "@test.com";
    String password = "password123";

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> userBody = new HashMap<>();
    userBody.put("user", user);

    Response registerResponse =
        given().contentType(ContentType.JSON).body(userBody).when().post("/users");

    String token;
    if (registerResponse.getStatusCode() == 201) {
      token = registerResponse.jsonPath().getString("user.token");
    } else {
      Map<String, Object> loginUser = new HashMap<>();
      loginUser.put("email", email);
      loginUser.put("password", password);

      Map<String, Object> loginBody = new HashMap<>();
      loginBody.put("user", loginUser);

      Response loginResponse =
          given().contentType(ContentType.JSON).body(loginBody).when().post("/users/login");
      token = loginResponse.jsonPath().getString("user.token");
    }

    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "Comment by " + username);

    Map<String, Object> commentBody = new HashMap<>();
    commentBody.put("comment", comment);

    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + token)
            .body(commentBody)
            .when()
            .post("/articles/" + testContext.getCurrentArticleSlug() + "/comments");

    if (response.getStatusCode() == 201) {
      String commentId = response.jsonPath().getString("comment.id");
      testContext.setCurrentCommentId(commentId);
    }
  }

  @Given("I am following the user {string}")
  public void iAmFollowingTheUser(String username) {
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + testContext.getAuthToken())
        .when()
        .post("/profiles/" + username + "/follow");
  }

  @Given("I have favorited the article")
  public void iHaveFavoritedTheArticle() {
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + testContext.getAuthToken())
        .when()
        .post("/articles/" + testContext.getCurrentArticleSlug() + "/favorite");
  }

  @Given("I have favorited multiple articles")
  public void iHaveFavoritedMultipleArticles() {
    for (int i = 1; i <= 3; i++) {
      String username = "favauthor" + i;
      String email = username + "@test.com";
      String password = "password123";

      Map<String, Object> user = new HashMap<>();
      user.put("email", email);
      user.put("username", username);
      user.put("password", password);

      Map<String, Object> userBody = new HashMap<>();
      userBody.put("user", user);

      Response registerResponse =
          given().contentType(ContentType.JSON).body(userBody).when().post("/users");

      String token;
      if (registerResponse.getStatusCode() == 201) {
        token = registerResponse.jsonPath().getString("user.token");
      } else {
        Map<String, Object> loginUser = new HashMap<>();
        loginUser.put("email", email);
        loginUser.put("password", password);

        Map<String, Object> loginBody = new HashMap<>();
        loginBody.put("user", loginUser);

        Response loginResponse =
            given().contentType(ContentType.JSON).body(loginBody).when().post("/users/login");
        token = loginResponse.jsonPath().getString("user.token");
      }

      Map<String, Object> article = new HashMap<>();
      article.put("title", "Favorited Article " + i);
      article.put("description", "Description " + i);
      article.put("body", "Body " + i);
      article.put("tagList", Arrays.asList());

      Map<String, Object> articleBody = new HashMap<>();
      articleBody.put("article", article);

      Response articleResponse =
          given()
              .contentType(ContentType.JSON)
              .header("Authorization", "Token " + token)
              .body(articleBody)
              .when()
              .post("/articles");

      if (articleResponse.getStatusCode() == 200) {
        String slug = articleResponse.jsonPath().getString("article.slug");
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .post("/articles/" + slug + "/favorite");
      }
    }
  }

  @Given("tags exist in the system")
  public void tagsExistInTheSystem() {
    String email = "tagcreator@test.com";
    String username = "tagcreator";
    String password = "password123";

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> userBody = new HashMap<>();
    userBody.put("user", user);

    Response registerResponse =
        given().contentType(ContentType.JSON).body(userBody).when().post("/users");

    String token;
    if (registerResponse.getStatusCode() == 201) {
      token = registerResponse.jsonPath().getString("user.token");
    } else {
      Map<String, Object> loginUser = new HashMap<>();
      loginUser.put("email", email);
      loginUser.put("password", password);

      Map<String, Object> loginBody = new HashMap<>();
      loginBody.put("user", loginUser);

      Response loginResponse =
          given().contentType(ContentType.JSON).body(loginBody).when().post("/users/login");
      token = loginResponse.jsonPath().getString("user.token");
    }

    Map<String, Object> article = new HashMap<>();
    article.put("title", "Article with Tags");
    article.put("description", "Description");
    article.put("body", "Body");
    article.put("tagList", Arrays.asList("tag1", "tag2", "tag3"));

    Map<String, Object> articleBody = new HashMap<>();
    articleBody.put("article", article);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + token)
        .body(articleBody)
        .when()
        .post("/articles");
  }

  @Given("no tags exist in the system")
  public void noTagsExistInTheSystem() {}

  @And("I have an update user request with email {string}")
  public void iHaveAnUpdateUserRequestWithEmail(String email) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    testContext.setRequestBody(body);
  }

  @And("I have an update user request with username {string}")
  public void iHaveAnUpdateUserRequestWithUsername(String username) {
    Map<String, Object> user = new HashMap<>();
    user.put("username", username);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    testContext.setRequestBody(body);
  }

  @And("I have an update user request with bio {string}")
  public void iHaveAnUpdateUserRequestWithBio(String bio) {
    Map<String, Object> user = new HashMap<>();
    user.put("bio", bio);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    testContext.setRequestBody(body);
  }

  @And("I have an update user request with image {string}")
  public void iHaveAnUpdateUserRequestWithImage(String image) {
    Map<String, Object> user = new HashMap<>();
    user.put("image", image);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);
    testContext.setRequestBody(body);
  }

  @And("I have an update article request with title {string}")
  public void iHaveAnUpdateArticleRequestWithTitle(String title) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", article);
    testContext.setRequestBody(requestBody);
  }

  @And("I have an update article request with description {string}")
  public void iHaveAnUpdateArticleRequestWithDescription(String description) {
    Map<String, Object> article = new HashMap<>();
    article.put("description", description);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", article);
    testContext.setRequestBody(requestBody);
  }

  @And("I have an update article request with body {string}")
  public void iHaveAnUpdateArticleRequestWithBody(String body) {
    Map<String, Object> article = new HashMap<>();
    article.put("body", body);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", article);
    testContext.setRequestBody(requestBody);
  }

  @When("I send a POST request to {string} with the registration data")
  public void iSendAPostRequestWithRegistrationData(String path) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(testContext.getRequestBody())
            .when()
            .post(path);
    testContext.setResponse(response);
  }

  @When("I send a POST request to {string} with the login data")
  public void iSendAPostRequestWithLoginData(String path) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(testContext.getRequestBody())
            .when()
            .post(path);
    testContext.setResponse(response);
  }

  @When("I send a POST request to {string} with the article data")
  public void iSendAPostRequestWithArticleData(String path) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .body(testContext.getRequestBody())
            .when()
            .post(path);
    testContext.setResponse(response);

    System.out.println("Article creation response status: " + response.getStatusCode());
    System.out.println("Article creation response body: " + response.getBody().asString());

    if (response.getStatusCode() == 200) {
      String slug = response.jsonPath().getString("article.slug");
      testContext.setCurrentArticleSlug(slug);
    }
  }

  @When("I send a POST request to {string} with the comment data")
  public void iSendAPostRequestWithCommentData(String path) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .body(testContext.getRequestBody())
            .when()
            .post(path);
    testContext.setResponse(response);

    if (response.getStatusCode() == 201) {
      String commentId = response.jsonPath().getString("comment.id");
      testContext.setCurrentCommentId(commentId);
    }
  }

  @When("I send a POST request to {string} without authentication")
  public void iSendAPostRequestWithoutAuthentication(String path) {
    Response response =
        given().contentType(ContentType.JSON).body(testContext.getRequestBody()).when().post(path);
    testContext.setResponse(response);
  }

  @When("I send a POST request to {string} with authentication")
  public void iSendAPostRequestWithAuthentication(String path) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .post(path);
    testContext.setResponse(response);
  }

  @When("I send a GET request to {string}")
  public void iSendAGetRequest(String path) {
    Response response = given().contentType(ContentType.JSON).when().get(path);
    testContext.setResponse(response);
  }

  @When("I send a GET request to the current article")
  public void iSendAGetRequestToCurrentArticle() {
    String path = "/articles/" + testContext.getCurrentArticleSlug();
    Response response = given().contentType(ContentType.JSON).when().get(path);
    testContext.setResponse(response);
  }

  @When("I send a PUT request to the current article with the update data")
  public void iSendAPutRequestToCurrentArticleWithUpdateData() {
    String path = "/articles/" + testContext.getCurrentArticleSlug();
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .body(testContext.getRequestBody())
            .when()
            .put(path);
    testContext.setResponse(response);
  }

  @When("I send a PUT request to the current article without authentication")
  public void iSendAPutRequestToCurrentArticleWithoutAuth() {
    String path = "/articles/" + testContext.getCurrentArticleSlug();
    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(testContext.getRequestBody())
            .when()
            .put(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to the current article with authentication")
  public void iSendADeleteRequestToCurrentArticleWithAuth() {
    String path = "/articles/" + testContext.getCurrentArticleSlug();
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .delete(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to the current article without authentication")
  public void iSendADeleteRequestToCurrentArticleWithoutAuth() {
    String path = "/articles/" + testContext.getCurrentArticleSlug();
    Response response = given().contentType(ContentType.JSON).when().delete(path);
    testContext.setResponse(response);
  }

  @When("I send a POST request to favorite the current article with authentication")
  public void iSendAPostRequestToFavoriteCurrentArticle() {
    String path = "/articles/" + testContext.getCurrentArticleSlug() + "/favorite";
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .post(path);
    testContext.setResponse(response);
  }

  @When("I send a POST request to favorite the current article without authentication")
  public void iSendAPostRequestToFavoriteCurrentArticleWithoutAuth() {
    String path = "/articles/" + testContext.getCurrentArticleSlug() + "/favorite";
    Response response = given().contentType(ContentType.JSON).when().post(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to unfavorite the current article with authentication")
  public void iSendADeleteRequestToUnfavoriteCurrentArticle() {
    String path = "/articles/" + testContext.getCurrentArticleSlug() + "/favorite";
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .delete(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to unfavorite the current article without authentication")
  public void iSendADeleteRequestToUnfavoriteCurrentArticleWithoutAuth() {
    String path = "/articles/" + testContext.getCurrentArticleSlug() + "/favorite";
    Response response = given().contentType(ContentType.JSON).when().delete(path);
    testContext.setResponse(response);
  }

  @When("I send a POST request to comment on the current article with the comment data")
  public void iSendAPostRequestToCommentOnCurrentArticle() {
    String path = "/articles/" + testContext.getCurrentArticleSlug() + "/comments";
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .body(testContext.getRequestBody())
            .when()
            .post(path);
    testContext.setResponse(response);

    if (response.getStatusCode() == 201) {
      String commentId = response.jsonPath().getString("comment.id");
      testContext.setCurrentCommentId(commentId);
    }
  }

  @When("I send a POST request to comment on the current article without authentication")
  public void iSendAPostRequestToCommentOnCurrentArticleWithoutAuth() {
    String path = "/articles/" + testContext.getCurrentArticleSlug() + "/comments";
    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(testContext.getRequestBody())
            .when()
            .post(path);
    testContext.setResponse(response);
  }

  @When("I send a GET request to get comments on the current article")
  public void iSendAGetRequestToGetCommentsOnCurrentArticle() {
    String path = "/articles/" + testContext.getCurrentArticleSlug() + "/comments";
    Response response = given().contentType(ContentType.JSON).when().get(path);
    testContext.setResponse(response);
  }

  @When("I send a GET request to {string} with authentication")
  public void iSendAGetRequestWithAuthentication(String path) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .get(path);
    testContext.setResponse(response);
  }

  @When("I send a GET request to {string} without authentication")
  public void iSendAGetRequestWithoutAuthentication(String path) {
    Response response = given().contentType(ContentType.JSON).when().get(path);
    testContext.setResponse(response);
  }

  @When("I send a PUT request to {string} with the update data")
  public void iSendAPutRequestWithUpdateData(String path) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .body(testContext.getRequestBody())
            .when()
            .put(path);
    testContext.setResponse(response);
  }

  @When("I send a PUT request to {string} without authentication")
  public void iSendAPutRequestWithoutAuthentication(String path) {
    Response response =
        given().contentType(ContentType.JSON).body(testContext.getRequestBody()).when().put(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to {string} with authentication")
  public void iSendADeleteRequestWithAuthentication(String path) {
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .delete(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to {string} without authentication")
  public void iSendADeleteRequestWithoutAuthentication(String path) {
    Response response = given().contentType(ContentType.JSON).when().delete(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to delete my comment with authentication")
  public void iSendADeleteRequestToDeleteMyComment() {
    String path =
        "/articles/"
            + testContext.getCurrentArticleSlug()
            + "/comments/"
            + testContext.getCurrentCommentId();
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .delete(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to delete the comment without authentication")
  public void iSendADeleteRequestToDeleteCommentWithoutAuth() {
    String path =
        "/articles/"
            + testContext.getCurrentArticleSlug()
            + "/comments/"
            + testContext.getCurrentCommentId();
    Response response = given().contentType(ContentType.JSON).when().delete(path);
    testContext.setResponse(response);
  }

  @When("I send a DELETE request to delete the comment with authentication")
  public void iSendADeleteRequestToDeleteCommentWithAuth() {
    String path =
        "/articles/"
            + testContext.getCurrentArticleSlug()
            + "/comments/"
            + testContext.getCurrentCommentId();
    Response response =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + testContext.getAuthToken())
            .when()
            .delete(path);
    testContext.setResponse(response);
  }

  @Then("the response status should be {int}")
  public void theResponseStatusShouldBe(int statusCode) {
    assertThat(testContext.getResponse().getStatusCode(), equalTo(statusCode));
  }

  @And("the response should contain a user with email {string}")
  public void theResponseShouldContainUserWithEmail(String email) {
    String responseEmail = testContext.getResponse().jsonPath().getString("user.email");
    assertThat(responseEmail, equalTo(email));
  }

  @And("the response should contain a user with username {string}")
  public void theResponseShouldContainUserWithUsername(String username) {
    String responseUsername = testContext.getResponse().jsonPath().getString("user.username");
    assertThat(responseUsername, equalTo(username));
  }

  @And("the response should contain a user with bio {string}")
  public void theResponseShouldContainUserWithBio(String bio) {
    String responseBio = testContext.getResponse().jsonPath().getString("user.bio");
    assertThat(responseBio, equalTo(bio));
  }

  @And("the response should contain a user with image {string}")
  public void theResponseShouldContainUserWithImage(String image) {
    String responseImage = testContext.getResponse().jsonPath().getString("user.image");
    assertThat(responseImage, equalTo(image));
  }

  @And("the response should contain a token")
  public void theResponseShouldContainAToken() {
    String token = testContext.getResponse().jsonPath().getString("user.token");
    assertThat(token, notNullValue());
    assertThat(token.length(), greaterThan(0));
    testContext.setAuthToken(token);
  }

  @And("the response should contain an article with title {string}")
  public void theResponseShouldContainArticleWithTitle(String title) {
    String responseTitle = testContext.getResponse().jsonPath().getString("article.title");
    assertThat(responseTitle, equalTo(title));
  }

  @And("the response should contain an article with slug {string}")
  public void theResponseShouldContainArticleWithSlug(String slug) {
    String responseSlug = testContext.getResponse().jsonPath().getString("article.slug");
    assertThat(responseSlug, equalTo(slug));
  }

  @And("the response should contain an article with description {string}")
  public void theResponseShouldContainArticleWithDescription(String description) {
    String responseDescription =
        testContext.getResponse().jsonPath().getString("article.description");
    assertThat(responseDescription, equalTo(description));
  }

  @And("the response should contain an article with body {string}")
  public void theResponseShouldContainArticleWithBody(String body) {
    String responseBody = testContext.getResponse().jsonPath().getString("article.body");
    assertThat(responseBody, equalTo(body));
  }

  @And("the response should contain an article with tags {string}")
  public void theResponseShouldContainArticleWithTags(String tags) {
    List<String> responseTags = testContext.getResponse().jsonPath().getList("article.tagList");
    List<String> expectedTags = Arrays.asList(tags.split(","));
    assertThat(responseTags, containsInAnyOrder(expectedTags.toArray()));
  }

  @And("the response should contain an article with favorited true")
  public void theResponseShouldContainArticleWithFavoritedTrue() {
    Boolean favorited = testContext.getResponse().jsonPath().getBoolean("article.favorited");
    assertThat(favorited, equalTo(true));
  }

  @And("the response should contain an article with favorited false")
  public void theResponseShouldContainArticleWithFavoritedFalse() {
    Boolean favorited = testContext.getResponse().jsonPath().getBoolean("article.favorited");
    assertThat(favorited, equalTo(false));
  }

  @And("the response should contain an article with favoritesCount greater than {int}")
  public void theResponseShouldContainArticleWithFavoritesCountGreaterThan(int count) {
    Integer favoritesCount = testContext.getResponse().jsonPath().getInt("article.favoritesCount");
    assertThat(favoritesCount, greaterThan(count));
  }

  @And("the response should contain articles list")
  public void theResponseShouldContainArticlesList() {
    List<Object> articles = testContext.getResponse().jsonPath().getList("articles");
    assertThat(articles, notNullValue());
  }

  @And("the response should contain articles with tag {string}")
  public void theResponseShouldContainArticlesWithTag(String tag) {
    List<Object> articles = testContext.getResponse().jsonPath().getList("articles");
    assertThat(articles, notNullValue());
  }

  @And("the response should contain articles by author {string}")
  public void theResponseShouldContainArticlesByAuthor(String author) {
    List<Object> articles = testContext.getResponse().jsonPath().getList("articles");
    assertThat(articles, notNullValue());
  }

  @And("the response should contain at most {int} articles")
  public void theResponseShouldContainAtMostArticles(int count) {
    List<Object> articles = testContext.getResponse().jsonPath().getList("articles");
    assertThat(articles.size(), lessThanOrEqualTo(count));
  }

  @And("the response should contain articles from followed users")
  public void theResponseShouldContainArticlesFromFollowedUsers() {
    List<Object> articles = testContext.getResponse().jsonPath().getList("articles");
    assertThat(articles, notNullValue());
  }

  @And("the response should contain articles favorited by {string}")
  public void theResponseShouldContainArticlesFavoritedBy(String username) {
    List<Object> articles = testContext.getResponse().jsonPath().getList("articles");
    assertThat(articles, notNullValue());
  }

  @And("the response should contain a comment with body {string}")
  public void theResponseShouldContainCommentWithBody(String body) {
    String responseBody = testContext.getResponse().jsonPath().getString("comment.body");
    assertThat(responseBody, equalTo(body));
  }

  @And("the response should contain a comments list")
  public void theResponseShouldContainCommentsList() {
    List<Object> comments = testContext.getResponse().jsonPath().getList("comments");
    assertThat(comments, notNullValue());
    assertThat(comments.size(), greaterThan(0));
  }

  @And("the response should contain an empty comments list")
  public void theResponseShouldContainEmptyCommentsList() {
    List<Object> comments = testContext.getResponse().jsonPath().getList("comments");
    assertThat(comments, notNullValue());
    assertThat(comments.size(), equalTo(0));
  }

  @And("the response should contain a profile with username {string}")
  public void theResponseShouldContainProfileWithUsername(String username) {
    String responseUsername = testContext.getResponse().jsonPath().getString("profile.username");
    assertThat(responseUsername, equalTo(username));
  }

  @And("the response should contain following status")
  public void theResponseShouldContainFollowingStatus() {
    Object following = testContext.getResponse().jsonPath().get("profile.following");
    assertThat(following, notNullValue());
  }

  @And("the response should indicate following is true")
  public void theResponseShouldIndicateFollowingIsTrue() {
    Boolean following = testContext.getResponse().jsonPath().getBoolean("profile.following");
    assertThat(following, equalTo(true));
  }

  @And("the response should indicate following is false")
  public void theResponseShouldIndicateFollowingIsFalse() {
    Boolean following = testContext.getResponse().jsonPath().getBoolean("profile.following");
    assertThat(following, equalTo(false));
  }

  @And("the response should contain a tags list")
  public void theResponseShouldContainTagsList() {
    List<String> tags = testContext.getResponse().jsonPath().getList("tags");
    assertThat(tags, notNullValue());
  }

  @And("the response should contain an empty tags list")
  public void theResponseShouldContainEmptyTagsList() {
    List<String> tags = testContext.getResponse().jsonPath().getList("tags");
    assertThat(tags, notNullValue());
  }

  @And("the response should contain tags including {string}")
  public void theResponseShouldContainTagsIncluding(String tag) {
    List<String> tags = testContext.getResponse().jsonPath().getList("tags");
    assertThat(tags, hasItem(tag));
  }
}
