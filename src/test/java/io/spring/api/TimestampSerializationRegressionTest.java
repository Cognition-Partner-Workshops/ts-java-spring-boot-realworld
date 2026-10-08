package io.spring.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.JacksonCustomizations;
import java.time.Instant;
import java.util.Collections;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class TimestampSerializationRegressionTest {
  private final ObjectMapper mapper =
      new ObjectMapper().registerModule(new JacksonCustomizations().realWorldModules());

  @Test
  public void should_serialize_instant_as_iso8601_utc_with_milliseconds() throws Exception {
    Instant instant = Instant.parse("2024-01-05T12:34:56.789Z");
    assertEquals("\"2024-01-05T12:34:56.789Z\"", mapper.writeValueAsString(instant));
  }

  @Test
  public void should_always_print_three_fraction_digits() throws Exception {
    assertEquals(
        "\"2024-01-05T12:34:56.000Z\"",
        mapper.writeValueAsString(Instant.parse("2024-01-05T12:34:56Z")));
    assertEquals(
        "\"2024-01-05T12:34:56.100Z\"",
        mapper.writeValueAsString(Instant.parse("2024-01-05T12:34:56.1Z")));
    assertEquals(
        "\"2024-01-05T12:34:56.123Z\"",
        mapper.writeValueAsString(Instant.parse("2024-01-05T12:34:56.123456789Z")));
  }

  @Test
  public void should_match_legacy_joda_format() throws Exception {
    long epochMillis = 1704458096789L;
    String legacy =
        mapper.writeValueAsString(new DateTime(epochMillis, DateTimeZone.forOffsetHours(8)));
    String modern = mapper.writeValueAsString(Instant.ofEpochMilli(epochMillis));
    assertEquals(legacy, modern);
    assertEquals("\"2024-01-05T12:34:56.789Z\"", modern);
  }

  @Test
  public void should_serialize_null_timestamp_as_null() throws Exception {
    assertEquals(
        "{\"createdAt\":null}",
        mapper.writeValueAsString(Collections.singletonMap("createdAt", (Instant) null)));
  }
}
