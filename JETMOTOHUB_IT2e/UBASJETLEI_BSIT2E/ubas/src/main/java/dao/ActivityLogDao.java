package dao;

import model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data access for the activity_logs audit trail. */
public class ActivityLogDao {

    private static final String SELECT_BASE = """
            SELECT log_id, user_id, action_type, description,
                   datetime(timestamp, 'localtime') AS timestamp
            FROM activity_logs
            """;

    /** Records one action. Never throws: a logging failure must not break the real action. */
    public boolean logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Could not write activity log: " + e.getMessage());
            return false;
        }
    }

    /** Returns every log entry, newest first. */
    public List<ActivityLog> getAllLogs() {
        String sql = SELECT_BASE + " ORDER BY timestamp DESC, log_id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                logs.add(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("Could not retrieve activity logs: " + e.getMessage());
        }
        return logs;
    }

    /** Returns the log entries of one user, newest first. */
    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = SELECT_BASE + " WHERE user_id = ? ORDER BY timestamp DESC, log_id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not retrieve activity logs: " + e.getMessage());
        }
        return logs;
    }

    /** Deletes all history. Returns the number of rows removed, or -1 on failure. */
    public int clearAllLogs() {
        String sql = "DELETE FROM activity_logs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Could not clear activity logs: " + e.getMessage());
            return -1;
        }
    }

    private ActivityLog map(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("log_id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp"));
    }
}
