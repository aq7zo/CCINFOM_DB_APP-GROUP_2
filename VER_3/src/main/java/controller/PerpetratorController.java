package controller;

import model.*;
import utils.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Perpetrator management operations
 */
public class PerpetratorController {
    
    /**
     * Create or get existing perpetrator
     */
    public static int createOrGetPerpetrator(String identifier, String identifierType, String associatedName) {
        if (!ValidationUtils.isNotEmpty(identifier) || !ValidationUtils.isNotEmpty(identifierType)) {
            System.out.println("Invalid input. Identifier and type are required.");
            return -1;
        }
        
        // Check if perpetrator already exists
        Integer existingID = getPerpetratorIdByIdentifier(identifier);
        if (existingID != null) {
            return existingID;
        }
        
        // Create new perpetrator
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Perpetrators (Identifier, IdentifierType, AssociatedName, ThreatLevel) VALUES (?, ?, ?, 'UnderReview')";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, identifier);
                stmt.setString(2, identifierType);
                stmt.setString(3, associatedName);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int newID = rs.getInt(1);
                            System.out.println("Perpetrator created successfully. ID: " + newID);
                            return newID;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                return getPerpetratorIdByIdentifier(identifier);
            } else {
                System.out.println("Error creating perpetrator: " + e.getMessage());
            }
        }
        
        return -1;
    }
    
    /**
     * Get perpetrator ID by identifier
     */
    private static Integer getPerpetratorIdByIdentifier(String identifier) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT PerpetratorID FROM Perpetrators WHERE Identifier = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, identifier);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("PerpetratorID");
                    }
                }
            }
        } catch (SQLException e) {
            // Ignore
        }
        
        return null;
    }
    
    /**
     * Get all perpetrators
     */
    public static List<Perpetrator> getAllPerpetrators() {
        List<Perpetrator> perpetrators = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Perpetrators ORDER BY PerpetratorID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Perpetrator perpetrator = new Perpetrator();
                    perpetrator.setPerpetratorID(rs.getInt("PerpetratorID"));
                    perpetrator.setIdentifier(rs.getString("Identifier"));
                    perpetrator.setIdentifierType(rs.getString("IdentifierType"));
                    perpetrator.setAssociatedName(rs.getString("AssociatedName"));
                    perpetrator.setThreatLevel(rs.getString("ThreatLevel"));
                    String lastIncident = rs.getString("LastIncidentDate");
                    if (lastIncident != null) {
                        perpetrator.setLastIncidentDate(DateUtils.fromDatabaseFormat(lastIncident));
                    }
                    perpetrators.add(perpetrator);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving perpetrators: " + e.getMessage());
        }
        
        return perpetrators;
    }
    
    /**
     * Get perpetrator by ID
     */
    public static Perpetrator getPerpetratorById(int perpetratorID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Perpetrators WHERE PerpetratorID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, perpetratorID);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Perpetrator perpetrator = new Perpetrator();
                        perpetrator.setPerpetratorID(rs.getInt("PerpetratorID"));
                        perpetrator.setIdentifier(rs.getString("Identifier"));
                        perpetrator.setIdentifierType(rs.getString("IdentifierType"));
                        perpetrator.setAssociatedName(rs.getString("AssociatedName"));
                        perpetrator.setThreatLevel(rs.getString("ThreatLevel"));
                        String lastIncident = rs.getString("LastIncidentDate");
                        if (lastIncident != null) {
                            perpetrator.setLastIncidentDate(DateUtils.fromDatabaseFormat(lastIncident));
                        }
                        return perpetrator;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving perpetrator: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Update perpetrator threat level
     */
    public static boolean updateThreatLevel(int perpetratorID, String newThreatLevel, Integer adminID) {
        if (!ValidationUtils.isValidEnumValue(newThreatLevel, 
                new String[]{"UnderReview", "Suspected", "Malicious", "Cleared"})) {
            System.out.println("Invalid threat level.");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get old threat level
            String oldThreatLevel = null;
            String getLevelSql = "SELECT ThreatLevel FROM Perpetrators WHERE PerpetratorID = ?";
            try (PreparedStatement getStmt = conn.prepareStatement(getLevelSql)) {
                getStmt.setInt(1, perpetratorID);
                try (ResultSet rs = getStmt.executeQuery()) {
                    if (rs.next()) {
                        oldThreatLevel = rs.getString("ThreatLevel");
                    } else {
                        System.out.println("Perpetrator not found.");
                        return false;
                    }
                }
            }
            
            // Update threat level
            String sql = "UPDATE Perpetrators SET ThreatLevel = ? WHERE PerpetratorID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newThreatLevel);
                stmt.setInt(2, perpetratorID);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Log the change
                    logThreatLevelChange(perpetratorID, oldThreatLevel, newThreatLevel, adminID);
                    System.out.println("Threat level updated successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating threat level: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check and auto-escalate perpetrators with ≥3 unique victims in 7 days
     */
    public static void checkAndAutoEscalate() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT p.PerpetratorID, p.ThreatLevel, COUNT(DISTINCT ir.VictimID) as VictimCount
                FROM Perpetrators p
                JOIN IncidentReports ir ON p.PerpetratorID = ir.PerpetratorID
                WHERE ir.DateReported >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                GROUP BY p.PerpetratorID, p.ThreatLevel
                HAVING VictimCount >= 3 AND p.ThreatLevel != 'Malicious'
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    int perpetratorID = rs.getInt("PerpetratorID");
                    String currentLevel = rs.getString("ThreatLevel");
                    
                    if (!"Malicious".equals(currentLevel)) {
                        updateThreatLevel(perpetratorID, "Malicious", null); // Auto-escalate, no admin
                        System.out.println("Auto-escalated PerpetratorID " + perpetratorID + 
                            " to Malicious (≥3 victims in 7 days)");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking for auto-escalation: " + e.getMessage());
        }
    }
    
    /**
     * Log threat level change
     */
    private static void logThreatLevelChange(int perpetratorID, String oldThreatLevel, 
                                           String newThreatLevel, Integer adminID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO ThreatLevelLog (PerpetratorID, OldThreatLevel, NewThreatLevel, AdminID) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, perpetratorID);
                stmt.setString(2, oldThreatLevel);
                stmt.setString(3, newThreatLevel);
                if (adminID != null) {
                    stmt.setInt(4, adminID);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error logging threat level change: " + e.getMessage());
        }
    }
    
    /**
     * Update last incident date for a perpetrator
     */
    public static void updateLastIncidentDate(int perpetratorID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Perpetrators SET LastIncidentDate = NOW() WHERE PerpetratorID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, perpetratorID);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error updating last incident date: " + e.getMessage());
        }
    }
}

