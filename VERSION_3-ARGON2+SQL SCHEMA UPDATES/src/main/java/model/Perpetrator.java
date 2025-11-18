package model;

import java.time.LocalDateTime;

/**
 * Model for Perpetrators Record Management
 * Fields: PerpetratorID, Identifier, IdentifierType, AssociatedName,
 *         ThreatLevel, LastIncidentDate
 */
public class Perpetrator {
    private int perpetratorID;
    private String identifier;           // e.g., 09171234567, scam@fake.com
    private String identifierType;       // Phone Number, Email Address, etc.
    private String associatedName;       // Optional
    private String threatLevel;          // UnderReview, Suspected, Malicious, Cleared
    private LocalDateTime lastIncidentDate;

    // Default constructor
    public Perpetrator() {}

    // Constructor with required fields
    public Perpetrator(String identifier, String identifierType, String associatedName,
                       String threatLevel, LocalDateTime lastIncidentDate) {
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.associatedName = associatedName;
        this.threatLevel = threatLevel;
        this.lastIncidentDate = lastIncidentDate;
    }

    // Getters and Setters
    public int getPerpetratorID() { return perpetratorID; }
    public void setPerpetratorID(int perpetratorID) { this.perpetratorID = perpetratorID; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getIdentifierType() { return identifierType; }
    public void setIdentifierType(String identifierType) { this.identifierType = identifierType; }

    public String getAssociatedName() { return associatedName; }
    public void setAssociatedName(String associatedName) { this.associatedName = associatedName; }

    public String getThreatLevel() { return threatLevel; }
    public void setThreatLevel(String threatLevel) { this.threatLevel = threatLevel; }

    public LocalDateTime getLastIncidentDate() { return lastIncidentDate; }
    public void setLastIncidentDate(LocalDateTime lastIncidentDate) { this.lastIncidentDate = lastIncidentDate; }

    @Override
    public String toString() {
        return String.format("Perpetrator[%d] %s (%s) - %s",
                perpetratorID, identifier, identifierType, threatLevel);
    }
}
