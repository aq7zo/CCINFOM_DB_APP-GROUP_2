package model;

import java.time.LocalDateTime;

/**
 * Model class representing an IncidentReport entity
 */
public class IncidentReport {
    private int incidentID;
    private int victimID;
    private int perpetratorID;
    private int attackTypeID;
    private Integer adminID;
    private LocalDateTime dateReported;
    private String description;
    private String status; // 'Pending', 'Validated'
    
    public IncidentReport() {}
    
    public IncidentReport(int victimID, int perpetratorID, int attackTypeID, 
                         Integer adminID, String description, String status) {
        this.victimID = victimID;
        this.perpetratorID = perpetratorID;
        this.attackTypeID = attackTypeID;
        this.adminID = adminID;
        this.description = description;
        this.status = status;
    }
    
    // Getters and Setters
    public int getIncidentID() {
        return incidentID;
    }
    
    public void setIncidentID(int incidentID) {
        this.incidentID = incidentID;
    }
    
    public int getVictimID() {
        return victimID;
    }
    
    public void setVictimID(int victimID) {
        this.victimID = victimID;
    }
    
    public int getPerpetratorID() {
        return perpetratorID;
    }
    
    public void setPerpetratorID(int perpetratorID) {
        this.perpetratorID = perpetratorID;
    }
    
    public int getAttackTypeID() {
        return attackTypeID;
    }
    
    public void setAttackTypeID(int attackTypeID) {
        this.attackTypeID = attackTypeID;
    }
    
    public Integer getAdminID() {
        return adminID;
    }
    
    public void setAdminID(Integer adminID) {
        this.adminID = adminID;
    }
    
    public LocalDateTime getDateReported() {
        return dateReported;
    }
    
    public void setDateReported(LocalDateTime dateReported) {
        this.dateReported = dateReported;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return String.format("IncidentID: %d, VictimID: %d, PerpetratorID: %d, AttackTypeID: %d, Status: %s", 
            incidentID, victimID, perpetratorID, attackTypeID, status);
    }
}

