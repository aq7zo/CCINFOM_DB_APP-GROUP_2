package model;

import java.time.LocalDateTime;

/**
 * Transaction Record: VictimStatusLog
 */
public class VictimStatusLog {
    private int logID;
    private int victimID;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changeDate;
    private int adminID;

    // Default constructor
    public VictimStatusLog() {}

    // Getters and Setters
    public int getLogID() { return logID; }
    public void setLogID(int logID) { this.logID = logID; }

    public int getVictimID() { return victimID; }
    public void setVictimID(int victimID) { this.victimID = victimID; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }

    public int getAdminID() { return adminID; }
    public void setAdminID(int adminID) { this.adminID = adminID; }

    @Override
    public String toString() {
        return String.format("StatusLog[%d] Victim:%d %s → %s",
                logID, victimID, oldStatus, newStatus);
    }
}
