package controller;

import model.*;
import utils.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Evidence management operations
 */
public class EvidenceController {
    
    /**
     * Upload evidence for an incident
     */
    public static boolean uploadEvidence(int incidentID, String evidenceType, String filePath, Integer adminID) {
        if (!ValidationUtils.isNotEmpty(evidenceType) || !ValidationUtils.isNotEmpty(filePath)) {
            System.out.println("Invalid input. Evidence type and file path are required.");
            return false;
        }
        
        if (!ValidationUtils.isValidEnumValue(evidenceType, 
                new String[]{"Screenshot", "Email", "File", "Chat Log"})) {
            System.out.println("Invalid evidence type. Must be: Screenshot, Email, File, or Chat Log");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO EvidenceUpload (IncidentID, EvidenceType, FilePath, VerifiedStatus, AdminID) VALUES (?, ?, ?, 'Pending', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, incidentID);
                stmt.setString(2, evidenceType);
                stmt.setString(3, filePath);
                if (adminID != null) {
                    stmt.setInt(4, adminID);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Evidence uploaded successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error uploading evidence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get all evidence
     */
    public static List<Evidence> getAllEvidence() {
        List<Evidence> evidenceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM EvidenceUpload ORDER BY SubmissionDate DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Evidence evidence = new Evidence();
                    evidence.setEvidenceID(rs.getInt("EvidenceID"));
                    evidence.setIncidentID(rs.getInt("IncidentID"));
                    evidence.setEvidenceType(rs.getString("EvidenceType"));
                    evidence.setFilePath(rs.getString("FilePath"));
                    evidence.setSubmissionDate(DateUtils.fromDatabaseFormat(rs.getString("SubmissionDate")));
                    evidence.setVerifiedStatus(rs.getString("VerifiedStatus"));
                    Integer adminID = rs.getObject("AdminID", Integer.class);
                    evidence.setAdminID(adminID);
                    evidenceList.add(evidence);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving evidence: " + e.getMessage());
        }
        
        return evidenceList;
    }
    
    /**
     * Get evidence by incident ID
     */
    public static List<Evidence> getEvidenceByIncident(int incidentID) {
        List<Evidence> evidenceList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM EvidenceUpload WHERE IncidentID = ? ORDER BY SubmissionDate DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, incidentID);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Evidence evidence = new Evidence();
                        evidence.setEvidenceID(rs.getInt("EvidenceID"));
                        evidence.setIncidentID(rs.getInt("IncidentID"));
                        evidence.setEvidenceType(rs.getString("EvidenceType"));
                        evidence.setFilePath(rs.getString("FilePath"));
                        evidence.setSubmissionDate(DateUtils.fromDatabaseFormat(rs.getString("SubmissionDate")));
                        evidence.setVerifiedStatus(rs.getString("VerifiedStatus"));
                        Integer adminID = rs.getObject("AdminID", Integer.class);
                        evidence.setAdminID(adminID);
                        evidenceList.add(evidence);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving evidence: " + e.getMessage());
        }
        
        return evidenceList;
    }
    
    /**
     * Update evidence verification status
     */
    public static boolean updateEvidenceStatus(int evidenceID, String verifiedStatus, int adminID) {
        if (!ValidationUtils.isValidEnumValue(verifiedStatus, 
                new String[]{"Pending", "Verified", "Rejected"})) {
            System.out.println("Invalid status. Must be: Pending, Verified, or Rejected");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE EvidenceUpload SET VerifiedStatus = ?, AdminID = ? WHERE EvidenceID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, verifiedStatus);
                stmt.setInt(2, adminID);
                stmt.setInt(3, evidenceID);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Evidence status updated successfully.");
                    
                    // If verified, check if this strengthens a malicious pattern
                    if ("Verified".equals(verifiedStatus)) {
                        checkMaliciousPattern(evidenceID);
                    }
                    
                    return true;
                } else {
                    System.out.println("Evidence not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating evidence status: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if verified evidence strengthens a malicious pattern
     */
    private static void checkMaliciousPattern(int evidenceID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get the incident and perpetrator for this evidence
            String sql = """
                SELECT ir.PerpetratorID, p.ThreatLevel
                FROM EvidenceUpload e
                JOIN IncidentReports ir ON e.IncidentID = ir.IncidentID
                JOIN Perpetrators p ON ir.PerpetratorID = p.PerpetratorID
                WHERE e.EvidenceID = ? AND e.VerifiedStatus = 'Verified'
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, evidenceID);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int perpetratorID = rs.getInt("PerpetratorID");
                        String threatLevel = rs.getString("ThreatLevel");
                        
                        // Count verified evidence for this perpetrator
                        String countSql = """
                            SELECT COUNT(DISTINCT e.EvidenceID) as VerifiedCount
                            FROM EvidenceUpload e
                            JOIN IncidentReports ir ON e.IncidentID = ir.IncidentID
                            WHERE ir.PerpetratorID = ? AND e.VerifiedStatus = 'Verified'
                            """;
                        
                        try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                            countStmt.setInt(1, perpetratorID);
                            
                            try (ResultSet countRs = countStmt.executeQuery()) {
                                if (countRs.next() && countRs.getInt("VerifiedCount") >= 3) {
                                    if (!"Malicious".equals(threatLevel)) {
                                        PerpetratorController.updateThreatLevel(perpetratorID, "Malicious", null);
                                        System.out.println("⚠️  Verified evidence pattern detected. Perpetrator escalated to Malicious.");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking malicious pattern: " + e.getMessage());
        }
    }
}

