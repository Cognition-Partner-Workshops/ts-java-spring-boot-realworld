package io.spring;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

public class UtilTest {

  @Test
  public void should_return_true_for_null() {
    assertThat(Util.isEmpty(null), is(true));
  }

  @Test
  public void should_return_true_for_empty_string() {
    assertThat(Util.isEmpty(""), is(true));
  }

  @Test
  public void should_return_false_for_non_empty_string() {
    assertThat(Util.isEmpty("hello"), is(false));
  }

  @Test
  public void should_return_false_for_whitespace() {
    assertThat(Util.isEmpty(" "), is(false));
  }

  @Test
  public void should_instantiate_util_class() throws Exception {
    Constructor<Util> constructor = Util.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    Util util = constructor.newInstance();
    assertThat(util, is(notNullValue()));
  }
}
