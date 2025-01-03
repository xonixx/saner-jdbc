package saner_jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlArgsTests {
  @Test
  void testArg() throws SQLException {
    class Case {
      final Object arg;
      final String result;

      Case(Object arg, String result) {
        this.arg = arg;
        this.result = result;
      }
    }
    Case[] cases =
        new Case[] {
          new Case(null, "NULL"),
          new Case(7, "7"),
          new Case(1.23, "1.23"),
          new Case("value", "'value'"),
          new Case(true, "1"),
          new Case(false, "0"),
          new Case(Long.MAX_VALUE, "9223372036854775807"),
          new Case(Long.MIN_VALUE, "-9223372036854775808"),
          new Case(new BigDecimal("123.456"), "123.456"),
          new Case(new BigInteger(Long.toString(Long.MAX_VALUE)), "9223372036854775807"),
          new Case(new java.util.Date(0).toInstant(), "'1970-01-01 02:00:00'"),
          new Case(new java.sql.Timestamp(0), "'1970-01-01 02:00:00'"),
          new Case(new java.util.Date(0), "'1970-01-01 02:00:00'"),
          new Case(new java.sql.Date(0), "'1970-01-01'"),
        };
    try (Connection conn = getConnection()) {
      for (Case aCase : cases) {
        SqlArgs s = new SqlArgs();
        try (PreparedStatement statement = conn.prepareStatement("SELECT " + s.arg(aCase.arg))) {
          s.setArgs(statement);
          Assertions.assertEquals("SELECT " + aCase.result, getResultingSql(statement));
        }
      }
    }
  }

  @Test
  void testList() throws SQLException {
    try (Connection conn = getConnection()) {
      {
        SqlArgs s = new SqlArgs();
        try (PreparedStatement statement =
            conn.prepareStatement("SELECT 1 WHERE 1 IN " + s.list(1, 2, 3))) {
          s.setArgs(statement);
          Assertions.assertEquals("SELECT 1 WHERE 1 IN (1,2,3)", getResultingSql(statement));
          try (ResultSet resultSet = statement.executeQuery()) {
            Assertions.assertTrue(resultSet.next());
          }
        }
      }
      {
        SqlArgs s = new SqlArgs();
        try (PreparedStatement statement =
            conn.prepareStatement("SELECT 1 WHERE 7 IN " + s.list(1, 2, 3))) {
          s.setArgs(statement);
          Assertions.assertEquals("SELECT 1 WHERE 7 IN (1,2,3)", getResultingSql(statement));
          try (ResultSet resultSet = statement.executeQuery()) {
            Assertions.assertFalse(resultSet.next());
          }
        }
      }
    }
  }

  private static String getResultingSql(PreparedStatement statement) {
    String res = statement.toString();
    int i = res.indexOf(": SELECT");
    return res.substring(i + 2);
  }

  private static Connection getConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:mysql://127.0.0.1/mysql", "root", "root");
  }
}
