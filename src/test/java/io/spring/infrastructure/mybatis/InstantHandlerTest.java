package io.spring.infrastructure.mybatis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class InstantHandlerTest {
  private static final Instant INSTANT = Instant.parse("2016-02-18T03:22:56.637Z");

  private final InstantHandler handler = new InstantHandler();

  @Test
  public void should_write_utc_timestamp_with_millisecond_precision() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);
    ArgumentCaptor<Timestamp> timestamp = ArgumentCaptor.forClass(Timestamp.class);
    ArgumentCaptor<Calendar> calendar = ArgumentCaptor.forClass(Calendar.class);

    handler.setParameter(ps, 1, INSTANT, JdbcType.TIMESTAMP);

    verify(ps).setTimestamp(eq(1), timestamp.capture(), calendar.capture());
    Assertions.assertEquals(INSTANT.toEpochMilli(), timestamp.getValue().getTime());
    Assertions.assertEquals("UTC", calendar.getValue().getTimeZone().getID());
  }

  @Test
  public void should_write_null_as_null_timestamp() throws SQLException {
    PreparedStatement ps = mock(PreparedStatement.class);

    handler.setParameter(ps, 2, null, JdbcType.TIMESTAMP);

    verify(ps).setTimestamp(eq(2), eq(null), any(Calendar.class));
  }

  @Test
  public void should_read_timestamp_back_as_same_instant() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq("created_at"), any(Calendar.class)))
        .thenReturn(new Timestamp(INSTANT.toEpochMilli()));
    when(rs.getTimestamp(eq(3), any(Calendar.class)))
        .thenReturn(new Timestamp(INSTANT.toEpochMilli()));

    Assertions.assertEquals(INSTANT, handler.getResult(rs, "created_at"));
    Assertions.assertEquals(INSTANT, handler.getResult(rs, 3));
  }

  @Test
  public void should_read_null_timestamp_as_null() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq("created_at"), any(Calendar.class))).thenReturn(null);

    Assertions.assertNull(handler.getResult(rs, "created_at"));
  }

  @Test
  public void should_truncate_sub_millisecond_precision_consistently() throws SQLException {
    Instant withNanos = INSTANT.plusNanos(999_999);
    PreparedStatement ps = mock(PreparedStatement.class);
    ArgumentCaptor<Timestamp> timestamp = ArgumentCaptor.forClass(Timestamp.class);

    handler.setParameter(ps, 1, withNanos, JdbcType.TIMESTAMP);

    verify(ps).setTimestamp(eq(1), timestamp.capture(), any(Calendar.class));
    Assertions.assertEquals(INSTANT.toEpochMilli(), timestamp.getValue().getTime());
  }
}
