package model;

import java.time.LocalDateTime;

/**
 * Model class representing a ThreatLevelLog entity
 */
public class ThreatLevelLog {
    private int logID;
    private int perpetratorID;
    private String oldThreatLevel;
    private String newThreatLevel;
    private LocalDateTime changeDate;
    private Integer adminID;
    
    public ThreatLevelLog() {}
    
    public ThreatLevelLog(int perpetratorID, String oldThreatLevel, String newThreatLevel, Integer adminID) {
        this.perpetratorID = perpetratorID;
        this.oldThreatLevel = oldThreatLevel;
        this.newThreatLevel = newThreatLevel;
        this.adminID = adminID;
    }
    
    // Getters and Setters
    public int getLogID() {
        return logID;
    }
    
    public void setLogID(int logID) {
        this.logID = logID;
    }
    
    public int getPerpetratorID() {
        return perpetratorID;
    }
    
    public void setPerpetratorID(int perpetratorID) {
        this.perpetratorID = perpetratorID;
    }
    
    public String getOldThreatLevel() {
        return oldThreatLevel;
    }
    
    public void setOldThreatLevel(String oldThreatLevel) {
        this.oldThreatLevel = oldThreatLevel;
    }
    
    public String getNewThreatLevel() {
        return newThreatLevel;
    }
    
    public void setNewThreatLevel(String newThreatLevel) {
        this.newThreatLevel = newThreatLevel;
    }
    
    public LocalDateTime getChangeDate() {
        return changeDate;
    }
    
    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }
    
    public Integer getAdminID() {
        return adminID;
    }
    
    public void setAdminID(Integer adminID) {
        this.adminID = adminID;
    }
    
    @Override
    public String toString() {
        return String.format("LogID: %d, PerpetratorID: %d, %s -> %s, Date: %s", 
            logID, perpetratorID, oldThreatLevel, newThreatLevel, changeDate);
    }
}

