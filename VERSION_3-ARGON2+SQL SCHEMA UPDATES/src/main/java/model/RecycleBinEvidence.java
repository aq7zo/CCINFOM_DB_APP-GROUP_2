package model;

import java.time.LocalDateTime;

public class RecycleBinEvidence {
    private int binID;
    private int evidenceID;
    private Integer incidentID;
    private String evidenceType;
    private String filePath;
    private LocalDateTime submissionDate;
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

    public int getEvidenceID() {
        return evidenceID;
    }

    public void setEvidenceID(int evidenceID) {
        this.evidenceID = evidenceID;
    }

    public Integer getIncidentID() {
        return incidentID;
    }

    public void setIncidentID(Integer incidentID) {
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

