package controller;

import model.*;
import utils.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Victim management operations
 */
public class VictimController {
    
    /**
     * Create a new victim
     */
    public static boolean createVictim(String name, String contactEmail) {
        if (!ValidationUtils.isNotEmpty(name) || !ValidationUtils.isValidEmail(contactEmail)) {
            System.out.println("Invalid input. Name and valid email are required.");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Victims (Name, ContactEmail, AccountStatus) VALUES (?, ?, 'Active')";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, contactEmail);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Victim created successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                System.out.println("Error: Email already exists.");
            } else {
                System.out.println("Error creating victim: " + e.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Get all victims
     */
    public static List<Victim> getAllVictims() {
        List<Victim> victims = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Victims ORDER BY VictimID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Victim victim = new Victim();
                    victim.setVictimID(rs.getInt("VictimID"));
                    victim.setName(rs.getString("Name"));
                    victim.setContactEmail(rs.getString("ContactEmail"));
                    victim.setAccountStatus(rs.getString("AccountStatus"));
                    victim.setDateCreated(DateUtils.fromDatabaseFormat(rs.getString("DateCreated")));
                    victims.add(victim);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving victims: " + e.getMessage());
        }
        
        return victims;
    }
    
    /**
     * Get victim by ID
     */
    public static Victim getVictimById(int victimID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Victims WHERE VictimID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Victim victim = new Victim();
                        victim.setVictimID(rs.getInt("VictimID"));
                        victim.setName(rs.getString("Name"));
                        victim.setContactEmail(rs.getString("ContactEmail"));
                        victim.setAccountStatus(rs.getString("AccountStatus"));
                        victim.setDateCreated(DateUtils.fromDatabaseFormat(rs.getString("DateCreated")));
                        return victim;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving victim: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Update victim status
     */
    public static boolean updateVictimStatus(int victimID, String newStatus, Integer adminID) {
        if (!ValidationUtils.isValidEnumValue(newStatus, new String[]{"Active", "Flagged", "Suspended"})) {
            System.out.println("Invalid status. Must be: Active, Flagged, or Suspended");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get old status
            String oldStatus = null;
            String getStatusSql = "SELECT AccountStatus FROM Victims WHERE VictimID = ?";
            try (PreparedStatement getStmt = conn.prepareStatement(getStatusSql)) {
                getStmt.setInt(1, victimID);
                try (ResultSet rs = getStmt.executeQuery()) {
                    if (rs.next()) {
                        oldStatus = rs.getString("AccountStatus");
                    } else {
                        System.out.println("Victim not found.");
                        return false;
                    }
                }
            }
            
            // Update status
            String sql = "UPDATE Victims SET AccountStatus = ? WHERE VictimID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, victimID);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Log the status change
                    logVictimStatusChange(victimID, oldStatus, newStatus, adminID);
                    System.out.println("Victim status updated successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating victim status: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check and auto-flag victims with >5 incidents in a month
     */
    public static void checkAndAutoFlagVictims() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT v.VictimID, v.AccountStatus, COUNT(ir.IncidentID) as IncidentCount
                FROM Victims v
                JOIN IncidentReports ir ON v.VictimID = ir.VictimID
                WHERE ir.DateReported >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
                GROUP BY v.VictimID, v.AccountStatus
                HAVING IncidentCount > 5 AND v.AccountStatus = 'Active'
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    int victimID = rs.getInt("VictimID");
                    updateVictimStatus(victimID, "Flagged", null); // Auto-flag, no admin
                    System.out.println("Auto-flagged VictimID " + victimID + " (>5 incidents in last month)");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking for auto-flag: " + e.getMessage());
        }
    }
    
    /**
     * Log victim status change
     */
    private static void logVictimStatusChange(int victimID, String oldStatus, String newStatus, Integer adminID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO VictimStatusLog (VictimID, OldStatus, NewStatus, AdminID) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                stmt.setString(2, oldStatus);
                stmt.setString(3, newStatus);
                if (adminID != null) {
                    stmt.setInt(4, adminID);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error logging status change: " + e.getMessage());
        }
    }
    
    /**
     * Delete victim
     */
    public static boolean deleteVictim(int victimID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Victims WHERE VictimID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Victim deleted successfully.");
                    return true;
                } else {
                    System.out.println("Victim not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting victim: " + e.getMessage());
        }
        
        return false;
    }
}

