package app.services;

import app.dao.EvidenceDAO;
import app.models.Evidence;
import java.util.List;

/**
 * Service for Evidence business logic
 */
public class EvidenceService {
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    
    public boolean uploadEvidence(int incidentID, String evidenceType, String filePath, Integer adminID) {
        Evidence evidence = new Evidence(incidentID, evidenceType, filePath, "Pending", adminID);
        int id = evidenceDAO.insert(evidence);
        return id > 0;
    }
    
    public List<Evidence> getAllEvidence() {
        return evidenceDAO.findAll();
    }
    
    public List<Evidence> getEvidenceByIncident(int incidentID) {
        return evidenceDAO.findByIncidentId(incidentID);
    }
    
    public boolean updateEvidenceStatus(int evidenceID, String status, Integer adminID) {
        return evidenceDAO.updateStatus(evidenceID, status, adminID);
    }
}

