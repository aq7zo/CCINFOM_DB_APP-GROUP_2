package app.dao;

import app.config.DatabaseConnection;
import app.models.Perpetrator;
import app.utils.DateUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Perpetrator database operations
 */
public class PerpetratorDAO {
    
    public List<Perpetrator> findAll() {
        List<Perpetrator> perpetrators = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Perpetrators ORDER BY PerpetratorID";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    perpetrators.add(mapResultSetToPerpetrator(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving perpetrators: " + e.getMessage());
        }
        return perpetrators;
    }
    
    public Perpetrator findById(int perpetratorID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Perpetrators WHERE PerpetratorID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, perpetratorID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToPerpetrator(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving perpetrator: " + e.getMessage());
        }
        return null;
    }
    
    public Integer findIdByIdentifier(String identifier) {
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
    
    public int insert(Perpetrator perpetrator) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Perpetrators (Identifier, IdentifierType, AssociatedName, ThreatLevel) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, perpetrator.getIdentifier());
                stmt.setString(2, perpetrator.getIdentifierType());
                stmt.setString(3, perpetrator.getAssociatedName());
                stmt.setString(4, perpetrator.getThreatLevel() != null ? perpetrator.getThreatLevel() : "UnderReview");
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating perpetrator: " + e.getMessage());
        }
        return -1;
    }
    
    public boolean updateThreatLevel(int perpetratorID, String threatLevel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Perpetrators SET ThreatLevel = ? WHERE PerpetratorID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, threatLevel);
                stmt.setInt(2, perpetratorID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating threat level: " + e.getMessage());
        }
        return false;
    }
    
    public boolean updateLastIncidentDate(int perpetratorID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Perpetrators SET LastIncidentDate = NOW() WHERE PerpetratorID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, perpetratorID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating last incident date: " + e.getMessage());
        }
        return false;
    }
    
    public String getThreatLevel(int perpetratorID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT ThreatLevel FROM Perpetrators WHERE PerpetratorID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, perpetratorID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("ThreatLevel");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting threat level: " + e.getMessage());
        }
        return null;
    }
    
    public List<Perpetrator> findPerpetratorsToAutoEscalate() {
        List<Perpetrator> perpetrators = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT p.*, COUNT(DISTINCT ir.VictimID) as VictimCount
                FROM Perpetrators p
                JOIN IncidentReports ir ON p.PerpetratorID = ir.PerpetratorID
                WHERE ir.DateReported >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                GROUP BY p.PerpetratorID
                HAVING VictimCount >= 3 AND p.ThreatLevel != 'Malicious'
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    perpetrators.add(mapResultSetToPerpetrator(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding perpetrators to auto-escalate: " + e.getMessage());
        }
        return perpetrators;
    }
    
    private Perpetrator mapResultSetToPerpetrator(ResultSet rs) throws SQLException {
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

