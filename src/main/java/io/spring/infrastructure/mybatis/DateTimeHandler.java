package io.spring.infrastructure.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

/**
 * Maps {@link Instant} to SQL timestamps using a UTC calendar, exactly like the former Joda
 * DateTimeHandler did, so existing SQLite rows (epoch millis written by the driver and the textual
 * seed data) keep reading back as the same UTC instant. Replaces MyBatis' built-in Instant handler.
 */
@MappedTypes(Instant.class)
public class InstantHandler implements TypeHandler<Instant> {

  private static final Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

  @Override
  public void setParameter(PreparedStatement ps, int i, Instant parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTimestamp(
        i, parameter != null ? new Timestamp(parameter.toEpochMilli()) : null, UTC_CALENDAR);
  }

  @Override
  public Instant getResult(ResultSet rs, String columnName) throws SQLException {
    return toInstant(rs.getTimestamp(columnName, UTC_CALENDAR));
  }

  @Override
  public Instant getResult(ResultSet rs, int columnIndex) throws SQLException {
    return toInstant(rs.getTimestamp(columnIndex, UTC_CALENDAR));
  }

  @Override
  public Instant getResult(CallableStatement cs, int columnIndex) throws SQLException {
    return toInstant(cs.getTimestamp(columnIndex, UTC_CALENDAR));
  }

  private static Instant toInstant(Timestamp timestamp) {
    return timestamp != null ? Instant.ofEpochMilli(timestamp.getTime()) : null;
  }
}
