package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CursorPageParameterTest {

  @Test
  void constructor_withValidParameters() {
    DateTime cursor = DateTime.now();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);

    assertEquals(cursor, param.getCursor());
    assertEquals(10, param.getLimit());
    assertEquals(Direction.NEXT, param.getDirection());
  }

  @Test
  void constructor_withLimitExceedingMax() {
    DateTime cursor = DateTime.now();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 2000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
  }

  @Test
  void constructor_withZeroLimit() {
    DateTime cursor = DateTime.now();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 0, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void constructor_withNegativeLimit() {
    DateTime cursor = DateTime.now();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, -5, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void isNext_withNextDirection() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(DateTime.now(), 10, Direction.NEXT);

    assertTrue(param.isNext());
  }

  @Test
  void isNext_withPrevDirection() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(DateTime.now(), 10, Direction.PREV);

    assertFalse(param.isNext());
  }

  @Test
  void getQueryLimit_returnsLimitPlusOne() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(DateTime.now(), 10, Direction.NEXT);

    assertEquals(11, param.getQueryLimit());
  }

  @Test
  void noArgsConstructor_setsDefaults() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();

    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
    assertNull(param.getDirection());
  }
}
