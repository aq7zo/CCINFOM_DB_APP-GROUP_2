package main.java.com.group2.dbapp.dao;

import main.java.com.group2.dbapp.model.Administrator;
import main.java.com.group2.dbapp.util.DatabaseConnection;
import main.java.com.group2.dbapp.util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of AdministratorDAO interface
 * Handles database operations for Administrator entity
 */
public class AdministratorDAOImpl implements AdministratorDAO {
    
    @Override
    public Administrator findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Administrators WHERE ContactEmail = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdministrator(rs);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public Administrator findById(int adminID) throws SQLException {
        String sql = "SELECT * FROM Administrators WHERE AdminID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adminID);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdministrator(rs);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public List<Administrator> findAll() throws SQLException {
        List<Administrator> admins = new ArrayList<>();
        String sql = "SELECT * FROM Administrators ORDER BY AdminID";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                admins.add(mapResultSetToAdministrator(rs));
            }
        }
        
        return admins;
    }
    
    @Override
    public boolean create(Administrator admin) throws SQLException {
        String sql = "INSERT INTO Administrators (Name, Role, ContactEmail, PasswordHash) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, admin.getName());
            stmt.setString(2, admin.getRole());
            stmt.setString(3, admin.getContactEmail());
            stmt.setString(4, admin.getPasswordHash());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        admin.setAdminID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean update(Administrator admin) throws SQLException {
        String sql = "UPDATE Administrators SET Name = ?, Role = ?, ContactEmail = ?, PasswordHash = ? WHERE AdminID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, admin.getName());
            stmt.setString(2, admin.getRole());
            stmt.setString(3, admin.getContactEmail());
            stmt.setString(4, admin.getPasswordHash());
            stmt.setInt(5, admin.getAdminID());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    @Override
    public boolean delete(int adminID) throws SQLException {
        String sql = "DELETE FROM Administrators WHERE AdminID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adminID);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Helper method to map ResultSet to Administrator object
     */
    private Administrator mapResultSetToAdministrator(ResultSet rs) throws SQLException {
        Administrator admin = new Administrator();
        admin.setAdminID(rs.getInt("AdminID"));
        admin.setName(rs.getString("Name"));
        admin.setRole(rs.getString("Role"));
        admin.setContactEmail(rs.getString("ContactEmail"));
        admin.setPasswordHash(rs.getString("PasswordHash"));
        
        String dateStr = rs.getString("DateAssigned");
        if (dateStr != null) {
            admin.setDateAssigned(DateUtils.fromDatabaseFormat(dateStr));
        }
        
        return admin;
    }
}

