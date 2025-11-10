package app.dao;

import app.config.DatabaseConnection;
import app.models.Victim;
import app.utils.DateUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Victim database operations
 */
public class VictimDAO {
    
    public List<Victim> findAll() {
        List<Victim> victims = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Victims ORDER BY VictimID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    victims.add(mapResultSetToVictim(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving victims: " + e.getMessage());
        }
        return victims;
    }
    
    public Victim findById(int victimID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Victims WHERE VictimID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToVictim(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving victim: " + e.getMessage());
        }
        return null;
    }
    
    public Victim findByEmail(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Victims WHERE ContactEmail = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToVictim(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving victim by email: " + e.getMessage());
        }
        return null;
    }
    
    public int insert(Victim victim) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Victims (Name, ContactEmail, AccountStatus) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, victim.getName());
                stmt.setString(2, victim.getContactEmail());
                stmt.setString(3, victim.getAccountStatus() != null ? victim.getAccountStatus() : "Active");
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating victim: " + e.getMessage());
        }
        return -1;
    }
    
    public boolean updateStatus(int victimID, String newStatus) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Victims SET AccountStatus = ? WHERE VictimID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, victimID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating victim status: " + e.getMessage());
        }
        return false;
    }
    
    public String getStatus(int victimID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT AccountStatus FROM Victims WHERE VictimID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("AccountStatus");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting victim status: " + e.getMessage());
        }
        return null;
    }
    
    public boolean delete(int victimID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Victims WHERE VictimID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting victim: " + e.getMessage());
        }
        return false;
    }
    
    public List<Victim> findVictimsToAutoFlag() {
        List<Victim> victims = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT v.*, COUNT(ir.IncidentID) as IncidentCount
                FROM Victims v
                JOIN IncidentReports ir ON v.VictimID = ir.VictimID
                WHERE ir.DateReported >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
                GROUP BY v.VictimID
                HAVING IncidentCount > 5 AND v.AccountStatus = 'Active'
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    victims.add(mapResultSetToVictim(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding victims to auto-flag: " + e.getMessage());
        }
        return victims;
    }
    
    private Victim mapResultSetToVictim(ResultSet rs) throws SQLException {
        Victim victim = new Victim();
        victim.setVictimID(rs.getInt("VictimID"));
        victim.setName(rs.getString("Name"));
        victim.setContactEmail(rs.getString("ContactEmail"));
        victim.setAccountStatus(rs.getString("AccountStatus"));
        victim.setDateCreated(DateUtils.fromDatabaseFormat(rs.getString("DateCreated")));
        return victim;
    }
}

