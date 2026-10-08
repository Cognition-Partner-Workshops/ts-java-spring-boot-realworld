package io.spring.application.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.JacksonCustomizations;
import io.spring.Util;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimestampJsonFormatTest {
  private static final Instant INSTANT = Instant.parse("2016-02-18T03:22:56.637Z");

  private final ObjectMapper objectMapper =
      new ObjectMapper().registerModule(new JacksonCustomizations.RealWorldModules());

  @Test
  public void should_format_timestamps_as_iso_utc_with_milliseconds() {
    Assertions.assertEquals("2016-02-18T03:22:56.637Z", Util.formatTimestamp(INSTANT));
    Assertions.assertEquals(
        "2016-02-18T03:22:56.000Z", Util.formatTimestamp(Instant.parse("2016-02-18T03:22:56Z")));
    Assertions.assertEquals(
        "2016-02-18T03:22:56.637Z",
        Util.formatTimestamp(Instant.parse("2016-02-18T03:22:56.637123Z")));
    Assertions.assertNull(Util.formatTimestamp(null));
  }

  @Test
  public void should_serialize_article_timestamps_with_project_jackson_module() throws Exception {
    ArticleData article =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            INSTANT,
            INSTANT.plusMillis(1),
            Arrays.asList("java"),
            new ProfileData("uid", "user", "", "", false));

    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(article));

    Assertions.assertEquals("2016-02-18T03:22:56.637Z", json.get("createdAt").asText());
    Assertions.assertEquals("2016-02-18T03:22:56.638Z", json.get("updatedAt").asText());
    Assertions.assertEquals("user", json.get("author").get("username").asText());
    Assertions.assertTrue(
        json.get("createdAt")
            .asText()
            .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));
  }

  @Test
  public void should_serialize_comment_timestamps_with_project_jackson_module() throws Exception {
    CommentData comment =
        new CommentData(
            "cid", "body", "aid", INSTANT, INSTANT, new ProfileData("uid", "user", "", "", false));

    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(comment));

    Assertions.assertEquals("2016-02-18T03:22:56.637Z", json.get("createdAt").asText());
    Assertions.assertNull(json.get("articleId"));
  }

  @Test
  public void should_truncate_now_to_milliseconds() {
    Instant now = Util.now();
    Assertions.assertEquals(0, now.getNano() % 1_000_000);
    Assertions.assertEquals(now, Instant.ofEpochMilli(now.toEpochMilli()));
  }
}
