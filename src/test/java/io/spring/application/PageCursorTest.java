package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class PageCursorTest {

  private static class TestPageCursor extends PageCursor<String> {
    public TestPageCursor(String data) {
      super(data);
    }
  }

  @Test
  public void should_create_page_cursor_with_data() {
    TestPageCursor cursor = new TestPageCursor("test-data");

    assertThat(cursor.getData(), is("test-data"));
  }

  @Test
  public void should_return_data_as_string() {
    TestPageCursor cursor = new TestPageCursor("cursor-value");

    assertThat(cursor.toString(), is("cursor-value"));
  }

  @Test
  public void should_handle_numeric_data() {
    PageCursor<Integer> cursor =
        new PageCursor<Integer>(42) {
          // Anonymous subclass
        };

    assertThat(cursor.getData(), is(42));
    assertThat(cursor.toString(), is("42"));
  }
}
