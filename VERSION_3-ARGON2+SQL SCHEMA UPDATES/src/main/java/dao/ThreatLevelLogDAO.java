package dao;

import model.ThreatLevelLog;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for handling threat level change logs.
 * Provides methods for inserting logs, recording changes, and retrieving logs.
 */
public interface ThreatLevelLogDAO {

    /**
     * Records a threat level change event for a perpetrator.
     *
     * @param perpetratorID ID of the perpetrator whose threat level changed
     * @param oldLevel the previous threat level
     * @param newLevel the updated threat level
     * @param adminID ID of the admin who made the change
     * @return true if logging was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean logChange(int perpetratorID, String oldLevel, String newLevel, int adminID) throws SQLException;

    /**
     * Inserts a threat level log entry into the database.
     * This method allows inserting a fully constructed ThreatLevelLog object.
     *
     * @param log the ThreatLevelLog model containing log details
     * @return true if insertion succeeds, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean insert(ThreatLevelLog log) throws SQLException; // Explicit insert method

    /**
     * Retrieves all threat level logs for a specific perpetrator.
     *
     * @param perpetratorID ID of the perpetrator
     * @return list of logs associated with the perpetrator
     * @throws SQLException if a database access error occurs
     */
    List<ThreatLevelLog> findByPerpetratorID(int perpetratorID) throws SQLException;

    /**
     * Retrieves all threat level logs stored in the system.
     *
     * @return list of all threat level logs
     * @throws SQLException if a database access error occurs
     */
    List<ThreatLevelLog> findAll() throws SQLException;
}
