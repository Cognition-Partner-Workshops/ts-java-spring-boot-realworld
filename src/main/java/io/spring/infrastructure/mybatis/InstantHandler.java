package io.spring.infrastructure.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

/**
 * Maps {@link Instant} to SQLite TIMESTAMP columns as UTC text ({@code yyyy-MM-dd HH:mm:ss.SSS}),
 * the same representation SQLite's own {@code datetime('now')} uses in the Flyway seed data, so
 * {@code ORDER BY created_at} and the cursor comparisons ({@code created_at < #{cursor}}) compare
 * like with like. Reading accepts that text (with or without fraction / 'T' / offset) as well as
 * the epoch-millis integers the former driver-level Joda handler wrote, so existing databases stay
 * readable. Replaces MyBatis' built-in Instant handler.
 */
@MappedTypes(Instant.class)
public class InstantHandler implements TypeHandler<Instant> {

  public static final DateTimeFormatter SQLITE_UTC_TEXT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

  private static final DateTimeFormatter LENIENT_UTC_TEXT =
      new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM-dd")
          .optionalStart()
          .appendLiteral(' ')
          .optionalEnd()
          .optionalStart()
          .appendLiteral('T')
          .optionalEnd()
          .appendPattern("HH:mm:ss")
          .optionalStart()
          .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
          .optionalEnd()
          .optionalStart()
          .appendOffsetId()
          .optionalEnd()
          .toFormatter();

  @Override
  public void setParameter(PreparedStatement ps, int i, Instant parameter, JdbcType jdbcType)
      throws SQLException {
    if (parameter == null) {
      ps.setNull(i, Types.VARCHAR);
    } else {
      ps.setString(i, format(parameter));
    }
  }

  @Override
  public Instant getResult(ResultSet rs, String columnName) throws SQLException {
    return toInstant(rs.getObject(columnName));
  }

  @Override
  public Instant getResult(ResultSet rs, int columnIndex) throws SQLException {
    return toInstant(rs.getObject(columnIndex));
  }

  @Override
  public Instant getResult(CallableStatement cs, int columnIndex) throws SQLException {
    return toInstant(cs.getObject(columnIndex));
  }

  public static String format(Instant instant) {
    return SQLITE_UTC_TEXT.format(instant.truncatedTo(ChronoUnit.MILLIS));
  }

  static Instant toInstant(Object value) throws SQLException {
    if (value == null) {
      return null;
    }
    if (value instanceof Number) {
      return Instant.ofEpochMilli(((Number) value).longValue());
    }
    if (value instanceof Date) {
      return Instant.ofEpochMilli(((Date) value).getTime());
    }
    if (value instanceof String) {
      return parseText((String) value);
    }
    throw new SQLException("Unsupported timestamp value: " + value.getClass().getName());
  }

  private static Instant parseText(String text) throws SQLException {
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    try {
      TemporalAccessor parsed = LENIENT_UTC_TEXT.parse(trimmed);
      if (parsed.isSupported(ChronoField.OFFSET_SECONDS)) {
        return OffsetDateTime.from(parsed).toInstant();
      }
      return LocalDateTime.from(parsed).toInstant(ZoneOffset.UTC);
    } catch (DateTimeParseException e) {
      throw new SQLException("Cannot parse timestamp text: " + text, e);
    }
  }
}
