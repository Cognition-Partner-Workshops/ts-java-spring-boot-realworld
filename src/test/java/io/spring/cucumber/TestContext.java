package io.spring.cucumber;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TestContext {
  private Response response;
  private String authToken;
  private String currentUserId;
  private String currentUsername;
  private String currentEmail;
  private String currentArticleSlug;
  private String currentArticleId;
  private String currentCommentId;
  private Map<String, Object> requestBody = new HashMap<>();

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getCurrentUserId() {
    return currentUserId;
  }

  public void setCurrentUserId(String currentUserId) {
    this.currentUserId = currentUserId;
  }

  public String getCurrentUsername() {
    return currentUsername;
  }

  public void setCurrentUsername(String currentUsername) {
    this.currentUsername = currentUsername;
  }

  public String getCurrentEmail() {
    return currentEmail;
  }

  public void setCurrentEmail(String currentEmail) {
    this.currentEmail = currentEmail;
  }

  public String getCurrentArticleSlug() {
    return currentArticleSlug;
  }

  public void setCurrentArticleSlug(String currentArticleSlug) {
    this.currentArticleSlug = currentArticleSlug;
  }

  public String getCurrentArticleId() {
    return currentArticleId;
  }

  public void setCurrentArticleId(String currentArticleId) {
    this.currentArticleId = currentArticleId;
  }

  public String getCurrentCommentId() {
    return currentCommentId;
  }

  public void setCurrentCommentId(String currentCommentId) {
    this.currentCommentId = currentCommentId;
  }

  public Map<String, Object> getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(Map<String, Object> requestBody) {
    this.requestBody = requestBody;
  }

  public void reset() {
    this.response = null;
    this.authToken = null;
    this.currentUserId = null;
    this.currentUsername = null;
    this.currentEmail = null;
    this.currentArticleSlug = null;
    this.currentArticleId = null;
    this.currentCommentId = null;
    this.requestBody = new HashMap<>();
  }
}
