package basic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Pg {
  public static Connection connect()
    throws ClassNotFoundException, SQLException
  {
    Class.forName("org.postgresql.Driver");
    return DriverManager.getConnection(
      "jdbc:postgresql://localhost/test",  // url
      "neko", "cat" // user, password
    );
  }
}
