package io.spring.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.JacksonCustomizations;
import java.time.Instant;
import java.util.Collections;
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
    // Joda ISODateTimeFormat.dateTime().withZoneUTC() printed this instant (+08:00 source zone) as:
    long epochMillis = 1704458096789L;
    String legacy = "\"2024-01-05T12:34:56.789Z\"";
    assertEquals(legacy, mapper.writeValueAsString(Instant.ofEpochMilli(epochMillis)));
  }

  @Test
  public void should_serialize_null_timestamp_as_null() throws Exception {
    assertEquals(
        "{\"createdAt\":null}",
        mapper.writeValueAsString(Collections.singletonMap("createdAt", (Instant) null)));
  }
}
