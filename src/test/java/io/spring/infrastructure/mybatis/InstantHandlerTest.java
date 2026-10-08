package io.spring.infrastructure.mybatis;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InstantHandlerTest {
  private static final Instant INSTANT = Instant.parse("2016-02-18T03:22:56.637Z");

  private final InstantHandler handler = new InstantHandler();

  @Test
  public void should_write_utc_text_with_millisecond_precision() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);

    handler.setParameter(ps, 1, INSTANT, JdbcType.TIMESTAMP);

    verify(ps).setString(1, "2016-02-18 03:22:56.637");
  }

  @Test
  public void should_write_null_as_sql_null() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);

    handler.setParameter(ps, 2, null, JdbcType.TIMESTAMP);

    verify(ps).setNull(2, Types.VARCHAR);
  }

  @Test
  public void should_truncate_sub_millisecond_precision_consistently() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);

    handler.setParameter(ps, 1, INSTANT.plusNanos(999_999), JdbcType.TIMESTAMP);

    verify(ps).setString(1, "2016-02-18 03:22:56.637");
  }

  @Test
  public void should_read_own_text_format_back_as_same_instant() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getObject("created_at")).thenReturn("2016-02-18 03:22:56.637");
    when(rs.getObject(3)).thenReturn("2016-02-18 03:22:56.637");

    Assertions.assertEquals(INSTANT, handler.getResult(rs, "created_at"));
    Assertions.assertEquals(INSTANT, handler.getResult(rs, 3));
  }

  @Test
  public void should_read_seed_sql_text_without_fraction_as_utc() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getObject("created_at")).thenReturn("2016-02-18 03:22:56");

    Assertions.assertEquals(
        Instant.parse("2016-02-18T03:22:56Z"), handler.getResult(rs, "created_at"));
  }

  @Test
  public void should_read_iso_text_with_offset() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getObject("created_at")).thenReturn("2016-02-18T03:22:56.637Z");
    when(rs.getObject("updated_at")).thenReturn("2016-02-18T05:22:56.637+02:00");

    Assertions.assertEquals(INSTANT, handler.getResult(rs, "created_at"));
    Assertions.assertEquals(INSTANT, handler.getResult(rs, "updated_at"));
  }

  @Test
  public void should_read_legacy_epoch_millis_integers() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    CallableStatement cs = mock(CallableStatement.class);
    when(rs.getObject("created_at")).thenReturn(INSTANT.toEpochMilli());
    when(rs.getObject("small")).thenReturn(1000);
    when(cs.getObject(1)).thenReturn(INSTANT.toEpochMilli());

    Assertions.assertEquals(INSTANT, handler.getResult(rs, "created_at"));
    Assertions.assertEquals(Instant.ofEpochMilli(1000), handler.getResult(rs, "small"));
    Assertions.assertEquals(INSTANT, handler.getResult(cs, 1));
  }

  @Test
  public void should_read_null_and_blank_as_null() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getObject("created_at")).thenReturn(null);
    when(rs.getObject("blank")).thenReturn("  ");

    Assertions.assertNull(handler.getResult(rs, "created_at"));
    Assertions.assertNull(handler.getResult(rs, "blank"));
  }

  @Test
  public void should_reject_unparseable_text() {
    ResultSet rs = mock(ResultSet.class);
    try {
      when(rs.getObject("created_at")).thenReturn("not a date");
    } catch (SQLException e) {
      throw new AssertionError(e);
    }

    Assertions.assertThrows(SQLException.class, () -> handler.getResult(rs, "created_at"));
  }
}
