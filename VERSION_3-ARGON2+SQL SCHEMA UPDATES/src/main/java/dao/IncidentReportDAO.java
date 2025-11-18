package dao;

import model.IncidentReport;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for managing IncidentReport entities.
 * Defines operations for creating, retrieving, updating, and analyzing incident reports
 * in the system, primarily used in a victim-perpetrator reporting and moderation workflow.
 */
public interface IncidentReportDAO {

    /**
     * Creates a new incident report in the database.
     * Typically called when a victim submits a report against a perpetrator.
     *
     * @param report the IncidentReport object containing all report details
     * @return true if the report was successfully inserted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean create(IncidentReport report) throws SQLException;

    /**
     * Retrieves an incident report by its unique identifier.
     *
     * @param incidentID the unique ID of the incident report
     * @return the IncidentReport object, or null if not found
     * @throws SQLException if a database access error occurs
     */
    IncidentReport findById(int incidentID) throws SQLException;

    /**
     * Finds all incident reports filed by a specific victim.
     * Useful for viewing a victim's reporting history.
     *
     * @param victimID the ID of the victim who submitted the reports
     * @return list of IncidentReport objects (may be empty)
     * @throws SQLException if a database access error occurs
     */
    List<IncidentReport> findByVictimID(int victimID) throws SQLException;

    /**
     * Finds all incident reports filed against a specific perpetrator.
     * Critical for moderation and detecting repeat offenders.
     *
     * @param perpetratorID the ID of the alleged perpetrator
     * @return list of IncidentReport objects targeting this perpetrator
     * @throws SQLException if a database access error occurs
     */
    List<IncidentReport> findByPerpetratorID(int perpetratorID) throws SQLException;

    /**
     * Retrieves all incident reports that are awaiting administrative review.
     * Usually reports with status 'Pending'.
     *
     * @return list of pending IncidentReport objects, typically ordered by submission time
     * @throws SQLException if a database access error occurs
     */
    List<IncidentReport> findPending() throws SQLException;
    List<IncidentReport> findAll() throws SQLException;
    boolean updateStatus(int incidentID, String status, Integer adminID) throws SQLException;

    /**
     * Permanently deletes an incident report.
     * Use with caution — consider soft deletion in production.
     *
     * @param incidentID the ID of the report to delete
     * @return true if deletion was successful
     * @throws SQLException if a database access error occurs
     */
    boolean delete(int incidentID) throws SQLException;

    /**
     * Counts how many incident reports a perpetrator has received in the last 7 days.
     * Includes duplicate reports from the same victim.
     *
     * @param perpetratorID the ID of the perpetrator
     * @return total number of reports in the last 7 days
     * @throws SQLException if a database access error occurs
     */
    int countVictimsLast7Days(int perpetratorID) throws SQLException;

    /**
     * Counts how many UNIQUE victims have reported a perpetrator in the last 7 days.
     * Helps identify widespread vs. targeted harassment patterns.
     *
     * @param perpetratorID the ID of the perpetrator
     * @return number of distinct victims who reported this perpetrator in the past week
     * @throws SQLException if a database access error occurs
     */
    int countUniqueVictimsLast7Days(int perpetratorID) throws SQLException;

    /**
     * Counts how many incident reports a specific victim has filed in the last 30 days.
     * Useful for detecting potential abuse of the reporting system.
     *
     * @param victimID the ID	of the reporting user
     * @return total number of reports submitted by this victim in the past month
     * @throws SQLException if a database access error occurs
     */
    int countIncidentsLastMonth(int victimID) throws SQLException;
}