package model;

import java.time.LocalDateTime;

/**
 * Model class representing a VictimStatusLog entity
 */
public class VictimStatusLog {
    private int logID;
    private int victimID;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changeDate;
    private Integer adminID;
    
    public VictimStatusLog() {}
    
    public VictimStatusLog(int victimID, String oldStatus, String newStatus, Integer adminID) {
        this.victimID = victimID;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.adminID = adminID;
    }
    
    // Getters and Setters
    public int getLogID() {
        return logID;
    }
    
    public void setLogID(int logID) {
        this.logID = logID;
    }
    
    public int getVictimID() {
        return victimID;
    }
    
    public void setVictimID(int victimID) {
        this.victimID = victimID;
    }
    
    public String getOldStatus() {
        return oldStatus;
    }
    
    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }
    
    public String getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
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
        return String.format("LogID: %d, VictimID: %d, %s -> %s, Date: %s", 
            logID, victimID, oldStatus, newStatus, changeDate);
    }
}

