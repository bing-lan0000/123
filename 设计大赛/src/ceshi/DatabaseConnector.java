package ceshi;
//连接数据库
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector{
  private static final String URL = "jdbc:mysql://localhost:3306/大赛数据";
  private static final String USER = "root";  // 根据实际情况修改
  private static final String PASSWORD = "2003227Cxm";  // 根据实际情况修改

  public static Connection getConnection() throws SQLException {//Java SQL API中的一个接口，代表与数据库的连接。
      return DriverManager.getConnection(URL, USER, PASSWORD);
      //Java SQL API中的一个类，作为获取数据库连接的工厂。DriverManager类的一个方法，用于建立数据库连接。数据库的URL，通常是数据库的位置和类型
  }
}
