package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  public void should_create_cursor_pager_with_next_direction_and_has_extra() {
    DateTime now = DateTime.now();
    ArticleData article = new ArticleData();
    article.setUpdatedAt(now);
    
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), Direction.NEXT, true);
    
    assertThat(pager.getData().size(), is(1));
    assertThat(pager.hasNext(), is(true));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_create_cursor_pager_with_next_direction_and_no_extra() {
    DateTime now = DateTime.now();
    ArticleData article = new ArticleData();
    article.setUpdatedAt(now);
    
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);
    
    assertThat(pager.getData().size(), is(1));
    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_create_cursor_pager_with_prev_direction_and_has_extra() {
    DateTime now = DateTime.now();
    ArticleData article = new ArticleData();
    article.setUpdatedAt(now);
    
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), Direction.PREV, true);
    
    assertThat(pager.getData().size(), is(1));
    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(true));
  }

  @Test
  public void should_create_cursor_pager_with_prev_direction_and_no_extra() {
    DateTime now = DateTime.now();
    ArticleData article = new ArticleData();
    article.setUpdatedAt(now);
    
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), Direction.PREV, false);
    
    assertThat(pager.getData().size(), is(1));
    assertThat(pager.hasNext(), is(false));
    assertThat(pager.hasPrevious(), is(false));
  }

  @Test
  public void should_return_start_cursor_from_first_element() {
    DateTime now = DateTime.now();
    ArticleData article1 = new ArticleData();
    article1.setUpdatedAt(now);
    ArticleData article2 = new ArticleData();
    article2.setUpdatedAt(now.plusDays(1));
    
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article1, article2), Direction.NEXT, false);
    
    assertThat(pager.getStartCursor(), notNullValue());
  }

  @Test
  public void should_return_end_cursor_from_last_element() {
    DateTime now = DateTime.now();
    ArticleData article1 = new ArticleData();
    article1.setUpdatedAt(now);
    ArticleData article2 = new ArticleData();
    article2.setUpdatedAt(now.plusDays(1));
    
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article1, article2), Direction.NEXT, false);
    
    assertThat(pager.getEndCursor(), notNullValue());
  }

  @Test
  public void should_return_null_start_cursor_for_empty_list() {
    CursorPager<ArticleData> pager = new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    
    assertThat(pager.getStartCursor(), nullValue());
  }

  @Test
  public void should_return_null_end_cursor_for_empty_list() {
    CursorPager<ArticleData> pager = new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    
    assertThat(pager.getEndCursor(), nullValue());
  }

  @Test
  public void should_use_is_next_getter() {
    DateTime now = DateTime.now();
    ArticleData article = new ArticleData();
    article.setUpdatedAt(now);
    
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), Direction.NEXT, true);
    
    assertThat(pager.isNext(), is(true));
  }

  @Test
  public void should_use_is_previous_getter() {
    DateTime now = DateTime.now();
    ArticleData article = new ArticleData();
    article.setUpdatedAt(now);
    
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(article), Direction.PREV, true);
    
    assertThat(pager.isPrevious(), is(true));
  }
}
