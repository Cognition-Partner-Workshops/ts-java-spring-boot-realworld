package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateTimeHandlerTest {

  private DateTimeHandler handler;

  @Mock private PreparedStatement preparedStatement;

  @Mock private ResultSet resultSet;

  @Mock private CallableStatement callableStatement;

  @BeforeEach
  void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  void setParameter_withDateTime() throws SQLException {
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0);

    handler.setParameter(preparedStatement, 1, dateTime, JdbcType.TIMESTAMP);

    verify(preparedStatement).setTimestamp(anyInt(), any(Timestamp.class), any(Calendar.class));
  }

  @Test
  void setParameter_withNull() throws SQLException {
    handler.setParameter(preparedStatement, 1, null, JdbcType.TIMESTAMP);

    verify(preparedStatement).setTimestamp(anyInt(), isNull(), any(Calendar.class));
  }

  @Test
  void getResult_byColumnName_withTimestamp() throws SQLException {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    when(resultSet.getTimestamp(anyString(), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(resultSet, "created_at");

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void getResult_byColumnName_withNull() throws SQLException {
    when(resultSet.getTimestamp(anyString(), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(resultSet, "created_at");

    assertNull(result);
  }

  @Test
  void getResult_byColumnIndex_withTimestamp() throws SQLException {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    when(resultSet.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(resultSet, 1);

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void getResult_byColumnIndex_withNull() throws SQLException {
    when(resultSet.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(resultSet, 1);

    assertNull(result);
  }

  @Test
  void getResult_fromCallableStatement_withTimestamp() throws SQLException {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    when(callableStatement.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(timestamp);

    DateTime result = handler.getResult(callableStatement, 1);

    assertNotNull(result);
    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  void getResult_fromCallableStatement_withNull() throws SQLException {
    when(callableStatement.getTimestamp(anyInt(), any(Calendar.class))).thenReturn(null);

    DateTime result = handler.getResult(callableStatement, 1);

    assertNull(result);
  }
}
