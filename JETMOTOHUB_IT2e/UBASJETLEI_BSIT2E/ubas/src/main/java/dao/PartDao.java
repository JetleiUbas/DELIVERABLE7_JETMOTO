package dao;

import model.Part;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartDao {

    /** Searches by part name, category, or brand. A blank keyword lists every part. */
    public List<Part> search(String keyword) {
        String sql = """
                SELECT part_id, part_name, category, brand, price, stock, description
                FROM motorcycle_parts
                WHERE part_name LIKE ? OR category LIKE ? OR brand LIKE ?
                ORDER BY part_name
                """;
        List<Part> parts = new ArrayList<>();
        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    parts.add(new Part(
                            rs.getInt("part_id"),
                            rs.getString("part_name"),
                            rs.getString("category"),
                            rs.getString("brand"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
        }
        return parts;
    }

    /** Adds a few sample parts the first time so the catalog is not empty. */
    public void seedIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM motorcycle_parts";
        String insertSql = """
                INSERT INTO motorcycle_parts (part_name, category, brand, price, stock, description)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        Object[][] samples = {
                {"Brake Pad Set", "Brakes", "Yamaha", 450.00, 25, "Front disc brake pads"},
                {"Spark Plug", "Engine", "NGK", 120.00, 60, "Standard spark plug"},
                {"Drive Chain 428", "Drivetrain", "DID", 980.00, 12, "Heavy duty drive chain"},
                {"Engine Oil 1L", "Fluids", "Motul", 520.00, 40, "10W-40 semi-synthetic oil"},
                {"Side Mirror", "Body", "Honda", 350.00, 8, "Left side mirror"},
                {"Air Filter", "Engine", "Kawasaki", 280.00, 5, "Replacement air filter"}
        };
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement count = conn.prepareStatement(countSql);
             ResultSet rs = count.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Object[] s : samples) {
                    ps.setString(1, (String) s[0]);
                    ps.setString(2, (String) s[1]);
                    ps.setString(3, (String) s[2]);
                    ps.setDouble(4, (Double) s[3]);
                    ps.setInt(5, (Integer) s[4]);
                    ps.setString(6, (String) s[5]);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Sample data failed: " + e.getMessage());
        }
    }
}
