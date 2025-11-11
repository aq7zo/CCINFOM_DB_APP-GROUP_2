package app.services;

import app.dao.VictimDAO;
import app.models.Victim;
import app.utils.ValidationUtils;
import java.sql.*;
import java.util.List;

/**
 * Service for Victim business logic
 */
public class VictimService {
    private final VictimDAO victimDAO = new VictimDAO();
    
    public boolean createVictim(String name, String contactEmail) {
        if (!ValidationUtils.isNotEmpty(name) || !ValidationUtils.isValidEmail(contactEmail)) {
            return false;
        }
        
        Victim victim = new Victim(name, contactEmail, "Active");
        int id = victimDAO.insert(victim);
        return id > 0;
    }
    
    public List<Victim> getAllVictims() {
        return victimDAO.findAll();
    }
    
    public Victim getVictimById(int victimID) {
        return victimDAO.findById(victimID);
    }

    public Victim getVictimByEmail(String email) {
        if (!ValidationUtils.isValidEmail(email)) {
            return null;
        }
        return victimDAO.findByEmail(email);
    }
    
    public boolean updateVictimStatus(int victimID, String newStatus, Integer adminID) {
        if (!ValidationUtils.isValidEnumValue(newStatus, new String[]{"Active", "Flagged", "Suspended"})) {
            return false;
        }
        
        String oldStatus = victimDAO.getStatus(victimID);
        if (oldStatus == null) {
            return false;
        }
        
        boolean updated = victimDAO.updateStatus(victimID, newStatus);
        if (updated) {
            logVictimStatusChange(victimID, oldStatus, newStatus, adminID);
        }
        return updated;
    }
    
    public void checkAndAutoFlagVictims() {
        List<Victim> victimsToFlag = victimDAO.findVictimsToAutoFlag();
        for (Victim victim : victimsToFlag) {
            updateVictimStatus(victim.getVictimID(), "Flagged", null);
        }
    }
    
    public boolean deleteVictim(int victimID) {
        return victimDAO.delete(victimID);
    }
    
    private void logVictimStatusChange(int victimID, String oldStatus, String newStatus, Integer adminID) {
        try (var conn = app.config.DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO VictimStatusLog (VictimID, OldStatus, NewStatus, AdminID) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, victimID);
                stmt.setString(2, oldStatus);
                stmt.setString(3, newStatus);
                if (adminID != null) {
                    stmt.setInt(4, adminID);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error logging status change: " + e.getMessage());
        }
    }
}

