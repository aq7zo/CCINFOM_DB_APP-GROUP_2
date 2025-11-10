package app.dao;

import app.config.DatabaseConnection;
import app.models.IncidentReport;
import app.utils.DateUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for IncidentReport database operations
 */
public class IncidentDAO {
    
    public List<IncidentReport> findAll() {
        List<IncidentReport> reports = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM IncidentReports ORDER BY DateReported DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToIncident(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving incidents: " + e.getMessage());
        }
        return reports;
    }
    
    public IncidentReport findById(int incidentID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM IncidentReports WHERE IncidentID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, incidentID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToIncident(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving incident: " + e.getMessage());
        }
        return null;
    }
    
    public List<IncidentReport> findByVictimId(int victimID) {
        List<IncidentReport> reports = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM IncidentReports WHERE VictimID = ? ORDER BY DateReported DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        reports.add(mapResultSetToIncident(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving incidents by victim: " + e.getMessage());
        }
        return reports;
    }
    
    public int insert(IncidentReport incident) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO IncidentReports (VictimID, PerpetratorID, AttackTypeID, AdminID, Description, Status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, incident.getVictimID());
                stmt.setInt(2, incident.getPerpetratorID());
                stmt.setInt(3, incident.getAttackTypeID());
                if (incident.getAdminID() != null) {
                    stmt.setInt(4, incident.getAdminID());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                stmt.setString(5, incident.getDescription());
                stmt.setString(6, incident.getStatus() != null ? incident.getStatus() : "Pending");
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating incident: " + e.getMessage());
        }
        return -1;
    }
    
    public boolean updateStatus(int incidentID, String status, Integer adminID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE IncidentReports SET Status = ?, AdminID = ? WHERE IncidentID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                if (adminID != null) {
                    stmt.setInt(2, adminID);
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setInt(3, incidentID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating incident status: " + e.getMessage());
        }
        return false;
    }
    
    private IncidentReport mapResultSetToIncident(ResultSet rs) throws SQLException {
        IncidentReport incident = new IncidentReport();
        incident.setIncidentID(rs.getInt("IncidentID"));
        incident.setVictimID(rs.getInt("VictimID"));
        incident.setPerpetratorID(rs.getInt("PerpetratorID"));
        incident.setAttackTypeID(rs.getInt("AttackTypeID"));
        Integer adminID = rs.getObject("AdminID", Integer.class);
        incident.setAdminID(adminID);
        incident.setDateReported(DateUtils.fromDatabaseFormat(rs.getString("DateReported")));
        incident.setDescription(rs.getString("Description"));
        incident.setStatus(rs.getString("Status"));
        return incident;
    }
}

