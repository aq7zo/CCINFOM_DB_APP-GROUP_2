package app.models;

import java.time.LocalDateTime;

/**
 * Model class representing an EvidenceUpload entity
 */
public class Evidence {
    private int evidenceID;
    private int incidentID;
    private String evidenceType; // 'Screenshot', 'Email', 'File', 'Chat Log'
    private String filePath;
    private LocalDateTime submissionDate;
    private String verifiedStatus; // 'Pending', 'Verified', 'Rejected'
    private Integer adminID;
    
    public Evidence() {}
    
    public Evidence(int incidentID, String evidenceType, String filePath, String verifiedStatus, Integer adminID) {
        this.incidentID = incidentID;
        this.evidenceType = evidenceType;
        this.filePath = filePath;
        this.verifiedStatus = verifiedStatus;
        this.adminID = adminID;
    }
    
    // Getters and Setters
    public int getEvidenceID() {
        return evidenceID;
    }
    
    public void setEvidenceID(int evidenceID) {
        this.evidenceID = evidenceID;
    }
    
    public int getIncidentID() {
        return incidentID;
    }
    
    public void setIncidentID(int incidentID) {
        this.incidentID = incidentID;
    }
    
    public String getEvidenceType() {
        return evidenceType;
    }
    
    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }
    
    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
    
    public String getVerifiedStatus() {
        return verifiedStatus;
    }
    
    public void setVerifiedStatus(String verifiedStatus) {
        this.verifiedStatus = verifiedStatus;
    }
    
    public Integer getAdminID() {
        return adminID;
    }
    
    public void setAdminID(Integer adminID) {
        this.adminID = adminID;
    }
    
    @Override
    public String toString() {
        return String.format("EvidenceID: %d, IncidentID: %d, Type: %s, Status: %s", 
            evidenceID, incidentID, evidenceType, verifiedStatus);
    }
}

