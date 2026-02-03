package io.spring.infrastructure.mybatis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateTimeHandlerTest {

  private DateTimeHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  public void should_set_parameter_with_datetime() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0);

    handler.setParameter(ps, 1, dateTime, JdbcType.TIMESTAMP);

    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any(Calendar.class));
  }

  @Test
  public void should_set_parameter_with_null() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);

    handler.setParameter(ps, 1, null, JdbcType.TIMESTAMP);

    verify(ps).setTimestamp(eq(1), eq(null), any(Calendar.class));
  }

  @Test
  public void should_get_result_by_column_name() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(rs, "created_at");

    assertThat(result.getMillis(), is(timestamp.getTime()));
  }

  @Test
  public void should_get_null_result_by_column_name() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(rs, "created_at");

    assertThat(result, is(nullValue()));
  }

  @Test
  public void should_get_result_by_column_index() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(rs, 1);

    assertThat(result.getMillis(), is(timestamp.getTime()));
  }

  @Test
  public void should_get_null_result_by_column_index() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(rs, 1);

    assertThat(result, is(nullValue()));
  }

  @Test
  public void should_get_result_from_callable_statement() throws SQLException {
    CallableStatement cs = mock(CallableStatement.class);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    when(cs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(cs, 1);

    assertThat(result.getMillis(), is(timestamp.getTime()));
  }

  @Test
  public void should_get_null_result_from_callable_statement() throws SQLException {
    CallableStatement cs = mock(CallableStatement.class);
    when(cs.getTimestamp(eq(1), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(cs, 1);

    assertThat(result, is(nullValue()));
  }
}
