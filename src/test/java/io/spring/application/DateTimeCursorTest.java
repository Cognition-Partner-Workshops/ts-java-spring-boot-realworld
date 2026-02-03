package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

class DateTimeCursorTest {

  @Test
  void constructor_createsWithDateTime() {
    DateTime dateTime = DateTime.now();
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertNotNull(cursor);
    assertEquals(dateTime, cursor.getData());
  }

  @Test
  void toString_returnsMillisAsString() {
    DateTime dateTime = new DateTime(1234567890000L, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertEquals("1234567890000", cursor.toString());
  }

  @Test
  void parse_withValidCursor() {
    String cursorString = "1234567890000";

    DateTime result = DateTimeCursor.parse(cursorString);

    assertNotNull(result);
    assertEquals(1234567890000L, result.getMillis());
    assertEquals(DateTimeZone.UTC, result.getZone());
  }

  @Test
  void parse_withNullCursor() {
    DateTime result = DateTimeCursor.parse(null);

    assertNull(result);
  }

  @Test
  void roundTrip_preservesDateTime() {
    DateTime original = DateTime.now(DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(original);
    String cursorString = cursor.toString();

    DateTime parsed = DateTimeCursor.parse(cursorString);

    assertEquals(original.getMillis(), parsed.getMillis());
  }
}
