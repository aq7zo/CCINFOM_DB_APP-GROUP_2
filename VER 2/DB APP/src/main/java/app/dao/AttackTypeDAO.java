package app.dao;

import app.config.DatabaseConnection;
import app.models.AttackType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for AttackType database operations
 */
public class AttackTypeDAO {
    
    public List<AttackType> findAll() {
        List<AttackType> attackTypes = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM AttackTypes ORDER BY AttackTypeID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attackTypes.add(mapResultSetToAttackType(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attack types: " + e.getMessage());
        }
        return attackTypes;
    }
    
    public AttackType findById(int attackTypeID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM AttackTypes WHERE AttackTypeID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, attackTypeID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAttackType(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attack type: " + e.getMessage());
        }
        return null;
    }
    
    public int insert(AttackType attackType) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO AttackTypes (AttackName, Description, SeverityLevel) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, attackType.getAttackName());
                stmt.setString(2, attackType.getDescription());
                stmt.setString(3, attackType.getSeverityLevel());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating attack type: " + e.getMessage());
        }
        return -1;
    }
    
    public boolean update(AttackType attackType) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE AttackTypes SET AttackName = ?, Description = ?, SeverityLevel = ? WHERE AttackTypeID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, attackType.getAttackName());
                stmt.setString(2, attackType.getDescription());
                stmt.setString(3, attackType.getSeverityLevel());
                stmt.setInt(4, attackType.getAttackTypeID());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating attack type: " + e.getMessage());
        }
        return false;
    }
    
    public boolean delete(int attackTypeID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM AttackTypes WHERE AttackTypeID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, attackTypeID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting attack type: " + e.getMessage());
        }
        return false;
    }
    
    private AttackType mapResultSetToAttackType(ResultSet rs) throws SQLException {
        AttackType attackType = new AttackType();
        attackType.setAttackTypeID(rs.getInt("AttackTypeID"));
        attackType.setAttackName(rs.getString("AttackName"));
        attackType.setDescription(rs.getString("Description"));
        attackType.setSeverityLevel(rs.getString("SeverityLevel"));
        return attackType;
    }
}

