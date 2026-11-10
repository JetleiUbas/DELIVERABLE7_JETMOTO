package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Creates the database schema before the application menu is displayed. */
public final class DatabaseInitializer {
    private DatabaseInitializer() {}

    public static void initialize() {
        String usersSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    name     TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles    TEXT NOT NULL CHECK (roles IN ('ADMIN', 'STAFF', 'CUSTOMER')),
                    status   TEXT NOT NULL DEFAULT 'PENDING'
                )
                """;
        String partsSql = """
                CREATE TABLE IF NOT EXISTS motorcycle_parts (
                    part_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    part_name   TEXT NOT NULL,
                    category    TEXT,
                    brand       TEXT,
                    price       REAL NOT NULL DEFAULT 0,
                    stock       INTEGER NOT NULL DEFAULT 0,
                    description TEXT
                )
                """;

        String logsSql = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    log_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id     INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT,
                    timestamp   DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(usersSql);
            statement.execute(partsSql);
            statement.execute(logsSql);
        } catch (SQLException e) {
            throw new IllegalStateException("Database initialization failed: " + e.getMessage(), e);
        }

        new PartDao().seedIfEmpty();
    }
}
