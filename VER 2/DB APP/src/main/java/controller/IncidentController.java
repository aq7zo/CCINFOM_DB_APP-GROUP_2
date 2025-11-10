package controller;

import model.*;
import utils.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Incident Report management operations
 */
public class IncidentController {
    
    /**
     * Create a new incident report
     */
    public static boolean createIncidentReport(int victimID, int perpetratorID, int attackTypeID, 
                                              Integer adminID, String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO IncidentReports (VictimID, PerpetratorID, AttackTypeID, AdminID, Description, Status) VALUES (?, ?, ?, ?, ?, 'Pending')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                stmt.setInt(2, perpetratorID);
                stmt.setInt(3, attackTypeID);
                if (adminID != null) {
                    stmt.setInt(4, adminID);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                stmt.setString(5, description);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Update perpetrator's last incident date
                    PerpetratorController.updateLastIncidentDate(perpetratorID);
                    
                    // Check for auto-escalation
                    PerpetratorController.checkAndAutoEscalate();
                    
                    // Check for auto-flagging victims
                    VictimController.checkAndAutoFlagVictims();
                    
                    System.out.println("Incident report created successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating incident report: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get all incident reports
     */
    public static List<IncidentReport> getAllIncidentReports() {
        List<IncidentReport> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM IncidentReports ORDER BY DateReported DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    IncidentReport report = new IncidentReport();
                    report.setIncidentID(rs.getInt("IncidentID"));
                    report.setVictimID(rs.getInt("VictimID"));
                    report.setPerpetratorID(rs.getInt("PerpetratorID"));
                    report.setAttackTypeID(rs.getInt("AttackTypeID"));
                    Integer adminID = rs.getObject("AdminID", Integer.class);
                    report.setAdminID(adminID);
                    report.setDateReported(DateUtils.fromDatabaseFormat(rs.getString("DateReported")));
                    report.setDescription(rs.getString("Description"));
                    report.setStatus(rs.getString("Status"));
                    reports.add(report);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving incident reports: " + e.getMessage());
        }
        
        return reports;
    }
    
    /**
     * Get incident report by ID
     */
    public static IncidentReport getIncidentReportById(int incidentID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM IncidentReports WHERE IncidentID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, incidentID);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        IncidentReport report = new IncidentReport();
                        report.setIncidentID(rs.getInt("IncidentID"));
                        report.setVictimID(rs.getInt("VictimID"));
                        report.setPerpetratorID(rs.getInt("PerpetratorID"));
                        report.setAttackTypeID(rs.getInt("AttackTypeID"));
                        Integer adminID = rs.getObject("AdminID", Integer.class);
                        report.setAdminID(adminID);
                        report.setDateReported(DateUtils.fromDatabaseFormat(rs.getString("DateReported")));
                        report.setDescription(rs.getString("Description"));
                        report.setStatus(rs.getString("Status"));
                        return report;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving incident report: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Validate an incident report
     */
    public static boolean validateIncidentReport(int incidentID, int adminID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE IncidentReports SET Status = 'Validated', AdminID = ? WHERE IncidentID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, adminID);
                stmt.setInt(2, incidentID);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Incident report validated successfully.");
                    return true;
                } else {
                    System.out.println("Incident report not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error validating incident report: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get incidents by victim ID
     */
    public static List<IncidentReport> getIncidentsByVictim(int victimID) {
        List<IncidentReport> reports = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM IncidentReports WHERE VictimID = ? ORDER BY DateReported DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        IncidentReport report = new IncidentReport();
                        report.setIncidentID(rs.getInt("IncidentID"));
                        report.setVictimID(rs.getInt("VictimID"));
                        report.setPerpetratorID(rs.getInt("PerpetratorID"));
                        report.setAttackTypeID(rs.getInt("AttackTypeID"));
                        Integer adminID = rs.getObject("AdminID", Integer.class);
                        report.setAdminID(adminID);
                        report.setDateReported(DateUtils.fromDatabaseFormat(rs.getString("DateReported")));
                        report.setDescription(rs.getString("Description"));
                        report.setStatus(rs.getString("Status"));
                        reports.add(report);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving incidents: " + e.getMessage());
        }
        
        return reports;
    }
}

