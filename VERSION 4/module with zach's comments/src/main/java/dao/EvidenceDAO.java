package dao;

import model.Evidence;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for managing Evidence uploads and verification workflow.
 *
 * Handles all database operations related to victim-submitted evidence files
 * (screenshots, chat logs, documents, etc.) that support incident reports.
 *
 * Key responsibilities:
 * - Securely storing new evidence with proper incident linkage
 * - Retrieving evidence for admin review and victim viewing
 * - Supporting the verification workflow (Pending → Verified / Rejected)
 * - Enforcing admin-only actions (verify, delete)
 *
 * All methods throw SQLException to allow service layer to handle errors,
 * logging, and user feedback appropriately.
 */
public interface EvidenceDAO {

    /**
     * Uploads a new piece of evidence submitted by a victim.
     *
     * Called during incident report submission when victims attach files.
     * Stores file metadata and server path — actual file is saved on disk.
     *
     * @param evidence fully populated Evidence object (incidentID, filePath, type, etc.)
     * @return true if upload was successful and ID was generated
     * @throws SQLException if database error occurs
     */
    boolean upload(Evidence evidence) throws SQLException;

    /**
     * Retrieves a specific evidence record by its unique ID.
     *
     * Used when displaying evidence details or downloading the file.
     *
     * @param evidenceID the primary key of the evidence
     * @return Evidence object or null if not found
     * @throws SQLException if database error occurs
     */
    Evidence findById(int evidenceID) throws SQLException;

    /**
     * Returns all evidence associated with a specific incident.
     *
     * Used in incident detail views for both victims and administrators.
     *
     * @param incidentID the parent incident
     * @return list of Evidence (may be empty)
     * @throws SQLException if database error occurs
     */
    List<Evidence> findByIncidentID(int incidentID) throws SQLException;

    /**
     * Retrieves all evidence currently awaiting admin verification.
     *
     * Core method for the admin "Evidence Review" queue.
     * Typically filters where VerifiedStatus = 'Pending'.
     *
     * @return list of pending evidence items, usually ordered by submission date
     * @throws SQLException if database error occurs
     */
    List<Evidence> findPending() throws SQLException;

    /**
     * Updates the verification status of evidence (Verified or Rejected).
     *
     * Called by administrators during review. Records which admin performed the action.
     *
     * @param evidenceID the evidence to verify
     * @param status     "Verified" or "Rejected"
     * @param adminID    the administrator performing the review
     * @return true if status was successfully updated
     * @throws SQLException if database error occurs
     */
    boolean verify(int evidenceID, String status, int adminID) throws SQLException;

    /**
     * Permanently deletes an evidence record (and ideally the file from disk).
     *
     * Admin-only operation — used for removing inappropriate or irrelevant submissions.
     * Should be audited and possibly soft-deleted in production systems.
     *
     * @param evidenceID the evidence to remove
     * @return true if deletion was successful
     * @throws SQLException if database error occurs
     */
    boolean delete(int evidenceID) throws SQLException;
}