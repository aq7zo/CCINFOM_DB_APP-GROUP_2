package controller;

import model.*;
import utils.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for AttackType management operations
 */
public class AttackTypeController {
    
    /**
     * Create a new attack type
     */
    public static boolean createAttackType(String attackName, String description, String severityLevel) {
        if (!ValidationUtils.isNotEmpty(attackName)) {
            System.out.println("Invalid input. Attack name is required.");
            return false;
        }
        
        if (!ValidationUtils.isValidEnumValue(severityLevel, new String[]{"Low", "Medium", "High"})) {
            System.out.println("Invalid severity level. Must be: Low, Medium, or High");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO AttackTypes (AttackName, Description, SeverityLevel) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, attackName);
                stmt.setString(2, description);
                stmt.setString(3, severityLevel);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Attack type created successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating attack type: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get all attack types
     */
    public static List<AttackType> getAllAttackTypes() {
        List<AttackType> attackTypes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM AttackTypes ORDER BY AttackTypeID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    AttackType attackType = new AttackType();
                    attackType.setAttackTypeID(rs.getInt("AttackTypeID"));
                    attackType.setAttackName(rs.getString("AttackName"));
                    attackType.setDescription(rs.getString("Description"));
                    attackType.setSeverityLevel(rs.getString("SeverityLevel"));
                    attackTypes.add(attackType);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving attack types: " + e.getMessage());
        }
        
        return attackTypes;
    }
    
    /**
     * Get attack type by ID
     */
    public static AttackType getAttackTypeById(int attackTypeID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM AttackTypes WHERE AttackTypeID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, attackTypeID);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        AttackType attackType = new AttackType();
                        attackType.setAttackTypeID(rs.getInt("AttackTypeID"));
                        attackType.setAttackName(rs.getString("AttackName"));
                        attackType.setDescription(rs.getString("Description"));
                        attackType.setSeverityLevel(rs.getString("SeverityLevel"));
                        return attackType;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving attack type: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Update attack type
     */
    public static boolean updateAttackType(int attackTypeID, String attackName, String description, String severityLevel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE AttackTypes SET AttackName = ?, Description = ?, SeverityLevel = ? WHERE AttackTypeID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, attackName);
                stmt.setString(2, description);
                stmt.setString(3, severityLevel);
                stmt.setInt(4, attackTypeID);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Attack type updated successfully.");
                    return true;
                } else {
                    System.out.println("Attack type not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating attack type: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Delete attack type
     */
    public static boolean deleteAttackType(int attackTypeID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM AttackTypes WHERE AttackTypeID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, attackTypeID);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Attack type deleted successfully.");
                    return true;
                } else {
                    System.out.println("Attack type not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting attack type: " + e.getMessage());
        }
        
        return false;
    }
}

