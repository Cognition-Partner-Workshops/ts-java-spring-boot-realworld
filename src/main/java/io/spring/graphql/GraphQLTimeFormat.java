package io.spring.graphql;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * Formats timestamps for the GraphQL layer as ISO-8601 UTC strings with millisecond precision (e.g.
 * {@code 2024-01-31T12:34:56.789Z}), matching the historical Joda {@code
 * ISODateTimeFormat.dateTime().withZoneUTC()} output.
 *
 * <p>Accepts {@link Instant} (and other {@code java.time} temporals), {@link Date}, and any legacy
 * instant type exposing {@code getMillis()} so the GraphQL layer compiles and behaves identically
 * before and after the domain model migrates from Joda-Time to {@code java.time}.
 */
public final class GraphQLTimeFormat {

  private static final DateTimeFormatter ISO_UTC_MILLIS =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").withZone(ZoneOffset.UTC);

  private GraphQLTimeFormat() {}

  public static String format(Object timestamp) {
    if (timestamp == null) {
      return null;
    }
    return ISO_UTC_MILLIS.format(toInstant(timestamp));
  }

  static Instant toInstant(Object timestamp) {
    if (timestamp instanceof Instant) {
      return (Instant) timestamp;
    }
    if (timestamp instanceof TemporalAccessor) {
      return Instant.from((TemporalAccessor) timestamp);
    }
    if (timestamp instanceof Date) {
      return ((Date) timestamp).toInstant();
    }
    try {
      Method getMillis = timestamp.getClass().getMethod("getMillis");
      return Instant.ofEpochMilli(((Number) getMillis.invoke(timestamp)).longValue());
    } catch (ReflectiveOperationException | ClassCastException e) {
      return OffsetDateTime.parse(timestamp.toString()).toInstant();
    }
  }
}
