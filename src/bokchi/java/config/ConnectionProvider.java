package bokchi.java.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {
    private static final String DB_NAME = "stamp_kiosk"; // ← 네 DB명으로 수정 가능

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL 8.x 드라이버
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("[DB] MySQL Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        // MySQLConnInfo.URL == "jdbc:mysql://localhost:3306"
        String url = MySQLConnInfo.URL + "/" + DB_NAME
                   + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true";
        return DriverManager.getConnection(url, MySQLConnInfo.USER, MySQLConnInfo.PASSWORD);
    }
}