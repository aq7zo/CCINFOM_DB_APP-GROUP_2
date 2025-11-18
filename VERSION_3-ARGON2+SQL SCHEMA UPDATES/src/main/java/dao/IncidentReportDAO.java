package dao;

import model.IncidentReport;
import java.sql.SQLException;
import java.util.List;

public interface IncidentReportDAO {
    boolean create(IncidentReport report) throws SQLException;
    IncidentReport findById(int incidentID) throws SQLException;
    List<IncidentReport> findByVictimID(int victimID) throws SQLException;
    List<IncidentReport> findByPerpetratorID(int perpetratorID) throws SQLException;
    List<IncidentReport> findPending() throws SQLException;

    /**
     * Updates the administrative status of an incident report (e.g., 'Verified', 'Rejected', 'Under Review').
     * Called by admins during moderation.
     *
     * @param incidentID the ID of the report to update
     * @param status     the new status (e.g., "Verified", "Rejected")
     * @param adminID    the ID of the admin performing the action, or null if not yet assigned
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateStatus(int incidentID, String status, Integer adminID) throws SQLException;
    boolean delete(int incidentID) throws SQLException;
    int countVictimsLast7Days(int perpetratorID) throws SQLException;
    int countUniqueVictimsLast7Days(int perpetratorID) throws SQLException; // ADD THIS
    int countIncidentsLastMonth(int victimID) throws SQLException;
}