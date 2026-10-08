package io.spring.regression.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.JacksonCustomizations;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserParam;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.regression.RegressionFixtures;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;

/** Jackson contract: UTC ISO-8601 timestamps with millis, root unwrapping, null handling. */
@JsonTest
@Import(JacksonCustomizations.class)
class JsonSerializationTest {

  @Autowired ObjectMapper objectMapper;

  private final ObjectMapper reader = new ObjectMapper();

  private JsonNode tree(Object value) throws Exception {
    return reader.readTree(objectMapper.writeValueAsString(value));
  }

  private final User author = new User("a@b.com", "author", "pw", null, null);

  @Test
  void timestamps_are_utc_iso8601_with_millis_and_z_suffix() throws Exception {
    Article article =
        new Article(
            "T",
            "d",
            "b",
            Collections.emptyList(),
            author.getId(),
            RegressionFixtures.atMillis(1700000000123L));
    ArticleData data = RegressionFixtures.articleData(article, author);
    JsonNode json = tree(data);
    assertEquals("2023-11-14T22:13:20.123Z", json.get("createdAt").asText());
    assertEquals("2023-11-14T22:13:20.123Z", json.get("updatedAt").asText());
    assertTrue(json.get("createdAt").isTextual());
  }

  @Test
  void epoch_and_zero_millis_are_still_rendered_with_three_fraction_digits() throws Exception {
    CommentData comment = RegressionFixtures.commentData("c", "a", author);
    comment.setCreatedAt(RegressionFixtures.atMillis(0L));
    comment.setUpdatedAt(RegressionFixtures.atMillis(1000L));
    JsonNode json = tree(comment);
    assertEquals("1970-01-01T00:00:00.000Z", json.get("createdAt").asText());
    assertEquals("1970-01-01T00:00:01.000Z", json.get("updatedAt").asText());
    assertEquals("c", json.get("id").asText());
    assertNull(json.get("articleId"), "articleId is @JsonIgnore'd");
    assertEquals("author", json.get("author").get("username").asText());
  }

  @Test
  void article_data_json_shape_uses_author_key_and_hides_internal_fields() throws Exception {
    ArticleData data =
        RegressionFixtures.articleData(RegressionFixtures.article("s", author), author);
    JsonNode json = tree(data);
    assertTrue(json.has("author"));
    assertFalse(json.has("profileData"));
    assertTrue(json.get("tagList").isArray());
    assertTrue(json.get("favorited").isBoolean());
    assertTrue(json.get("favoritesCount").isInt());
    assertFalse(json.get("author").has("id"));
    assertTrue(json.get("author").has("following"));

    JsonNode list = tree(new ArticleDataList(Arrays.asList(data), 1));
    assertTrue(list.get("articles").isArray());
    assertEquals(1, list.get("articlesCount").asInt());
    assertFalse(list.has("articleDatas"));
    assertFalse(list.has("count"));
  }

  @Test
  void null_bio_and_image_are_serialized_as_explicit_nulls() throws Exception {
    ProfileData profile = new ProfileData("id", "name", null, null, false);
    JsonNode json = tree(profile);
    assertTrue(json.has("bio"));
    assertTrue(json.get("bio").isNull());
    assertTrue(json.has("image"));
    assertTrue(json.get("image").isNull());
    assertFalse(json.has("id"));

    UserData userData = new UserData("id", "e@e.com", "name", null, null);
    JsonNode userJson = tree(new UserWithToken(userData, "t"));
    assertTrue(userJson.get("bio").isNull());
    assertTrue(userJson.get("image").isNull());
    assertEquals("t", userJson.get("token").asText());
    assertFalse(userJson.has("id"));
    assertFalse(userJson.has("password"));
  }

  @Test
  void request_params_are_root_unwrapped() throws Exception {
    assertTrue(
        objectMapper.isEnabled(
            com.fasterxml.jackson.databind.DeserializationFeature.UNWRAP_ROOT_VALUE));
    RegisterParam register =
        objectMapper.readValue(
            "{\"user\":{\"email\":\"e@e.com\",\"username\":\"u\",\"password\":\"p\"}}",
            RegisterParam.class);
    assertEquals("e@e.com", register.getEmail());
    assertEquals("u", register.getUsername());
    assertEquals("p", register.getPassword());

    UpdateUserParam update =
        objectMapper.readValue("{\"user\":{\"bio\":\"b\"}}", UpdateUserParam.class);
    assertEquals("b", update.getBio());
    assertEquals("", update.getEmail(), "missing fields keep builder defaults");

    NewArticleParam article =
        objectMapper.readValue(
            "{\"article\":{\"title\":\"t\",\"description\":\"d\",\"body\":\"b\",\"tagList\":[\"x\"]}}",
            NewArticleParam.class);
    assertEquals("t", article.getTitle());
    assertEquals(Arrays.asList("x"), article.getTagList());

    UpdateArticleParam updateArticle =
        objectMapper.readValue("{\"article\":{\"title\":\"nt\"}}", UpdateArticleParam.class);
    assertEquals("nt", updateArticle.getTitle());
    assertEquals("", updateArticle.getBody());
  }

  @Test
  void request_without_root_wrapper_is_rejected() {
    org.junit.jupiter.api.Assertions.assertThrows(
        Exception.class,
        () -> objectMapper.readValue("{\"email\":\"e@e.com\"}", RegisterParam.class));
  }
}
