package io.spring;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UtilTest {

  @Test
  void isEmpty_withNull() {
    assertTrue(Util.isEmpty(null));
  }

  @Test
  void isEmpty_withEmptyString() {
    assertTrue(Util.isEmpty(""));
  }

  @Test
  void isEmpty_withNonEmptyString() {
    assertFalse(Util.isEmpty("hello"));
  }

  @Test
  void isEmpty_withWhitespace() {
    assertFalse(Util.isEmpty(" "));
  }
}
