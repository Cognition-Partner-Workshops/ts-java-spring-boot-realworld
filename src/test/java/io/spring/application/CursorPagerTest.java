package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private static class TestNode implements Node {
    private final String id;

    public TestNode(String id) {
      this.id = id;
    }

    @Override
    public PageCursor getCursor() {
      return new TestPageCursor(id);
    }
  }

  private static class TestPageCursor extends PageCursor<String> {
    public TestPageCursor(String data) {
      super(data);
    }
  }

  @Test
  public void should_create_cursor_pager_with_next_direction_and_has_extra() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, true);

    assertThat(pager.getData(), is(data));
    assertThat(pager.hasNext(), is(true));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_create_cursor_pager_with_next_direction_and_no_extra() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_create_cursor_pager_with_prev_direction_and_has_extra() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.PREV, true);

    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(true));
  }

  @Test
  public void should_create_cursor_pager_with_prev_direction_and_no_extra() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.PREV, false);

    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_return_start_cursor_from_first_element() {
    List<TestNode> data = Arrays.asList(new TestNode("first"), new TestNode("last"));
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getStartCursor().getData(), is("first"));
  }

  @Test
  public void should_return_end_cursor_from_last_element() {
    List<TestNode> data = Arrays.asList(new TestNode("first"), new TestNode("last"));
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getEndCursor().getData(), is("last"));
  }

  @Test
  public void should_return_null_start_cursor_for_empty_data() {
    List<TestNode> data = new ArrayList<>();
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getStartCursor(), is(nullValue()));
  }

  @Test
  public void should_return_null_end_cursor_for_empty_data() {
    List<TestNode> data = new ArrayList<>();
    CursorPager<TestNode> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);

    assertThat(pager.getEndCursor(), is(nullValue()));
  }

  @Test
  public void should_have_next_and_prev_direction_enum_values() {
    assertThat(CursorPager.Direction.NEXT.name(), is("NEXT"));
    assertThat(CursorPager.Direction.PREV.name(), is("PREV"));
  }

  @Test
  public void should_return_is_next_true_for_next_direction() {
    assertThat(CursorPager.Direction.valueOf("NEXT"), is(CursorPager.Direction.NEXT));
    assertThat(CursorPager.Direction.valueOf("PREV"), is(CursorPager.Direction.PREV));
  }
}
