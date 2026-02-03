package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CursorPagerTest {

  private ArticleData createArticleData(String id, DateTime updatedAt) {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image.jpg", false);
    return new ArticleData(
        id,
        "slug-" + id,
        "Title " + id,
        "Description",
        "Body",
        false,
        0,
        DateTime.now(),
        updatedAt,
        Collections.emptyList(),
        profileData);
  }

  @Test
  void constructor_withNextDirection_andHasExtra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1", DateTime.now()));

    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertEquals(1, pager.getData().size());
  }

  @Test
  void constructor_withNextDirection_andNoExtra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1", DateTime.now()));

    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void constructor_withPrevDirection_andHasExtra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1", DateTime.now()));

    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void constructor_withPrevDirection_andNoExtra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1", DateTime.now()));

    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void getStartCursor_withData() {
    DateTime time1 = DateTime.now().minusHours(1);
    DateTime time2 = DateTime.now();
    List<ArticleData> data =
        Arrays.asList(createArticleData("1", time1), createArticleData("2", time2));

    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertEquals(time1.getMillis(), ((DateTimeCursor) pager.getStartCursor()).getData().getMillis());
  }

  @Test
  void getStartCursor_withEmptyData() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
  }

  @Test
  void getEndCursor_withData() {
    DateTime time1 = DateTime.now().minusHours(1);
    DateTime time2 = DateTime.now();
    List<ArticleData> data =
        Arrays.asList(createArticleData("1", time1), createArticleData("2", time2));

    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNotNull(pager.getEndCursor());
    assertEquals(time2.getMillis(), ((DateTimeCursor) pager.getEndCursor()).getData().getMillis());
  }

  @Test
  void getEndCursor_withEmptyData() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getEndCursor());
  }

  @Test
  void getData_returnsCorrectList() {
    List<ArticleData> data =
        Arrays.asList(createArticleData("1", DateTime.now()), createArticleData("2", DateTime.now()));

    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertEquals(2, pager.getData().size());
    assertEquals(data, pager.getData());
  }
}
