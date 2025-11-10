package app.models;

import java.time.LocalDateTime;

/**
 * Model class representing a Victim entity
 */
public class Victim {
    private int victimID;
    private String name;
    private String contactEmail;
    private String accountStatus; // 'Active', 'Flagged', 'Suspended'
    private LocalDateTime dateCreated;
    
    public Victim() {}
    
    public Victim(String name, String contactEmail, String accountStatus) {
        this.name = name;
        this.contactEmail = contactEmail;
        this.accountStatus = accountStatus;
    }
    
    // Getters and Setters
    public int getVictimID() {
        return victimID;
    }
    
    public void setVictimID(int victimID) {
        this.victimID = victimID;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    @Override
    public String toString() {
        return String.format("VictimID: %d, Name: %s, Email: %s, Status: %s", 
            victimID, name, contactEmail, accountStatus);
    }
}

