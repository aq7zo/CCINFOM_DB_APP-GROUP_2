package model;

import java.time.LocalDateTime;

/**
 * Transaction Record: ThreatLevelLog
 */
public class ThreatLevelLog {
    private int logID;
    private int perpetratorID;
    private String oldThreatLevel;
    private String newThreatLevel;
    private LocalDateTime changeDate;
    private int adminID;

    // Default constructor
    public ThreatLevelLog() {}

    // Getters and Setters
    public int getLogID() { return logID; }
    public void setLogID(int logID) { this.logID = logID; }

    public int getPerpetratorID() { return perpetratorID; }
    public void setPerpetratorID(int perpetratorID) { this.perpetratorID = perpetratorID; }

    public String getOldThreatLevel() { return oldThreatLevel; }
    public void setOldThreatLevel(String oldThreatLevel) { this.oldThreatLevel = oldThreatLevel; }

    public String getNewThreatLevel() { return newThreatLevel; }
    public void setNewThreatLevel(String newThreatLevel) { this.newThreatLevel = newThreatLevel; }

    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }

    public int getAdminID() { return adminID; }
    public void setAdminID(int adminID) { this.adminID = adminID; }

    @Override
    public String toString() {
        return String.format("ThreatLog[%d] Perp:%d %s → %s by Admin:%d",
                logID, perpetratorID, oldThreatLevel, newThreatLevel, adminID);
    }
}