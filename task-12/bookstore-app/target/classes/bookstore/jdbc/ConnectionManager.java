package bookstore.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionManager {
    private static ConnectionManager instance;
    private final String url;
    private final String username;
    private final String password;

    private ConnectionManager() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL драйвер не найден", e);
        }

        Properties props = new Properties();
        try (InputStream input = ConnectionManager.class.getResourceAsStream("jdbc.properties")) {
            if (input == null) {
                throw new RuntimeException("Файл jdbc.properties не найден рядом с ConnectionManager.java");
            }
            props.load(input);
            this.url = props.getProperty("jdbc.url");
            this.username = props.getProperty("jdbc.username");
            this.password = props.getProperty("jdbc.password");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить jdbc.properties", e);
        }
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}