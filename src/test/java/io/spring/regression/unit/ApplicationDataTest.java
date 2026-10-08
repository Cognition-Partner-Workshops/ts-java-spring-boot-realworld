package io.spring.regression.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.Node;
import io.spring.application.Page;
import io.spring.application.PageCursor;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.regression.RegressionFixtures;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ApplicationDataTest {

  private final User author = RegressionFixtures.user("author");
  private final Article article = RegressionFixtures.article("x", author);

  @Nested
  class DataObjects {
    @Test
    void article_data_equality_getters_setters_and_cursor() {
      ArticleData a = RegressionFixtures.articleData(article, author);
      ArticleData b = RegressionFixtures.articleData(article, author);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertTrue(a.toString().contains(article.getSlug()));
      assertEquals(article.getId(), a.getId());
      assertEquals(article.getSlug(), a.getSlug());
      assertEquals(article.getTitle(), a.getTitle());
      assertEquals(article.getDescription(), a.getDescription());
      assertEquals(article.getBody(), a.getBody());
      assertFalse(a.isFavorited());
      assertEquals(0, a.getFavoritesCount());
      assertEquals(Arrays.asList("t1", "t2"), a.getTagList());
      assertEquals(author.getUsername(), a.getProfileData().getUsername());
      assertEquals(
          RegressionFixtures.millisOf(article.getUpdatedAt()),
          RegressionFixtures.millisOf(a.getCursor().getData()));

      b.setFavorited(true);
      b.setFavoritesCount(3);
      assertNotEquals(a, b);
      assertTrue(b.isFavorited());
      assertEquals(3, b.getFavoritesCount());

      ArticleData empty = new ArticleData();
      empty.setId("id");
      empty.setSlug("slug");
      empty.setTitle("title");
      empty.setDescription("d");
      empty.setBody("b");
      empty.setTagList(Collections.emptyList());
      empty.setCreatedAt(RegressionFixtures.atMillis(5L));
      empty.setUpdatedAt(RegressionFixtures.atMillis(6L));
      empty.setProfileData(new ProfileData());
      assertEquals("id", empty.getId());
      assertEquals("6", empty.getCursor().toString());
      assertNotEquals(empty, a);
      assertFalse(a.equals(null));
    }

    @Test
    void comment_data_equality_and_cursor() {
      CommentData a = RegressionFixtures.commentData("c1", article.getId(), author);
      CommentData b = new CommentData();
      b.setId(a.getId());
      b.setBody(a.getBody());
      b.setArticleId(a.getArticleId());
      b.setCreatedAt(a.getCreatedAt());
      b.setUpdatedAt(a.getUpdatedAt());
      b.setProfileData(a.getProfileData());
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertEquals(
          RegressionFixtures.millisOf(a.getCreatedAt()),
          RegressionFixtures.millisOf(a.getCursor().getData()));
      assertEquals("comment c1", a.getBody());
      assertEquals(article.getId(), a.getArticleId());
      b.setBody("other");
      assertNotEquals(a, b);
      assertTrue(a.toString().contains("c1"));
    }

    @Test
    void profile_data_equality_and_following_flag() {
      ProfileData a = RegressionFixtures.profile(author, false);
      ProfileData b = RegressionFixtures.profile(author, false);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      b.setFollowing(true);
      assertNotEquals(a, b);
      assertTrue(b.isFollowing());
      ProfileData empty = new ProfileData();
      empty.setId("i");
      empty.setUsername("u");
      empty.setBio(null);
      empty.setImage(null);
      assertEquals("i", empty.getId());
      assertEquals("u", empty.getUsername());
      assertNull(empty.getBio());
      assertNull(empty.getImage());
      assertTrue(empty.toString().contains("u"));
    }

    @Test
    void user_data_and_user_with_token() {
      UserData a = new UserData("id", "e@e.com", "name", "bio", "img");
      UserData b = new UserData();
      b.setId("id");
      b.setEmail("e@e.com");
      b.setUsername("name");
      b.setBio("bio");
      b.setImage("img");
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertTrue(a.toString().contains("name"));
      b.setBio("other");
      assertNotEquals(a, b);

      UserWithToken token = new UserWithToken(a, "jwt");
      assertEquals("e@e.com", token.getEmail());
      assertEquals("name", token.getUsername());
      assertEquals("bio", token.getBio());
      assertEquals("img", token.getImage());
      assertEquals("jwt", token.getToken());
    }

    @Test
    void article_data_list_and_favorite_count() {
      List<ArticleData> data = Arrays.asList(RegressionFixtures.articleData(article, author));
      ArticleDataList list = new ArticleDataList(data, 42);
      assertEquals(data, list.getArticleDatas());
      assertEquals(42, list.getCount());

      ArticleFavoriteCount c1 = new ArticleFavoriteCount("a", 3);
      ArticleFavoriteCount c2 = new ArticleFavoriteCount("a", 3);
      assertEquals(c1, c2);
      assertEquals(c1.hashCode(), c2.hashCode());
      assertEquals("a", c1.getId());
      assertEquals(3, c1.getCount());
      assertNotEquals(c1, new ArticleFavoriteCount("a", 4));
      assertTrue(c1.toString().contains("3"));
    }
  }

  @Nested
  class Params {
    @Test
    void new_article_param_builder_and_constructors() {
      NewArticleParam p =
          NewArticleParam.builder()
              .title("t")
              .description("d")
              .body("b")
              .tagList(Arrays.asList("x"))
              .build();
      assertEquals("t", p.getTitle());
      assertEquals("d", p.getDescription());
      assertEquals("b", p.getBody());
      assertEquals(Arrays.asList("x"), p.getTagList());
      NewArticleParam all = new NewArticleParam("t", "d", "b", null);
      assertNull(all.getTagList());
      assertNull(new NewArticleParam().getTitle());
      assertNotNull(NewArticleParam.builder().toString());
    }

    @Test
    void update_article_param_defaults_to_empty_strings() {
      UpdateArticleParam p = new UpdateArticleParam();
      assertEquals("", p.getTitle());
      assertEquals("", p.getBody());
      assertEquals("", p.getDescription());
      UpdateArticleParam full = new UpdateArticleParam("t", "b", "d");
      assertEquals("t", full.getTitle());
      assertEquals("b", full.getBody());
      assertEquals("d", full.getDescription());
    }

    @Test
    void register_and_update_user_params() {
      RegisterParam r = new RegisterParam("e@e.com", "u", "p");
      assertEquals("e@e.com", r.getEmail());
      assertEquals("u", r.getUsername());
      assertEquals("p", r.getPassword());
      assertNull(new RegisterParam().getEmail());

      UpdateUserParam defaults = UpdateUserParam.builder().build();
      assertEquals("", defaults.getEmail());
      assertEquals("", defaults.getPassword());
      assertEquals("", defaults.getUsername());
      assertEquals("", defaults.getBio());
      assertEquals("", defaults.getImage());
      assertEquals("", new UpdateUserParam().getEmail());
      UpdateUserParam full =
          UpdateUserParam.builder()
              .email("e")
              .password("p")
              .username("u")
              .bio("b")
              .image("i")
              .build();
      assertEquals("e", full.getEmail());
      assertEquals("p", full.getPassword());
      assertEquals("u", full.getUsername());
      assertEquals("b", full.getBio());
      assertEquals("i", full.getImage());
      UpdateUserParam ctor = new UpdateUserParam("e", "p", "u", "b", "i");
      assertEquals("i", ctor.getImage());
      assertNotNull(UpdateUserParam.builder().toString());

      UpdateUserCommand cmd = new UpdateUserCommand(author, full);
      assertEquals(author, cmd.getTargetUser());
      assertEquals(full, cmd.getParam());
    }
  }

  @Nested
  class Paging {
    @Test
    void page_clamps_offset_and_limit() {
      Page defaults = new Page();
      assertEquals(0, defaults.getOffset());
      assertEquals(20, defaults.getLimit());
      assertEquals(0, new Page(-5, 10).getOffset());
      assertEquals(10, new Page(-5, 10).getLimit());
      assertEquals(20, new Page(0, 0).getLimit());
      assertEquals(20, new Page(0, -1).getLimit());
      assertEquals(100, new Page(0, 1000).getLimit());
      assertEquals(100, new Page(0, 100).getLimit());
      assertEquals(new Page(3, 7), new Page(3, 7));
      assertEquals(new Page(3, 7).hashCode(), new Page(3, 7).hashCode());
      assertNotEquals(new Page(3, 7), new Page(3, 8));
      assertTrue(new Page(3, 7).toString().contains("7"));
    }

    @Test
    void cursor_page_parameter_clamps_limit_and_exposes_direction() {
      CursorPageParameter<String> defaults = new CursorPageParameter<>();
      assertEquals(20, defaults.getLimit());
      assertNull(defaults.getCursor());
      assertNull(defaults.getDirection());
      assertFalse(defaults.isNext());
      assertEquals(21, defaults.getQueryLimit());

      CursorPageParameter<String> next = new CursorPageParameter<>("c", 5, Direction.NEXT);
      assertTrue(next.isNext());
      assertEquals("c", next.getCursor());
      assertEquals(5, next.getLimit());
      assertEquals(6, next.getQueryLimit());
      assertEquals(Direction.NEXT, next.getDirection());

      assertEquals(1000, new CursorPageParameter<>("c", 5000, Direction.PREV).getLimit());
      assertEquals(20, new CursorPageParameter<>("c", 0, Direction.PREV).getLimit());
      assertEquals(20, new CursorPageParameter<>("c", -3, Direction.PREV).getLimit());
      assertFalse(new CursorPageParameter<>("c", 1, Direction.PREV).isNext());
      assertEquals(next, new CursorPageParameter<>("c", 5, Direction.NEXT));
      assertEquals(next.hashCode(), new CursorPageParameter<>("c", 5, Direction.NEXT).hashCode());
      assertTrue(next.toString().contains("NEXT"));
      next.setDirection(Direction.PREV);
      assertFalse(next.isNext());
    }

    @Test
    void cursor_pager_next_direction() {
      ArticleData a1 = RegressionFixtures.articleData(article, author);
      a1.setUpdatedAt(RegressionFixtures.atMillis(100L));
      ArticleData a2 = RegressionFixtures.articleData(article, author);
      a2.setUpdatedAt(RegressionFixtures.atMillis(200L));
      CursorPager<ArticleData> pager =
          new CursorPager<>(new ArrayList<>(Arrays.asList(a1, a2)), Direction.NEXT, true);
      assertTrue(pager.hasNext());
      assertTrue(pager.isNext());
      assertFalse(pager.hasPrevious());
      assertFalse(pager.isPrevious());
      assertEquals("100", pager.getStartCursor().toString());
      assertEquals("200", pager.getEndCursor().toString());
      assertEquals(2, pager.getData().size());

      CursorPager<ArticleData> noMore = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, false);
      assertFalse(noMore.hasNext());
      assertFalse(noMore.hasPrevious());
    }

    @Test
    void cursor_pager_prev_direction_and_empty() {
      CursorPager<Node> empty = new CursorPager<>(Collections.emptyList(), Direction.PREV, true);
      assertTrue(empty.hasPrevious());
      assertFalse(empty.hasNext());
      assertNull(empty.getStartCursor());
      assertNull(empty.getEndCursor());

      CursorPager<Node> none = new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
      assertFalse(none.hasPrevious());
    }

    @Test
    void date_time_cursor_round_trips_millis() {
      DateTimeCursor cursor = new DateTimeCursor(RegressionFixtures.atMillis(1234567890123L));
      assertEquals("1234567890123", cursor.toString());
      assertEquals(
          1234567890123L, RegressionFixtures.millisOf(DateTimeCursor.parse("1234567890123")));
      assertNull(DateTimeCursor.parse(null));
      assertEquals(1234567890123L, RegressionFixtures.millisOf(cursor.getData()));
    }

    @Test
    void page_cursor_to_string_delegates_to_data() {
      PageCursor<String> cursor = new PageCursor<String>("abc") {};
      assertEquals("abc", cursor.toString());
      assertEquals("abc", cursor.getData());
    }
  }
}
