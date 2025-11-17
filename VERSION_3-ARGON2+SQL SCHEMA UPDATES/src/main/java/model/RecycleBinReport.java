package model;

import java.time.LocalDateTime;

public class RecycleBinReport {
    private int binID;
    private int incidentID;
    private Integer victimID;
    private Integer perpetratorID;
    private Integer attackTypeID;
    private LocalDateTime dateReported;
    private String description;
    private String originalStatus;
    private Integer adminAssignedID;
    private int rejectedByAdminID;
    private String archiveReason;
    private LocalDateTime archivedAt;

    public int getBinID() {
        return binID;
    }

    public void setBinID(int binID) {
        this.binID = binID;
    }

    public int getIncidentID() {
        return incidentID;
    }

    public void setIncidentID(int incidentID) {
        this.incidentID = incidentID;
    }

    public Integer getVictimID() {
        return victimID;
    }

    public void setVictimID(Integer victimID) {
        this.victimID = victimID;
    }

    public Integer getPerpetratorID() {
        return perpetratorID;
    }

    public void setPerpetratorID(Integer perpetratorID) {
        this.perpetratorID = perpetratorID;
    }

    public Integer getAttackTypeID() {
        return attackTypeID;
    }

    public void setAttackTypeID(Integer attackTypeID) {
        this.attackTypeID = attackTypeID;
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

    public String getOriginalStatus() {
        return originalStatus;
    }

    public void setOriginalStatus(String originalStatus) {
        this.originalStatus = originalStatus;
    }

    public Integer getAdminAssignedID() {
        return adminAssignedID;
    }

    public void setAdminAssignedID(Integer adminAssignedID) {
        this.adminAssignedID = adminAssignedID;
    }

    public int getRejectedByAdminID() {
        return rejectedByAdminID;
    }

    public void setRejectedByAdminID(int rejectedByAdminID) {
        this.rejectedByAdminID = rejectedByAdminID;
    }

    public String getArchiveReason() {
        return archiveReason;
    }

    public void setArchiveReason(String archiveReason) {
        this.archiveReason = archiveReason;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }
}

