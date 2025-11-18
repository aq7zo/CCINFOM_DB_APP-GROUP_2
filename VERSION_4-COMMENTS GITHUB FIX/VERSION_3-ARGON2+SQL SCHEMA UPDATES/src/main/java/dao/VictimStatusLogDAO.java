package dao;

import model.VictimStatusLog;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO Interface for VictimStatusLog, audit trail of all victim account status changes.
 *
 * This interface defines operations for creating and retrieving immutable log entries
 * whenever a victim's AccountStatus is modified (e.g., Active → Under Investigation → Resolved).
 *
 * Purpose:
 * - Ensure full traceability of status changes
 * - Support administrative oversight and accountability
 * - Comply with data privacy and incident reporting regulations (e.g., RA 10173)
 * - Enable detailed victim history and timeline reconstruction
 *
 * All methods throw SQLException to allow callers to handle database errors appropriately.
 */
public interface VictimStatusLogDAO {

    /**
     * Records a status change event in the audit log.
     *
     * This method should be called immediately after any successful update to a victim's
     * AccountStatus field. The log entry is immutable and used for historical tracking.
     *
     * @param victimID   the ID of the victim whose status changed
     * @param oldStatus  the previous status (may be null for newly created accounts)
     * @param newStatus  the new status after the change
     * @param adminID    ID of the administrator who made the change (nullable if system-generated)
     * @return true if the log entry was successfully inserted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean logChange(int victimID, String oldStatus, String newStatus, Integer adminID) throws SQLException;

    /**
     * Retrieves the complete status change history for a specific victim.
     *
     * Results are typically ordered by ChangeDate DESC (newest first).
     *
     * @param victimID the victim's unique ID
     * @return ordered list of all status change events for this victim (never null)
     * @throws SQLException if a database access error occurs
     */
    List<VictimStatusLog> findByVictimID(int victimID) throws SQLException;

    /**
     * Retrieves all victim status change logs in the system.
     *
     * Used by administrators for system-wide auditing, compliance reporting,
     * and investigating patterns in account status workflows.
     *
     * @return list of all log entries, typically ordered by ChangeDate DESC
     * @throws SQLException if a database access error occurs
     */
    List<VictimStatusLog> findAll() throws SQLException;
}