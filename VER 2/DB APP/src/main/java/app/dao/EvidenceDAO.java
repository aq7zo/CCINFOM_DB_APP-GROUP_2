package app.dao;

import app.config.DatabaseConnection;
import app.models.Evidence;
import app.utils.DateUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Evidence database operations
 */
public class EvidenceDAO {
    
    public List<Evidence> findAll() {
        List<Evidence> evidenceList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM EvidenceUpload ORDER BY SubmissionDate DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evidenceList.add(mapResultSetToEvidence(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving evidence: " + e.getMessage());
        }
        return evidenceList;
    }
    
    public List<Evidence> findByIncidentId(int incidentID) {
        List<Evidence> evidenceList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM EvidenceUpload WHERE IncidentID = ? ORDER BY SubmissionDate DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, incidentID);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        evidenceList.add(mapResultSetToEvidence(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving evidence by incident: " + e.getMessage());
        }
        return evidenceList;
    }
    
    public int insert(Evidence evidence) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO EvidenceUpload (IncidentID, EvidenceType, FilePath, VerifiedStatus, AdminID) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, evidence.getIncidentID());
                stmt.setString(2, evidence.getEvidenceType());
                stmt.setString(3, evidence.getFilePath());
                stmt.setString(4, evidence.getVerifiedStatus() != null ? evidence.getVerifiedStatus() : "Pending");
                if (evidence.getAdminID() != null) {
                    stmt.setInt(5, evidence.getAdminID());
                } else {
                    stmt.setNull(5, Types.INTEGER);
                }
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating evidence: " + e.getMessage());
        }
        return -1;
    }
    
    public boolean updateStatus(int evidenceID, String status, Integer adminID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE EvidenceUpload SET VerifiedStatus = ?, AdminID = ? WHERE EvidenceID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                if (adminID != null) {
                    stmt.setInt(2, adminID);
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setInt(3, evidenceID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating evidence status: " + e.getMessage());
        }
        return false;
    }
    
    private Evidence mapResultSetToEvidence(ResultSet rs) throws SQLException {
        Evidence evidence = new Evidence();
        evidence.setEvidenceID(rs.getInt("EvidenceID"));
        evidence.setIncidentID(rs.getInt("IncidentID"));
        evidence.setEvidenceType(rs.getString("EvidenceType"));
        evidence.setFilePath(rs.getString("FilePath"));
        evidence.setSubmissionDate(DateUtils.fromDatabaseFormat(rs.getString("SubmissionDate")));
        evidence.setVerifiedStatus(rs.getString("VerifiedStatus"));
        Integer adminID = rs.getObject("AdminID", Integer.class);
        evidence.setAdminID(adminID);
        return evidence;
    }
}

