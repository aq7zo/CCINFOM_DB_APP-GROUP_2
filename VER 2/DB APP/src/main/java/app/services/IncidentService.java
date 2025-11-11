package app.services;

import app.dao.IncidentDAO;
import app.models.IncidentReport;
import java.util.List;

/**
 * Service for Incident business logic
 */
public class IncidentService {
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final PerpetratorService perpetratorService = new PerpetratorService();
    private final VictimService victimService = new VictimService();
    
    public int createIncident(int victimID, int perpetratorID, int attackTypeID,
                              Integer adminID, String description) {
        IncidentReport incident = new IncidentReport(victimID, perpetratorID, attackTypeID, 
                                                     adminID, description, "Pending");
        int id = incidentDAO.insert(incident);
        
        if (id > 0) {
            // Update perpetrator's last incident date
            perpetratorService.updateLastIncidentDate(perpetratorID);
            
            // Check for auto-escalation
            perpetratorService.checkAndAutoEscalate();
            
            // Check for auto-flagging victims
            victimService.checkAndAutoFlagVictims();
            
            return id;
        }
        return -1;
    }
    
    public List<IncidentReport> getAllIncidents() {
        return incidentDAO.findAll();
    }
    
    public IncidentReport getIncidentById(int incidentID) {
        return incidentDAO.findById(incidentID);
    }
    
    public List<IncidentReport> getIncidentsByVictim(int victimID) {
        return incidentDAO.findByVictimId(victimID);
    }
    
    public boolean validateIncident(int incidentID, int adminID) {
        return incidentDAO.updateStatus(incidentID, "Validated", adminID);
    }

    public boolean updateIncidentStatus(int incidentID, String status, Integer adminID) {
        return incidentDAO.updateStatus(incidentID, status, adminID);
    }
}

