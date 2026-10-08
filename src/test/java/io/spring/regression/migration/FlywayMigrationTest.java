package io.spring.regression.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Applies every migration to a brand new SQLite file through the Flyway API and inspects it. */
class FlywayMigrationTest {

  @TempDir static Path tempDir;
  static Flyway flyway;
  static JdbcTemplate jdbc;

  @BeforeAll
  static void migrateFreshDatabase() {
    String url = "jdbc:sqlite:" + tempDir.resolve("fresh-migration.db").toAbsolutePath();
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url);
    dataSource.setDriverClassName("org.sqlite.JDBC");
    flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
    flyway.migrate();
    jdbc = new JdbcTemplate(dataSource);
  }

  @Test
  void info_shows_v1_and_v2_applied_successfully() {
    MigrationInfo[] all = flyway.info().all();
    assertEquals(2, all.length);
    assertEquals("1", all[0].getVersion().getVersion());
    assertEquals("create tables", all[0].getDescription());
    assertEquals(MigrationState.SUCCESS, all[0].getState());
    assertEquals("2", all[1].getVersion().getVersion());
    assertEquals("seed data", all[1].getDescription());
    assertEquals(MigrationState.SUCCESS, all[1].getState());
    assertEquals("2", flyway.info().current().getVersion().getVersion());
    assertEquals(0, flyway.info().pending().length);
    assertNotNull(all[1].getInstalledOn());
  }

  @Test
  void re_running_migrate_is_a_no_op() {
    assertEquals(0, flyway.migrate().migrationsExecuted);
    assertEquals(2, flyway.info().applied().length);
  }

  @Test
  void schema_contains_expected_tables() {
    Set<String> tables =
        new HashSet<>(
            jdbc.queryForList(
                "select name from sqlite_master where type = 'table' and name not like 'sqlite_%'",
                String.class));
    assertTrue(
        tables.containsAll(
            Arrays.asList(
                "users",
                "articles",
                "article_favorites",
                "follows",
                "tags",
                "article_tags",
                "comments",
                "flyway_schema_history")),
        tables.toString());
  }

  private List<String> columns(String table) {
    return jdbc.queryForList("pragma table_info('" + table + "')").stream()
        .map(row -> (String) row.get("name"))
        .collect(Collectors.toList());
  }

  @Test
  void tables_have_expected_columns() {
    assertEquals(
        Arrays.asList("id", "username", "password", "email", "bio", "image"), columns("users"));
    assertEquals(
        Arrays.asList(
            "id", "user_id", "slug", "title", "description", "body", "created_at", "updated_at"),
        columns("articles"));
    assertEquals(Arrays.asList("article_id", "user_id"), columns("article_favorites"));
    assertEquals(Arrays.asList("user_id", "follow_id"), columns("follows"));
    assertEquals(Arrays.asList("id", "name"), columns("tags"));
    assertEquals(Arrays.asList("article_id", "tag_id"), columns("article_tags"));
    assertEquals(
        Arrays.asList("id", "body", "article_id", "user_id", "created_at", "updated_at"),
        columns("comments"));
  }

  private Set<String> uniqueIndexedColumns(String table) {
    Set<String> result = new HashSet<>();
    for (Map<String, Object> index : jdbc.queryForList("pragma index_list('" + table + "')")) {
      if (((Number) index.get("unique")).intValue() == 1) {
        for (Map<String, Object> col :
            jdbc.queryForList("pragma index_info('" + index.get("name") + "')")) {
          result.add((String) col.get("name"));
        }
      }
    }
    return result;
  }

  @Test
  void unique_constraints_exist_on_username_email_slug_and_favorite_pk() {
    assertTrue(uniqueIndexedColumns("users").containsAll(Arrays.asList("username", "email")));
    assertTrue(uniqueIndexedColumns("articles").contains("slug"));
    assertEquals(
        new HashSet<>(Arrays.asList("article_id", "user_id")),
        uniqueIndexedColumns("article_favorites"));
  }

  @Test
  void seed_data_row_counts() {
    assertEquals(3, count("users"));
    assertEquals(7, count("tags"));
    assertEquals(5, count("articles"));
    assertEquals(6, count("article_favorites"));
    assertEquals(4, count("follows"));
    assertEquals(5, count("comments"));
    assertTrue(count("article_tags") >= 5);
    assertEquals(
        Arrays.asList("bobsmith", "janedoe", "johndoe"),
        jdbc.queryForList("select username from users order by username", String.class));
    assertEquals(
        Integer.valueOf(0),
        jdbc.queryForObject(
            "select count(*) from articles a left join users u on u.id = a.user_id where u.id is null",
            Integer.class));
    assertEquals(
        Integer.valueOf(0),
        jdbc.queryForObject(
            "select count(*) from article_tags at left join tags t on t.id = at.tag_id where t.id is null",
            Integer.class));
  }

  private int count(String table) {
    return jdbc.queryForObject("select count(*) from " + table, Integer.class);
  }
}
