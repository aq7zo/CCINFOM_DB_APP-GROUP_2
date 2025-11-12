package dao;

import model.ThreatLevelLog;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO Interface for Threat Level Change Logging
 */
public interface ThreatLevelLogDAO {
    /**
     * Log a threat level change
     */
    boolean logChange(int perpetratorID, String oldLevel, String newLevel, int adminID) throws SQLException;

    /**
     * Get all logs for a perpetrator
     */
    List<ThreatLevelLog> findByPerpetratorID(int perpetratorID) throws SQLException;

    /**
     * Get all logs
     */
    List<ThreatLevelLog> findAll() throws SQLException;
}