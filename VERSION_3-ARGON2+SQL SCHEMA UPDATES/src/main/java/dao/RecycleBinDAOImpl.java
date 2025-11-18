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

/**
 * Implementation of the RecycleBinDAO interface.
 * Handles archiving, restoring, and retrieving recycled Incident Reports and Evidence.
 * This class interacts with the recycle bin tables in the database.
 */
public class RecycleBinDAOImpl implements RecycleBinDAO {

    // SQL for inserting archived reports
    private static final String INSERT_REPORT_SQL = """
        INSERT INTO RecycleBinReports
        (IncidentID, VictimID, PerpetratorID, AttackTypeID, DateReported, Description,
        OriginalStatus, AdminAssignedID, RejectedByAdminID, ArchiveReason, ArchivedAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    // SQL for inserting archived evidence
    private static final String INSERT_EVIDENCE_SQL = """
        INSERT INTO RecycleBinEvidence
        (EvidenceID, IncidentID, EvidenceType, FilePath, SubmissionDate, OriginalStatus,
        AdminAssignedID, RejectedByAdminID, ArchiveReason, ArchivedAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    // SQL for selecting all recycled items
    private static final String SELECT_REPORTS_SQL = "SELECT * FROM RecycleBinReports ORDER BY ArchivedAt DESC";
    private static final String SELECT_EVIDENCE_SQL = "SELECT * FROM RecycleBinEvidence ORDER BY ArchivedAt DESC";

    // SQL for deleting from recycle bin after restore
    private static final String DELETE_RECYCLE_REPORT_SQL = "DELETE FROM RecycleBinReports WHERE BinID = ?";
    private static final String DELETE_RECYCLE_EVIDENCE_SQL = "DELETE FROM RecycleBinEvidence WHERE BinID = ?";

    // SQL for restoring archived reports
    private static final String RESTORE_REPORT_SQL = """
        INSERT INTO IncidentReports
        (IncidentID, VictimID, PerpetratorID, AttackTypeID, AdminID, DateReported, Description, Status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    // SQL for restoring archived evidence
    private static final String RESTORE_EVIDENCE_SQL = """
        INSERT INTO EvidenceUpload
        (EvidenceID, IncidentID, EvidenceType, FilePath, SubmissionDate, VerifiedStatus, AdminID)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    /**
     * Archives an Incident Report into the recycle bin.
     */
    @Override
    public boolean archiveIncidentReport(IncidentReport report, int rejectedByAdminId, String reason) throws SQLException {
        // Use a default reason if none provided
        String archiveReason = (reason == null || reason.isBlank()) ? "Rejected from Pending Reports Review" : reason;

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(INSERT_REPORT_SQL)) {

            // Fill prepared statement values
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

    /**
     * Retrieves all archived reports from the recycle bin.
     */
    @Override
    public List<RecycleBinReport> findAllReports() throws SQLException {
        List<RecycleBinReport> reports = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SELECT_REPORTS_SQL);
            ResultSet rs = stmt.executeQuery()) {

            // Loop through all rows
            while (rs.next()) {
                reports.add(mapRecycleReport(rs));  // Convert row to model object
            }
        }
        return reports;
    }

    /**
     * Retrieves all archived evidence records.
     */
    @Override
    public List<RecycleBinEvidence> findAllEvidence() throws SQLException {
        List<RecycleBinEvidence> evidenceList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(SELECT_EVIDENCE_SQL);
            ResultSet rs = stmt.executeQuery()) {

            // Loop through all rows
            while (rs.next()) {
                evidenceList.add(mapRecycleEvidence(rs));  // Convert row to model object
            }
        }
        return evidenceList;
    }

    /**
     * Restores an archived incident report back into the main IncidentReports table.
     * Uses a manual transaction to ensure atomicity.
     */
    @Override
    public boolean restoreIncidentReport(RecycleBinReport archivedReport) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {

            boolean defaultAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement insertStmt = conn.prepareStatement(RESTORE_REPORT_SQL);
                PreparedStatement deleteStmt = conn.prepareStatement(DELETE_RECYCLE_REPORT_SQL)) {

                // Insert back into main IncidentReports table
                insertStmt.setInt(1, archivedReport.getIncidentID());
                setInteger(insertStmt, 2, archivedReport.getVictimID());
                setInteger(insertStmt, 3, archivedReport.getPerpetratorID());
                setInteger(insertStmt, 4, archivedReport.getAttackTypeID());
                setInteger(insertStmt, 5, archivedReport.getAdminAssignedID());
                insertStmt.setString(6, DateUtils.toDatabaseFormat(
                        archivedReport.getDateReported() != null ? archivedReport.getDateReported() : DateUtils.now()));
                insertStmt.setString(7, archivedReport.getDescription());

                // Restore original status or default to Pending
                String status = archivedReport.getOriginalStatus() != null ? archivedReport.getOriginalStatus() : "Pending";
                insertStmt.setString(8, status);

                insertStmt.executeUpdate();

                // Remove entry from recycle bin
                deleteStmt.setInt(1, archivedReport.getBinID());
                deleteStmt.executeUpdate();

                conn.commit(); // Commit transaction
                conn.setAutoCommit(defaultAutoCommit);
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Rollback on failure
                throw e;
            }
        }
    }

    /**
     * Restores archived evidence back into EvidenceUpload.
     */
    @Override
    public boolean restoreEvidence(RecycleBinEvidence archivedEvidence) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {

            boolean defaultAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement insertStmt = conn.prepareStatement(RESTORE_EVIDENCE_SQL);
                PreparedStatement deleteStmt = conn.prepareStatement(DELETE_RECYCLE_EVIDENCE_SQL)) {

                // Reinsert evidence into main EvidenceUpload table
                insertStmt.setInt(1, archivedEvidence.getEvidenceID());
                setInteger(insertStmt, 2, archivedEvidence.getIncidentID());
                insertStmt.setString(3, archivedEvidence.getEvidenceType());
                insertStmt.setString(4, archivedEvidence.getFilePath());
                insertStmt.setString(5, DateUtils.toDatabaseFormat(
                        archivedEvidence.getSubmissionDate() != null ? archivedEvidence.getSubmissionDate() : DateUtils.now()));

                String status = archivedEvidence.getOriginalStatus() != null ? archivedEvidence.getOriginalStatus() : "Pending";
                insertStmt.setString(6, status);

                setInteger(insertStmt, 7, archivedEvidence.getAdminAssignedID());

                insertStmt.executeUpdate();

                // Remove entry from recycle bin
                deleteStmt.setInt(1, archivedEvidence.getBinID());
                deleteStmt.executeUpdate();

                conn.commit(); // Commit
                conn.setAutoCommit(defaultAutoCommit);
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Rollback if failed
                throw e;
            }
        }
    }

    /**
     * Archives evidence by inserting it into the recycle bin.
     */
    @Override
    public boolean archiveEvidence(Evidence evidence, int rejectedByAdminId, String reason) throws SQLException {
        // Default reason if empty
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

    /**
     * Helper method for setting nullable integers in PreparedStatements.
     */
    private void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER); // Store NULL if value is null
        } else {
            stmt.setInt(index, value);
        }
    }

    /**
     * Maps a ResultSet row to a RecycleBinReport object.
     */
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

    /**
     * Maps a ResultSet row to a RecycleBinEvidence object.
     */
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

    /**
     * Safely retrieves nullable Integer fields from a ResultSet.
     */
    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value; // Convert SQL NULL → Java null
    }
}
