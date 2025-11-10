package app.services;

import app.dao.PerpetratorDAO;
import app.models.Perpetrator;
import app.utils.ValidationUtils;
import java.sql.*;
import java.util.List;

/**
 * Service for Perpetrator business logic
 */
public class PerpetratorService {
    private final PerpetratorDAO perpetratorDAO = new PerpetratorDAO();
    
    public int createOrGetPerpetrator(String identifier, String identifierType, String associatedName) {
        if (!ValidationUtils.isNotEmpty(identifier) || !ValidationUtils.isNotEmpty(identifierType)) {
            return -1;
        }
        
        // Check if exists
        Integer existingID = perpetratorDAO.findIdByIdentifier(identifier);
        if (existingID != null) {
            return existingID;
        }
        
        // Create new
        Perpetrator perpetrator = new Perpetrator(identifier, identifierType, associatedName, "UnderReview");
        return perpetratorDAO.insert(perpetrator);
    }
    
    public List<Perpetrator> getAllPerpetrators() {
        return perpetratorDAO.findAll();
    }
    
    public Perpetrator getPerpetratorById(int perpetratorID) {
        return perpetratorDAO.findById(perpetratorID);
    }
    
    public boolean updateThreatLevel(int perpetratorID, String newThreatLevel, Integer adminID) {
        if (!ValidationUtils.isValidEnumValue(newThreatLevel, 
                new String[]{"UnderReview", "Suspected", "Malicious", "Cleared"})) {
            return false;
        }
        
        String oldThreatLevel = perpetratorDAO.getThreatLevel(perpetratorID);
        if (oldThreatLevel == null) {
            return false;
        }
        
        boolean updated = perpetratorDAO.updateThreatLevel(perpetratorID, newThreatLevel);
        if (updated) {
            logThreatLevelChange(perpetratorID, oldThreatLevel, newThreatLevel, adminID);
        }
        return updated;
    }
    
    public void updateLastIncidentDate(int perpetratorID) {
        perpetratorDAO.updateLastIncidentDate(perpetratorID);
    }
    
    public void checkAndAutoEscalate() {
        List<Perpetrator> perpetratorsToEscalate = perpetratorDAO.findPerpetratorsToAutoEscalate();
        for (Perpetrator perpetrator : perpetratorsToEscalate) {
            updateThreatLevel(perpetrator.getPerpetratorID(), "Malicious", null);
        }
    }
    
    private void logThreatLevelChange(int perpetratorID, String oldLevel, String newLevel, Integer adminID) {
        try (var conn = app.config.DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO ThreatLevelLog (PerpetratorID, OldThreatLevel, NewThreatLevel, AdminID) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, perpetratorID);
                stmt.setString(2, oldLevel);
                stmt.setString(3, newLevel);
                if (adminID != null) {
                    stmt.setInt(4, adminID);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error logging threat level change: " + e.getMessage());
        }
    }
}

