package io.spring.api.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building consistent API responses across REST endpoints. This provides a
 * unified way to wrap response data in the expected format (e.g., {"article": {...}}).
 */
public final class ApiResponse {

  private ApiResponse() {}

  /**
   * Wraps a single object in a response map with the specified key.
   *
   * @param key the key to use in the response map
   * @param value the value to wrap
   * @return a map containing the key-value pair
   */
  public static Map<String, Object> wrap(String key, Object value) {
    Map<String, Object> response = new HashMap<>();
    response.put(key, value);
    return response;
  }

  /**
   * Creates an article response wrapper.
   *
   * @param articleData the article data to wrap
   * @return a map with "article" as the key
   */
  public static Map<String, Object> article(Object articleData) {
    return wrap("article", articleData);
  }

  /**
   * Creates a user response wrapper.
   *
   * @param userData the user data to wrap
   * @return a map with "user" as the key
   */
  public static Map<String, Object> user(Object userData) {
    return wrap("user", userData);
  }

  /**
   * Creates a profile response wrapper.
   *
   * @param profileData the profile data to wrap
   * @return a map with "profile" as the key
   */
  public static Map<String, Object> profile(Object profileData) {
    return wrap("profile", profileData);
  }

  /**
   * Creates a comment response wrapper.
   *
   * @param commentData the comment data to wrap
   * @return a map with "comment" as the key
   */
  public static Map<String, Object> comment(Object commentData) {
    return wrap("comment", commentData);
  }

  /**
   * Creates a comments list response wrapper.
   *
   * @param comments the list of comments to wrap
   * @return a map with "comments" as the key
   */
  public static Map<String, Object> comments(Object comments) {
    return wrap("comments", comments);
  }

  /**
   * Creates a tags response wrapper.
   *
   * @param tags the list of tags to wrap
   * @return a map with "tags" as the key
   */
  public static Map<String, Object> tags(Object tags) {
    return wrap("tags", tags);
  }
}
