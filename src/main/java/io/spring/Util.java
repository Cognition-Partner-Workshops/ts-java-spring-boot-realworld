package io.spring;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Util {
  /** RealWorld timestamp wire format: ISO-8601 UTC with fixed millisecond precision. */
  public static final DateTimeFormatter ISO_UTC_MILLIS =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").withZone(ZoneOffset.UTC);

  public static boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }

  /** Current time truncated to milliseconds (the precision persisted and exposed by the API). */
  public static Instant now() {
    return Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }

  public static String formatTimestamp(Instant instant) {
    return instant == null ? null : ISO_UTC_MILLIS.format(instant);
  }
}
