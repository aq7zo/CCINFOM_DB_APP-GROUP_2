package controller;

import model.*;
import utils.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Administrator management operations
 */
public class AdminController {
    
    /**
     * Create a new administrator
     */
    public static boolean createAdministrator(String name, String role, String contactEmail) {
        if (!ValidationUtils.isNotEmpty(name) || !ValidationUtils.isValidEmail(contactEmail)) {
            System.out.println("Invalid input. Name and valid email are required.");
            return false;
        }
        
        if (!ValidationUtils.isValidEnumValue(role, new String[]{"System Admin", "Cybersecurity Staff"})) {
            System.out.println("Invalid role. Must be: System Admin or Cybersecurity Staff");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Administrators (Name, Role, ContactEmail) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, role);
                stmt.setString(3, contactEmail);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Administrator created successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                System.out.println("Error: Email already exists.");
            } else {
                System.out.println("Error creating administrator: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Get all administrators
     */
    public static List<Administrator> getAllAdministrators() {
        List<Administrator> admins = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Administrators ORDER BY AdminID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Administrator admin = new Administrator();
                    admin.setAdminID(rs.getInt("AdminID"));
                    admin.setName(rs.getString("Name"));
                    admin.setRole(rs.getString("Role"));
                    admin.setContactEmail(rs.getString("ContactEmail"));
                    admin.setDateAssigned(DateUtils.fromDatabaseFormat(rs.getString("DateAssigned")));
                    admins.add(admin);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving administrators: " + e.getMessage());
        }
        
        return admins;
    }
    
    /**
     * Get administrator by ID
     */
    public static Administrator getAdministratorById(int adminID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Administrators WHERE AdminID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, adminID);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Administrator admin = new Administrator();
                        admin.setAdminID(rs.getInt("AdminID"));
                        admin.setName(rs.getString("Name"));
                        admin.setRole(rs.getString("Role"));
                        admin.setContactEmail(rs.getString("ContactEmail"));
                        admin.setDateAssigned(DateUtils.fromDatabaseFormat(rs.getString("DateAssigned")));
                        return admin;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving administrator: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Authenticate administrator (simple email-based login)
     */
    public static Administrator authenticate(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Administrators WHERE ContactEmail = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Administrator admin = new Administrator();
                        admin.setAdminID(rs.getInt("AdminID"));
                        admin.setName(rs.getString("Name"));
                        admin.setRole(rs.getString("Role"));
                        admin.setContactEmail(rs.getString("ContactEmail"));
                        admin.setDateAssigned(DateUtils.fromDatabaseFormat(rs.getString("DateAssigned")));
                        return admin;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error authenticating: " + e.getMessage());
        }
        
        return null;
    }
}

