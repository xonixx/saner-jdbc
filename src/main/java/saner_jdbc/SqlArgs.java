package saner_jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlArgs {
  private final List<Object> args = new ArrayList<>();

  public String arg(Object v) {
    args.add(v);
    return "?";
  }

  public String list(Object... vv) {
    if (vv.length == 0) throw new IllegalArgumentException();
    Collections.addAll(args, vv);
    return "(" + ",?".repeat(vv.length).substring(1) + ")";
  }

  public void setArgs(PreparedStatement statement) throws SQLException {
    int idx = 0;
    for (Object arg : args) {
      statement.setObject(++idx, arg);
    }
  }
}
