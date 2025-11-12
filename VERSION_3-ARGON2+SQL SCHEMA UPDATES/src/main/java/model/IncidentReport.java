package model;

import java.time.LocalDateTime;

/**
 * Transaction Record: IncidentReports
 * Links Victim → Perpetrator → AttackType
 */
public class IncidentReport {
    private int incidentID;
    private int victimID;
    private int perpetratorID;
    private int attackTypeID;
    private Integer adminID;           // Nullable
    private LocalDateTime dateReported;
    private String description;
    private String status;             // Pending, Validated

    // Default constructor
    public IncidentReport() {}

    // Getters and Setters
    public int getIncidentID() { return incidentID; }
    public void setIncidentID(int incidentID) { this.incidentID = incidentID; }

    public int getVictimID() { return victimID; }
    public void setVictimID(int victimID) { this.victimID = victimID; }

    public int getPerpetratorID() { return perpetratorID; }
    public void setPerpetratorID(int perpetratorID) { this.perpetratorID = perpetratorID; }

    public int getAttackTypeID() { return attackTypeID; }
    public void setAttackTypeID(int attackTypeID) { this.attackTypeID = attackTypeID; }

    public Integer getAdminID() { return adminID; }
    public void setAdminID(Integer adminID) { this.adminID = adminID; }

    public LocalDateTime getDateReported() { return dateReported; }
    public void setDateReported(LocalDateTime dateReported) { this.dateReported = dateReported; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Incident[%d] Victim:%d → Perp:%d | %s",
                incidentID, victimID, perpetratorID, status);
    }
}
