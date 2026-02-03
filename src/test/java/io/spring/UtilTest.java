package io.spring;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UtilTest {

  @Test
  public void should_return_true_when_value_is_null() {
    assertTrue(Util.isEmpty(null));
  }

  @Test
  public void should_return_true_when_value_is_empty_string() {
    assertTrue(Util.isEmpty(""));
  }

  @Test
  public void should_return_false_when_value_is_not_empty() {
    assertFalse(Util.isEmpty("test"));
  }

  @Test
  public void should_return_false_when_value_has_whitespace() {
    assertFalse(Util.isEmpty(" "));
  }

  @Test
  public void should_return_false_when_value_has_content() {
    assertFalse(Util.isEmpty("hello world"));
  }
}
