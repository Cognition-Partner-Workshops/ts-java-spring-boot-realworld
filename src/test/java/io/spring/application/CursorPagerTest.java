package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  void constructor_nextDirection_withExtra() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertEquals(data, pager.getData());
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void constructor_nextDirection_withoutExtra() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertEquals(data, pager.getData());
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void constructor_prevDirection_withExtra() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, true);

    assertEquals(data, pager.getData());
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void constructor_prevDirection_withoutExtra() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, false);

    assertEquals(data, pager.getData());
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void getStartCursor_withData() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
  }

  @Test
  void getStartCursor_emptyData() {
    List<ArticleData> data = Collections.emptyList();
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNull(pager.getStartCursor());
  }

  @Test
  void getEndCursor_withData() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNotNull(pager.getEndCursor());
  }

  @Test
  void getEndCursor_emptyData() {
    List<ArticleData> data = Collections.emptyList();
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNull(pager.getEndCursor());
  }

  @Test
  void isNext_getter() {
    List<ArticleData> data = createArticleDataList(1);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertTrue(pager.isNext());
  }

  @Test
  void isPrevious_getter() {
    List<ArticleData> data = createArticleDataList(1);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, true);

    assertTrue(pager.isPrevious());
  }

  private List<ArticleData> createArticleDataList(int count) {
    List<ArticleData> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      ArticleData article = new ArticleData();
      article.setId("id-" + i);
      article.setUpdatedAt(new DateTime());
      list.add(article);
    }
    return list;
  }
}
