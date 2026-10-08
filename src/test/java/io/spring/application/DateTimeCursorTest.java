package io.spring.application;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateTimeCursorTest {
  private static final Instant INSTANT = Instant.parse("2016-02-18T03:22:56.637Z");

  @Test
  public void should_render_cursor_as_epoch_millis_string() {
    Assertions.assertEquals("1455765776637", new DateTimeCursor(INSTANT).toString());
    Assertions.assertEquals(INSTANT, new DateTimeCursor(INSTANT).getData());
  }

  @Test
  public void should_parse_cursor_back_to_same_instant() {
    Assertions.assertEquals(INSTANT, DateTimeCursor.parse(new DateTimeCursor(INSTANT).toString()));
    Assertions.assertEquals(Instant.EPOCH, DateTimeCursor.parse("0"));
  }

  @Test
  public void should_parse_null_cursor_as_null() {
    Assertions.assertNull(DateTimeCursor.parse(null));
  }

  @Test
  public void should_reject_invalid_cursor() {
    Assertions.assertThrows(NumberFormatException.class, () -> DateTimeCursor.parse("abc"));
    Assertions.assertThrows(NumberFormatException.class, () -> DateTimeCursor.parse(""));
    Assertions.assertThrows(
        NumberFormatException.class, () -> DateTimeCursor.parse("2016-02-18T03:22:56.637Z"));
  }

  @Test
  public void should_expose_start_and_end_cursors_from_data() {
    ArticleData first = articleAt(INSTANT.plusMillis(2));
    ArticleData last = articleAt(INSTANT.plusMillis(1));
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(first, last), Direction.NEXT, true);

    Assertions.assertEquals(
        String.valueOf(INSTANT.plusMillis(2).toEpochMilli()), pager.getStartCursor().toString());
    Assertions.assertEquals(
        String.valueOf(INSTANT.plusMillis(1).toEpochMilli()), pager.getEndCursor().toString());
    Assertions.assertTrue(pager.hasNext());
    Assertions.assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_have_null_cursors_and_no_pages_for_empty_result() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    Assertions.assertNull(pager.getStartCursor());
    Assertions.assertNull(pager.getEndCursor());
    Assertions.assertFalse(pager.hasNext());
    Assertions.assertFalse(pager.hasPrevious());
    Assertions.assertTrue(pager.getData().isEmpty());
  }

  @Test
  public void should_flag_previous_page_only_for_prev_direction() {
    List<ArticleData> data = Arrays.asList(articleAt(INSTANT));
    CursorPager<ArticleData> prevWithExtra = new CursorPager<>(data, Direction.PREV, true);
    CursorPager<ArticleData> prevWithoutExtra = new CursorPager<>(data, Direction.PREV, false);

    Assertions.assertTrue(prevWithExtra.hasPrevious());
    Assertions.assertFalse(prevWithExtra.hasNext());
    Assertions.assertFalse(prevWithoutExtra.hasPrevious());
    Assertions.assertFalse(prevWithoutExtra.hasNext());
  }

  @Test
  public void should_clamp_page_limit_and_request_one_extra_row() {
    Assertions.assertEquals(20, new CursorPageParameter<>(null, 0, Direction.NEXT).getLimit());
    Assertions.assertEquals(20, new CursorPageParameter<>(null, -5, Direction.NEXT).getLimit());
    Assertions.assertEquals(7, new CursorPageParameter<>(null, 7, Direction.NEXT).getLimit());
    Assertions.assertEquals(1000, new CursorPageParameter<>(null, 5000, Direction.NEXT).getLimit());
    Assertions.assertEquals(8, new CursorPageParameter<>(null, 7, Direction.NEXT).getQueryLimit());

    CursorPageParameter<Instant> next = new CursorPageParameter<>(INSTANT, 7, Direction.NEXT);
    Assertions.assertTrue(next.isNext());
    Assertions.assertEquals(INSTANT, next.getCursor());
    Assertions.assertFalse(new CursorPageParameter<>(INSTANT, 7, Direction.PREV).isNext());
  }

  private static ArticleData articleAt(Instant updatedAt) {
    ArticleData data = new ArticleData();
    data.setId(updatedAt.toString());
    data.setCreatedAt(updatedAt);
    data.setUpdatedAt(updatedAt);
    return data;
  }
}
