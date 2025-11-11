package utils;

import java.sql.*;
import model.DatabaseConnection;

/**
 * Utility class for generating various reports
 */
public class ReportGenerator {
    
    /**
     * Generate Monthly Attack Trends Report
     */
    public static String generateMonthlyAttackTrends(int year, int month) {
        StringBuilder report = new StringBuilder();
        report.append("\n=== MONTHLY ATTACK TRENDS REPORT ===\n");
        report.append(String.format("Period: %d-%02d\n\n", year, month));
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Group by attack type
            String sql = """
                SELECT at.AttackName, at.SeverityLevel, COUNT(ir.IncidentID) as IncidentCount,
                       COUNT(DISTINCT ir.VictimID) as VictimCount,
                       COUNT(e.EvidenceID) as EvidenceCount
                FROM IncidentReports ir
                JOIN AttackTypes at ON ir.AttackTypeID = at.AttackTypeID
                LEFT JOIN EvidenceUpload e ON ir.IncidentID = e.IncidentID
                WHERE YEAR(ir.DateReported) = ? AND MONTH(ir.DateReported) = ?
                GROUP BY at.AttackTypeID, at.AttackName, at.SeverityLevel
                ORDER BY IncidentCount DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    report.append("ATTACK TYPE BREAKDOWN:\n");
                    report.append(String.format("%-30s %-15s %-15s %-15s %-15s\n", 
                        "Attack Type", "Severity", "Incidents", "Victims", "Evidence"));
                    report.append("-".repeat(90) + "\n");
                    
                    while (rs.next()) {
                        report.append(String.format("%-30s %-15s %-15d %-15d %-15d\n",
                            rs.getString("AttackName"),
                            rs.getString("SeverityLevel"),
                            rs.getInt("IncidentCount"),
                            rs.getInt("VictimCount"),
                            rs.getInt("EvidenceCount")));
                    }
                }
            }
            
            // Time of day analysis
            sql = """
                SELECT HOUR(DateReported) as Hour, COUNT(*) as Count
                FROM IncidentReports
                WHERE YEAR(DateReported) = ? AND MONTH(DateReported) = ?
                GROUP BY HOUR(DateReported)
                ORDER BY Hour
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    report.append("\nTIME OF DAY ANALYSIS:\n");
                    report.append(String.format("%-10s %-15s\n", "Hour", "Incidents"));
                    report.append("-".repeat(25) + "\n");
                    
                    while (rs.next()) {
                        report.append(String.format("%-10d %-15d\n",
                            rs.getInt("Hour"),
                            rs.getInt("Count")));
                    }
                }
            }
            
        } catch (SQLException e) {
            report.append("Error generating report: ").append(e.getMessage());
        }
        
        return report.toString();
    }
    
    /**
     * Generate Top Perpetrators Report
     */
    public static String generateTopPerpetrators(int year, int month) {
        StringBuilder report = new StringBuilder();
        report.append("\n=== TOP PERPETRATORS REPORT ===\n");
        report.append(String.format("Period: %d-%02d\n\n", year, month));
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT p.PerpetratorID, p.Identifier, p.IdentifierType, p.ThreatLevel,
                       COUNT(ir.IncidentID) as IncidentCount,
                       COUNT(DISTINCT ir.VictimID) as VictimCount,
                       GROUP_CONCAT(DISTINCT at.AttackName) as AttackTypes
                FROM Perpetrators p
                JOIN IncidentReports ir ON p.PerpetratorID = ir.PerpetratorID
                JOIN AttackTypes at ON ir.AttackTypeID = at.AttackTypeID
                WHERE YEAR(ir.DateReported) = ? AND MONTH(ir.DateReported) = ?
                GROUP BY p.PerpetratorID, p.Identifier, p.IdentifierType, p.ThreatLevel
                ORDER BY IncidentCount DESC
                LIMIT 10
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    report.append(String.format("%-5s %-30s %-20s %-15s %-12s %-12s %-30s\n",
                        "ID", "Identifier", "Type", "Threat Level", "Incidents", "Victims", "Attack Types"));
                    report.append("-".repeat(125) + "\n");
                    
                    int rank = 1;
                    while (rs.next()) {
                        report.append(String.format("%-5d %-30s %-20s %-15s %-12d %-12d %-30s\n",
                            rank++,
                            rs.getString("Identifier"),
                            rs.getString("IdentifierType"),
                            rs.getString("ThreatLevel"),
                            rs.getInt("IncidentCount"),
                            rs.getInt("VictimCount"),
                            rs.getString("AttackTypes") != null ? rs.getString("AttackTypes") : "N/A"));
                    }
                }
            }
        } catch (SQLException e) {
            report.append("Error generating report: ").append(e.getMessage());
        }
        
        return report.toString();
    }
    
    /**
     * Generate Victim Activity Report
     */
    public static String generateVictimActivity(int year, int month) {
        StringBuilder report = new StringBuilder();
        report.append("\n=== VICTIM ACTIVITY REPORT ===\n");
        report.append(String.format("Period: %d-%02d\n\n", year, month));
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT v.VictimID, v.Name, v.AccountStatus,
                       COUNT(ir.IncidentID) as IncidentCount,
                       COUNT(DISTINCT ir.PerpetratorID) as PerpetratorCount
                FROM Victims v
                LEFT JOIN IncidentReports ir ON v.VictimID = ir.VictimID
                    AND YEAR(ir.DateReported) = ? AND MONTH(ir.DateReported) = ?
                GROUP BY v.VictimID, v.Name, v.AccountStatus
                HAVING IncidentCount > 0
                ORDER BY IncidentCount DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    report.append(String.format("%-10s %-30s %-15s %-15s %-20s\n",
                        "Victim ID", "Name", "Status", "Incidents", "Unique Perpetrators"));
                    report.append("-".repeat(90) + "\n");
                    
                    while (rs.next()) {
                        String name = rs.getString("Name");
                        // Anonymize for privacy (RA 10173 compliance)
                        if (name != null && name.length() > 1) {
                            name = SecurityUtils.anonymizeName(name);
                        }
                        
                        report.append(String.format("%-10d %-30s %-15s %-15d %-20d\n",
                            rs.getInt("VictimID"),
                            name,
                            rs.getString("AccountStatus"),
                            rs.getInt("IncidentCount"),
                            rs.getInt("PerpetratorCount")));
                    }
                }
            }
            
            // Highlight frequently targeted victims (>5 incidents)
            sql = """
                SELECT v.VictimID, v.Name, COUNT(ir.IncidentID) as IncidentCount
                FROM Victims v
                JOIN IncidentReports ir ON v.VictimID = ir.VictimID
                WHERE YEAR(ir.DateReported) = ? AND MONTH(ir.DateReported) = ?
                GROUP BY v.VictimID, v.Name
                HAVING IncidentCount > 5
                ORDER BY IncidentCount DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        report.append("\n⚠️  FREQUENTLY TARGETED VICTIMS (>5 incidents):\n");
                        report.append(String.format("%-10s %-30s %-15s\n",
                            "Victim ID", "Name", "Incidents"));
                        report.append("-".repeat(55) + "\n");
                        
                        do {
                            String name = rs.getString("Name");
                            if (name != null && name.length() > 1) {
                                name = SecurityUtils.anonymizeName(name);
                            }
                            
                            report.append(String.format("%-10d %-30s %-15d\n",
                                rs.getInt("VictimID"),
                                name,
                                rs.getInt("IncidentCount")));
                        } while (rs.next());
                    }
                }
            }
        } catch (SQLException e) {
            report.append("Error generating report: ").append(e.getMessage());
        }
        
        return report.toString();
    }
    
    /**
     * Generate Incident Evidence Summary Report
     */
    public static String generateEvidenceSummary() {
        StringBuilder report = new StringBuilder();
        report.append("\n=== INCIDENT EVIDENCE SUMMARY REPORT ===\n\n");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT e.EvidenceID, e.IncidentID, e.EvidenceType, e.FilePath,
                       e.SubmissionDate, e.VerifiedStatus,
                       a.Name as AdminName, a.Role as AdminRole,
                       ir.Description as IncidentDescription
                FROM EvidenceUpload e
                JOIN IncidentReports ir ON e.IncidentID = ir.IncidentID
                LEFT JOIN Administrators a ON e.AdminID = a.AdminID
                ORDER BY e.SubmissionDate DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                report.append(String.format("%-12s %-12s %-15s %-30s %-20s %-15s %-30s\n",
                    "Evidence ID", "Incident ID", "Type", "File Path", "Submission Date",
                    "Status", "Reviewing Admin"));
                report.append("-".repeat(135) + "\n");
                
                while (rs.next()) {
                    report.append(String.format("%-12d %-12d %-15s %-30s %-20s %-15s %-30s\n",
                        rs.getInt("EvidenceID"),
                        rs.getInt("IncidentID"),
                        rs.getString("EvidenceType"),
                        rs.getString("FilePath").length() > 30 ? 
                            rs.getString("FilePath").substring(0, 27) + "..." : 
                            rs.getString("FilePath"),
                        DateUtils.formatForDisplay(DateUtils.fromDatabaseFormat(rs.getString("SubmissionDate"))),
                        rs.getString("VerifiedStatus"),
                        rs.getString("AdminName") != null ? rs.getString("AdminName") : "N/A"));
                }
            }
            
            // Summary statistics
            sql = """
                SELECT VerifiedStatus, COUNT(*) as Count
                FROM EvidenceUpload
                GROUP BY VerifiedStatus
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                report.append("\nSUMMARY STATISTICS:\n");
                report.append(String.format("%-20s %-15s\n", "Status", "Count"));
                report.append("-".repeat(35) + "\n");
                
                while (rs.next()) {
                    report.append(String.format("%-20s %-15d\n",
                        rs.getString("VerifiedStatus"),
                        rs.getInt("Count")));
                }
            }
        } catch (SQLException e) {
            report.append("Error generating report: ").append(e.getMessage());
        }
        
        return report.toString();
    }
}

