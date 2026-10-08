package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;

class GraphQLTimeFormatTest {

  private static final Instant INSTANT = Instant.parse("2024-03-05T07:08:09.123Z");

  @Test
  void formats_instant_as_iso_8601_utc_with_milliseconds() {
    assertThat(GraphQLTimeFormat.format(INSTANT)).isEqualTo("2024-03-05T07:08:09.123Z");
  }

  @Test
  void always_prints_three_fraction_digits() {
    assertThat(GraphQLTimeFormat.format(Instant.parse("2024-03-05T07:08:09Z")))
        .isEqualTo("2024-03-05T07:08:09.000Z");
  }

  @Test
  void normalizes_offset_date_times_to_utc() {
    OffsetDateTime shifted = INSTANT.atOffset(ZoneOffset.ofHours(8));
    assertThat(GraphQLTimeFormat.format(shifted)).isEqualTo("2024-03-05T07:08:09.123Z");
  }

  @Test
  void accepts_java_util_date() {
    assertThat(GraphQLTimeFormat.format(Date.from(INSTANT))).isEqualTo("2024-03-05T07:08:09.123Z");
  }

  @Test
  void accepts_legacy_instants_exposing_get_millis() {
    Object legacy =
        new Object() {
          @SuppressWarnings("unused")
          public long getMillis() {
            return INSTANT.toEpochMilli();
          }
        };
    assertThat(GraphQLTimeFormat.format(legacy)).isEqualTo("2024-03-05T07:08:09.123Z");
  }

  @Test
  void falls_back_to_parsing_to_string() {
    Object printable =
        new Object() {
          @Override
          public String toString() {
            return "2024-03-05T15:08:09.123+08:00";
          }
        };
    assertThat(GraphQLTimeFormat.format(printable)).isEqualTo("2024-03-05T07:08:09.123Z");
  }

  @Test
  void null_stays_null_and_garbage_is_rejected() {
    assertThat(GraphQLTimeFormat.format(null)).isNull();
    assertThatThrownBy(() -> GraphQLTimeFormat.format("not a timestamp"))
        .isInstanceOf(java.time.format.DateTimeParseException.class);
  }
}
