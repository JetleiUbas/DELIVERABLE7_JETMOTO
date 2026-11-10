package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    /** Saves a new user. Status is always PENDING on registration. */
    public boolean register(String name, String username, String password, String roles) {
        String sql = "INSERT INTO users (name, username, password, roles, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, roles);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Register failed: " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Lookup failed: " + e.getMessage());
            return false;
        }
    }

    /** Returns every registered account, regardless of role or status. */
    public List<User> getAllUsers() {
        return findUsers("SELECT id, name, username, password, roles, status FROM users ORDER BY id");
    }

    /** Returns pending accounts for every role. */
    public List<User> getPendingUsers() {
        return findUsers("SELECT id, name, username, password, roles, status FROM users WHERE status = 'PENDING' ORDER BY id");
    }

    private List<User> findUsers(String sql) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            System.out.println("Could not retrieve users: " + e.getMessage());
        }
        return users;
    }

    /** Approves a user only when their current status is PENDING. */
    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Could not approve user: " + e.getMessage());
            return false;
        }
    }

    /** Updates a user's role and account status using parameterized SQL. */
    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus) {
        String sql = "UPDATE users SET roles = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole.toUpperCase(java.util.Locale.ROOT));
            ps.setString(2, newStatus.toUpperCase(java.util.Locale.ROOT));
            ps.setInt(3, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Could not update user: " + e.getMessage());
            return false;
        }
    }

    /** Returns the user if username and password match, otherwise empty. */
    public Optional<User> login(String username, String password) {
        String sql = "SELECT id, name, username, password, roles, status FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return Optional.empty();
    }
}
