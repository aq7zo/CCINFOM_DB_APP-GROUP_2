package dao;

import model.Evidence;
import model.IncidentReport;
import model.RecycleBinEvidence;
import model.RecycleBinReport;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecycleBinDAOImpl implements RecycleBinDAO {

    private static final String INSERT_REPORT_SQL = """
        INSERT INTO RecycleBinReports
        (IncidentID, VictimID, PerpetratorID, AttackTypeID, DateReported, Description,
         OriginalStatus, AdminAssignedID, RejectedByAdminID, ArchiveReason, ArchivedAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String INSERT_EVIDENCE_SQL = """
        INSERT INTO RecycleBinEvidence
        (EvidenceID, IncidentID, EvidenceType, FilePath, SubmissionDate, OriginalStatus,
         AdminAssignedID, RejectedByAdminID, ArchiveReason, ArchivedAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String SELECT_REPORTS_SQL = "SELECT * FROM RecycleBinReports ORDER BY ArchivedAt DESC";
    private static final String SELECT_EVIDENCE_SQL = "SELECT * FROM RecycleBinEvidence ORDER BY ArchivedAt DESC";
    private static final String DELETE_RECYCLE_REPORT_SQL = "DELETE FROM RecycleBinReports WHERE BinID = ?";
    private static final String DELETE_RECYCLE_EVIDENCE_SQL = "DELETE FROM RecycleBinEvidence WHERE BinID = ?";

    private static final String RESTORE_REPORT_SQL = """
        INSERT INTO IncidentReports
        (IncidentID, VictimID, PerpetratorID, AttackTypeID, AdminID, DateReported, Description, Status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
    private static final String UPDATE_REPORT_SQL = """
        UPDATE IncidentReports
        SET VictimID = ?, PerpetratorID = ?, AttackTypeID = ?, AdminID = ?, 
            DateReported = ?, Description = ?, Status = ?
        WHERE IncidentID = ?
        """;
    
    private static final String CHECK_REPORT_EXISTS_SQL = """
        SELECT COUNT(*) FROM IncidentReports WHERE IncidentID = ?
        """;

    private static final String RESTORE_EVIDENCE_SQL = """
        INSERT INTO EvidenceUpload
        (EvidenceID, IncidentID, EvidenceType, FilePath, SubmissionDate, VerifiedStatus, AdminID)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
    
    private static final String UPDATE_EVIDENCE_SQL = """
        UPDATE EvidenceUpload
        SET IncidentID = ?, EvidenceType = ?, FilePath = ?, SubmissionDate = ?, 
            VerifiedStatus = ?, AdminID = ?
        WHERE EvidenceID = ?
        """;
    
    private static final String CHECK_EVIDENCE_EXISTS_SQL = """
        SELECT COUNT(*) FROM EvidenceUpload WHERE EvidenceID = ?
        """;

    @Override
    public boolean archiveIncidentReport(IncidentReport report, int rejectedByAdminId, String reason) throws SQLException {
        String archiveReason = (reason == null || reason.isBlank()) ? "Rejected from Pending Reports Review" : reason;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_REPORT_SQL)) {

            stmt.setInt(1, report.getIncidentID());
            stmt.setInt(2, report.getVictimID());
            stmt.setInt(3, report.getPerpetratorID());
            stmt.setInt(4, report.getAttackTypeID());
            stmt.setString(5, DateUtils.toDatabaseFormat(report.getDateReported()));
            stmt.setString(6, report.getDescription());
            stmt.setString(7, report.getStatus());
            setInteger(stmt, 8, report.getAdminID());
            stmt.setInt(9, rejectedByAdminId);
            stmt.setString(10, archiveReason);
            stmt.setString(11, DateUtils.toDatabaseFormat(DateUtils.now()));

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<RecycleBinReport> findAllReports() throws SQLException {
        List<RecycleBinReport> reports = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_REPORTS_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reports.add(mapRecycleReport(rs));
            }
        }
        return reports;
    }

    @Override
    public List<RecycleBinEvidence> findAllEvidence() throws SQLException {
        List<RecycleBinEvidence> evidenceList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_EVIDENCE_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                evidenceList.add(mapRecycleEvidence(rs));
            }
        }
        return evidenceList;
    }

    @Override
    public boolean restoreIncidentReport(RecycleBinReport archivedReport) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean defaultAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            // Check if report already exists
            boolean reportExists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(CHECK_REPORT_EXISTS_SQL)) {
                checkStmt.setInt(1, archivedReport.getIncidentID());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        reportExists = rs.getInt(1) > 0;
                    }
                }
            }
            
            try {
                String status = archivedReport.getOriginalStatus() != null ? archivedReport.getOriginalStatus() : "Pending";
                String dateReported = DateUtils.toDatabaseFormat(
                        archivedReport.getDateReported() != null ? archivedReport.getDateReported() : DateUtils.now());
                
                if (reportExists) {
                    // Update existing report
                    try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_REPORT_SQL);
                         PreparedStatement deleteStmt = conn.prepareStatement(DELETE_RECYCLE_REPORT_SQL)) {
                        
                        setInteger(updateStmt, 1, archivedReport.getVictimID());
                        setInteger(updateStmt, 2, archivedReport.getPerpetratorID());
                        setInteger(updateStmt, 3, archivedReport.getAttackTypeID());
                        setInteger(updateStmt, 4, archivedReport.getAdminAssignedID());
                        updateStmt.setString(5, dateReported);
                        updateStmt.setString(6, archivedReport.getDescription());
                        updateStmt.setString(7, status);
                        updateStmt.setInt(8, archivedReport.getIncidentID());
                        
                        int rowsUpdated = updateStmt.executeUpdate();
                        if (rowsUpdated == 0) {
                            throw new SQLException("Failed to update report #" + archivedReport.getIncidentID());
                        }
                        
                        deleteStmt.setInt(1, archivedReport.getBinID());
                        deleteStmt.executeUpdate();
                        
                        conn.commit();
                        conn.setAutoCommit(defaultAutoCommit);
                        return true;
                    }
                } else {
                    // Insert new report (shouldn't happen normally, but handle it)
                    try (PreparedStatement insertStmt = conn.prepareStatement(RESTORE_REPORT_SQL);
                         PreparedStatement deleteStmt = conn.prepareStatement(DELETE_RECYCLE_REPORT_SQL)) {
                        
                        insertStmt.setInt(1, archivedReport.getIncidentID());
                        setInteger(insertStmt, 2, archivedReport.getVictimID());
                        setInteger(insertStmt, 3, archivedReport.getPerpetratorID());
                        setInteger(insertStmt, 4, archivedReport.getAttackTypeID());
                        setInteger(insertStmt, 5, archivedReport.getAdminAssignedID());
                        insertStmt.setString(6, dateReported);
                        insertStmt.setString(7, archivedReport.getDescription());
                        insertStmt.setString(8, status);
                        insertStmt.executeUpdate();
                        
                        deleteStmt.setInt(1, archivedReport.getBinID());
                        deleteStmt.executeUpdate();
                        
                        conn.commit();
                        conn.setAutoCommit(defaultAutoCommit);
                        return true;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean restoreEvidence(RecycleBinEvidence archivedEvidence) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean defaultAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            // Check if evidence already exists
            boolean evidenceExists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(CHECK_EVIDENCE_EXISTS_SQL)) {
                checkStmt.setInt(1, archivedEvidence.getEvidenceID());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        evidenceExists = rs.getInt(1) > 0;
                    }
                }
            }
            
            try {
                String status = archivedEvidence.getOriginalStatus() != null ? archivedEvidence.getOriginalStatus() : "Pending";
                String submissionDate = DateUtils.toDatabaseFormat(
                        archivedEvidence.getSubmissionDate() != null ? archivedEvidence.getSubmissionDate() : DateUtils.now());
                
                if (evidenceExists) {
                    // Update existing evidence
                    try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_EVIDENCE_SQL);
                         PreparedStatement deleteStmt = conn.prepareStatement(DELETE_RECYCLE_EVIDENCE_SQL)) {
                        
                        setInteger(updateStmt, 1, archivedEvidence.getIncidentID());
                        updateStmt.setString(2, archivedEvidence.getEvidenceType());
                        updateStmt.setString(3, archivedEvidence.getFilePath());
                        updateStmt.setString(4, submissionDate);
                        updateStmt.setString(5, status);
                        setInteger(updateStmt, 6, archivedEvidence.getAdminAssignedID());
                        updateStmt.setInt(7, archivedEvidence.getEvidenceID());
                        
                        int rowsUpdated = updateStmt.executeUpdate();
                        if (rowsUpdated == 0) {
                            throw new SQLException("Failed to update evidence #" + archivedEvidence.getEvidenceID());
                        }
                        
                        deleteStmt.setInt(1, archivedEvidence.getBinID());
                        deleteStmt.executeUpdate();
                        
                        conn.commit();
                        conn.setAutoCommit(defaultAutoCommit);
                        return true;
                    }
                } else {
                    // Insert new evidence
                    try (PreparedStatement insertStmt = conn.prepareStatement(RESTORE_EVIDENCE_SQL);
                         PreparedStatement deleteStmt = conn.prepareStatement(DELETE_RECYCLE_EVIDENCE_SQL)) {
                        
                        insertStmt.setInt(1, archivedEvidence.getEvidenceID());
                        setInteger(insertStmt, 2, archivedEvidence.getIncidentID());
                        insertStmt.setString(3, archivedEvidence.getEvidenceType());
                        insertStmt.setString(4, archivedEvidence.getFilePath());
                        insertStmt.setString(5, submissionDate);
                        insertStmt.setString(6, status);
                        setInteger(insertStmt, 7, archivedEvidence.getAdminAssignedID());
                        insertStmt.executeUpdate();
                        
                        deleteStmt.setInt(1, archivedEvidence.getBinID());
                        deleteStmt.executeUpdate();
                        
                        conn.commit();
                        conn.setAutoCommit(defaultAutoCommit);
                        return true;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean archiveEvidence(Evidence evidence, int rejectedByAdminId, String reason) throws SQLException {
        String archiveReason = (reason == null || reason.isBlank()) ? "Rejected from Pending Evidence Review" : reason;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_EVIDENCE_SQL)) {

            stmt.setInt(1, evidence.getEvidenceID());
            stmt.setInt(2, evidence.getIncidentID());
            stmt.setString(3, evidence.getEvidenceType());
            stmt.setString(4, evidence.getFilePath());
            stmt.setString(5, DateUtils.toDatabaseFormat(evidence.getSubmissionDate()));
            stmt.setString(6, evidence.getVerifiedStatus());
            setInteger(stmt, 7, evidence.getAdminID());
            stmt.setInt(8, rejectedByAdminId);
            stmt.setString(9, archiveReason);
            stmt.setString(10, DateUtils.toDatabaseFormat(DateUtils.now()));

            return stmt.executeUpdate() > 0;
        }
    }

    private void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }

    private RecycleBinReport mapRecycleReport(ResultSet rs) throws SQLException {
        RecycleBinReport report = new RecycleBinReport();
        report.setBinID(rs.getInt("BinID"));
        report.setIncidentID(rs.getInt("IncidentID"));
        report.setVictimID(getInteger(rs, "VictimID"));
        report.setPerpetratorID(getInteger(rs, "PerpetratorID"));
        report.setAttackTypeID(getInteger(rs, "AttackTypeID"));
        report.setDateReported(DateUtils.fromDatabaseFormat(rs.getString("DateReported")));
        report.setDescription(rs.getString("Description"));
        report.setOriginalStatus(rs.getString("OriginalStatus"));
        report.setAdminAssignedID(getInteger(rs, "AdminAssignedID"));
        report.setRejectedByAdminID(rs.getInt("RejectedByAdminID"));
        report.setArchiveReason(rs.getString("ArchiveReason"));
        report.setArchivedAt(DateUtils.fromDatabaseFormat(rs.getString("ArchivedAt")));
        return report;
    }

    private RecycleBinEvidence mapRecycleEvidence(ResultSet rs) throws SQLException {
        RecycleBinEvidence evidence = new RecycleBinEvidence();
        evidence.setBinID(rs.getInt("BinID"));
        evidence.setEvidenceID(rs.getInt("EvidenceID"));
        evidence.setIncidentID(getInteger(rs, "IncidentID"));
        evidence.setEvidenceType(rs.getString("EvidenceType"));
        evidence.setFilePath(rs.getString("FilePath"));
        evidence.setSubmissionDate(DateUtils.fromDatabaseFormat(rs.getString("SubmissionDate")));
        evidence.setOriginalStatus(rs.getString("OriginalStatus"));
        evidence.setAdminAssignedID(getInteger(rs, "AdminAssignedID"));
        evidence.setRejectedByAdminID(rs.getInt("RejectedByAdminID"));
        evidence.setArchiveReason(rs.getString("ArchiveReason"));
        evidence.setArchivedAt(DateUtils.fromDatabaseFormat(rs.getString("ArchivedAt")));
        return evidence;
    }

    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}

