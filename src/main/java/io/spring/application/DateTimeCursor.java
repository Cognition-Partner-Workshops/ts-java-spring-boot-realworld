package io.spring.application;

import java.time.Instant;

/** Cursor over a UTC {@link Instant}; the wire format stays the epoch-millis string. */
public class DateTimeCursor extends PageCursor<Instant> {

  public DateTimeCursor(Instant data) {
    super(data);
  }

  @Override
  public String toString() {
    return String.valueOf(getData().toEpochMilli());
  }

  public static Instant parse(String cursor) {
    if (cursor == null) {
      return null;
    }
    return Instant.ofEpochMilli(Long.parseLong(cursor));
  }
}
