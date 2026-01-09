package saner_jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class simplifies the management of a complex list of arguments in PreparedStatement. You
 * simply use <code>.arg(value)</code> to add arguments to an internal list instead of question
 * marks, and then <code>.setArgs()</code> once to apply the list instead of a list of <code>
 * .set[data_type]()</code> calls.
 *
 * <p>This is particularly useful to simplify code for "dynamic" SQL queries, i.e., when the shape
 * of the query is determined by conditions.
 *
 * <p>See <a href="https://maximullaris.com/saner_jdbc.html">article</a>.
 */
public class SqlArgs {
  private final List<Object> args = new ArrayList<>();

  /**
   * Use this to provide a single prepared statement argument.
   *
   * @param v argument value (any common Java type works)
   * @return <code>"?"</code> for JDBC placeholder
   */
  public String arg(Object v) {
    args.add(v);
    return "?";
  }

  /**
   * Use this to provide a list of prepared statement arguments to be used in <code>IN (...)</code>
   * query.
   *
   * <p>NB! At least one argument is required, because empty <code>IN ()</code> is a syntax error in
   * SQL.
   *
   * @param vv list of argument values (any common Java type works)
   * @return <code>"(?,?,?)"</code>
   */
  public String list(Object... vv) {
    if (vv.length == 0) {
      // because `IN ()` gives a syntax error in SQL
      throw new IllegalArgumentException();
    }
    Collections.addAll(args, vv);
    return "(" + ",?".repeat(vv.length).substring(1) + ")";
  }

  /** Populates a prepared statement with arguments provided via {@link #arg} and {@link #list}. */
  public void setArgs(PreparedStatement statement) throws SQLException {
    int idx = 0;
    for (Object arg : args) {
      statement.setObject(++idx, arg);
    }
  }
}
