package dao;

import model.Evidence;
import model.IncidentReport;
import model.RecycleBinEvidence;
import model.RecycleBinReport;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for handling archive and recovery operations
 * related to the recycle bin tables (soft-deleted records).
 *
 * This interface supports:
 * - Archiving incident reports and evidence
 * - Fetching archived items
 * - Restoring archived records back to active tables
 */
public interface RecycleBinDAO {

    /**
     * Archives an incident report into the recycle bin table.
     *
     * @param report             the incident report being archived
     * @param rejectedByAdminId  the ID of the admin performing the rejection
     * @param reason             the reason for archiving (e.g., invalid, duplicate)
     * @return true if the archiving was successful, false otherwise
     * @throws SQLException if a database operation fails
     */
    boolean archiveIncidentReport(IncidentReport report, int rejectedByAdminId, String reason) throws SQLException;

    /**
     * Archives an evidence record into the recycle bin table.
     *
     * @param evidence           the evidence record being archived
     * @param rejectedByAdminId  the ID of the admin performing the rejection
     * @param reason             the justification for archiving
     * @return true if the archiving operation succeeds
     * @throws SQLException if a database error occurs
     */
    boolean archiveEvidence(Evidence evidence, int rejectedByAdminId, String reason) throws SQLException;

    /**
     * Retrieves all archived incident reports from the recycle bin.
     *
     * @return a list of archived reports
     * @throws SQLException if a database read operation fails
     */
    List<RecycleBinReport> findAllReports() throws SQLException;

    /**
     * Retrieves all archived evidence records from the recycle bin.
     *
     * @return a list of archived evidence entries
     * @throws SQLException if a database access error occurs
     */
    List<RecycleBinEvidence> findAllEvidence() throws SQLException;

    /**
     * Restores an archived incident report back into the active table.
     *
     * @param archivedReport the archived report to restore
     * @return true if successful, false if the restore operation fails
     * @throws SQLException if a database update operation fails
     */
    boolean restoreIncidentReport(RecycleBinReport archivedReport) throws SQLException;

    /**
     * Restores an archived evidence record back into the active table.
     *
     * @param archivedEvidence the archived evidence entry to restore
     * @return true if the restore was completed successfully
     * @throws SQLException if a database operation fails
     */
    boolean restoreEvidence(RecycleBinEvidence archivedEvidence) throws SQLException;
}
