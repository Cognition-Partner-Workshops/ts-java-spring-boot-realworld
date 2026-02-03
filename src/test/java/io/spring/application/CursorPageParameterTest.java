package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  void constructor_withValidParams() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.NEXT);

    assertEquals("cursor", param.getCursor());
    assertEquals(10, param.getLimit());
    assertEquals(Direction.NEXT, param.getDirection());
  }

  @Test
  void isNext_withNextDirection() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.NEXT);

    assertTrue(param.isNext());
  }

  @Test
  void isNext_withPrevDirection() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.PREV);

    assertFalse(param.isNext());
  }

  @Test
  void getQueryLimit_returnsLimitPlusOne() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.NEXT);

    assertEquals(11, param.getQueryLimit());
  }

  @Test
  void setLimit_exceedsMaxLimit() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 2000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
  }

  @Test
  void setLimit_negativeValue() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", -5, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void setLimit_zeroValue() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 0, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void noArgsConstructor() {
    CursorPageParameter<String> param = new CursorPageParameter<>();

    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
    assertNull(param.getDirection());
  }
}
