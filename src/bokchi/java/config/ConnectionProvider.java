 package bokchi.java.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {
    private static final String DB_NAME = "stamp_kiosk";

    static {
        try {
        	Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("드라이버 로드 성공");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("[DB] MySQL Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = MySQLConnInfo.URL + "/" + DB_NAME
                   + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true";
        return DriverManager.getConnection(url, MySQLConnInfo.USER, MySQLConnInfo.PASSWORD);
    }
}