package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {

  @Test
  public void should_create_date_time_cursor_with_datetime() {
    DateTime now = DateTime.now();
    DateTimeCursor cursor = new DateTimeCursor(now);
    
    assertThat(cursor.getData(), is(now));
  }

  @Test
  public void should_return_millis_as_string_in_toString() {
    DateTime dateTime = new DateTime(1609459200000L, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(dateTime);
    
    assertThat(cursor.toString(), is("1609459200000"));
  }

  @Test
  public void should_parse_cursor_string_to_datetime() {
    DateTime result = DateTimeCursor.parse("1609459200000");
    
    assertThat(result, notNullValue());
    assertThat(result.getMillis(), is(1609459200000L));
    assertThat(result.getZone(), is(DateTimeZone.UTC));
  }

  @Test
  public void should_return_null_when_parsing_null_cursor() {
    DateTime result = DateTimeCursor.parse(null);
    
    assertThat(result, nullValue());
  }

  @Test
  public void should_roundtrip_datetime_through_cursor() {
    DateTime original = new DateTime(1609459200000L, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(original);
    DateTime parsed = DateTimeCursor.parse(cursor.toString());
    
    assertThat(parsed.getMillis(), is(original.getMillis()));
  }

  @Test
  public void should_handle_current_time() {
    DateTime now = DateTime.now();
    DateTimeCursor cursor = new DateTimeCursor(now);
    DateTime parsed = DateTimeCursor.parse(cursor.toString());
    
    assertThat(parsed.getMillis(), is(now.getMillis()));
  }
}
