package ceshi;
//�������ݿ�
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector{
  private static final String URL = "jdbc:mysql://localhost:3306/��������";
  private static final String USER = "root";  // ����ʵ������޸�
  private static final String PASSWORD = "2003227Cxm";  // ����ʵ������޸�

  public static Connection getConnection() throws SQLException {//Java SQL API�е�һ���ӿڣ����������ݿ�����ӡ�
      return DriverManager.getConnection(URL, USER, PASSWORD);
      //Java SQL API�е�һ���࣬��Ϊ��ȡ���ݿ����ӵĹ�����DriverManager���һ�����������ڽ������ݿ����ӡ����ݿ��URL��ͨ�������ݿ��λ�ú�����
  }
}
