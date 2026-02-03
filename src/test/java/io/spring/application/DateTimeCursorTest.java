package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  void constructor_withDateTime() {
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertEquals(dateTime, cursor.getData());
  }

  @Test
  void toString_returnsMillis() {
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertEquals(String.valueOf(dateTime.getMillis()), cursor.toString());
  }

  @Test
  void parse_withValidCursor() {
    DateTime original = new DateTime(2023, 1, 15, 10, 30, 0);
    String cursorString = String.valueOf(original.getMillis());

    DateTime parsed = DateTimeCursor.parse(cursorString);

    assertNotNull(parsed);
    assertEquals(original.getMillis(), parsed.getMillis());
  }

  @Test
  void parse_withNull() {
    DateTime parsed = DateTimeCursor.parse(null);

    assertNull(parsed);
  }
}
