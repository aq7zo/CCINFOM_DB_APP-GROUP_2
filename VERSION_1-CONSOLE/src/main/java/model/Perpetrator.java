package model;

import java.time.LocalDateTime;

/**
 * Model class representing a Perpetrator entity
 */
public class Perpetrator {
    private int perpetratorID;
    private String identifier;
    private String identifierType; // 'Phone Number', 'Email Address', 'Social Media Account', 'Website URL', 'IP Address'
    private String associatedName;
    private String threatLevel; // 'UnderReview', 'Suspected', 'Malicious', 'Cleared'
    private LocalDateTime lastIncidentDate;
    
    public Perpetrator() {}
    
    public Perpetrator(String identifier, String identifierType, String associatedName, String threatLevel) {
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.associatedName = associatedName;
        this.threatLevel = threatLevel;
    }
    
    // Getters and Setters
    public int getPerpetratorID() {
        return perpetratorID;
    }
    
    public void setPerpetratorID(int perpetratorID) {
        this.perpetratorID = perpetratorID;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getIdentifierType() {
        return identifierType;
    }
    
    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }
    
    public String getAssociatedName() {
        return associatedName;
    }
    
    public void setAssociatedName(String associatedName) {
        this.associatedName = associatedName;
    }
    
    public String getThreatLevel() {
        return threatLevel;
    }
    
    public void setThreatLevel(String threatLevel) {
        this.threatLevel = threatLevel;
    }
    
    public LocalDateTime getLastIncidentDate() {
        return lastIncidentDate;
    }
    
    public void setLastIncidentDate(LocalDateTime lastIncidentDate) {
        this.lastIncidentDate = lastIncidentDate;
    }
    
    @Override
    public String toString() {
        return String.format("PerpetratorID: %d, Identifier: %s (%s), Threat Level: %s", 
            perpetratorID, identifier, identifierType, threatLevel);
    }
}

