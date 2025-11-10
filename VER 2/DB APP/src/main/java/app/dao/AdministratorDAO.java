package app.dao;

import app.config.DatabaseConnection;
import app.models.Administrator;
import app.utils.DateUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Administrator database operations
 */
public class AdministratorDAO {
    
    public List<Administrator> findAll() {
        List<Administrator> admins = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Administrators ORDER BY AdminID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    admins.add(mapResultSetToAdmin(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving administrators: " + e.getMessage());
        }
        return admins;
    }
    
    public Administrator findById(int adminID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Administrators WHERE AdminID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, adminID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAdmin(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving administrator: " + e.getMessage());
        }
        return null;
    }
    
    public Administrator findByEmail(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Administrators WHERE ContactEmail = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAdmin(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving administrator by email: " + e.getMessage());
        }
        return null;
    }
    
    public int insert(Administrator admin) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Administrators (Name, Role, ContactEmail) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, admin.getName());
                stmt.setString(2, admin.getRole());
                stmt.setString(3, admin.getContactEmail());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating administrator: " + e.getMessage());
        }
        return -1;
    }
    
    public boolean delete(int adminID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Administrators WHERE AdminID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, adminID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting administrator: " + e.getMessage());
        }
        return false;
    }
    
    private Administrator mapResultSetToAdmin(ResultSet rs) throws SQLException {
        Administrator admin = new Administrator();
        admin.setAdminID(rs.getInt("AdminID"));
        admin.setName(rs.getString("Name"));
        admin.setRole(rs.getString("Role"));
        admin.setContactEmail(rs.getString("ContactEmail"));
        admin.setDateAssigned(DateUtils.fromDatabaseFormat(rs.getString("DateAssigned")));
        return admin;
    }
}

