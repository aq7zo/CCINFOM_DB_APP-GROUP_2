package main.java.com.group2.dbapp.model;

import java.time.LocalDateTime;

/**
 * Model class representing an Administrator entity
 */
public class Administrator {
    private int adminID;
    private String name;
    private String role; // 'System Admin', 'Cybersecurity Staff'
    private String contactEmail;
    private String passwordHash; // Password stored as hash
    private LocalDateTime dateAssigned;
    
    public Administrator() {}
    
    public Administrator(String name, String role, String contactEmail, String passwordHash) {
        this.name = name;
        this.role = role;
        this.contactEmail = contactEmail;
        this.passwordHash = passwordHash;
    }
    
    // Getters and Setters
    public int getAdminID() {
        return adminID;
    }
    
    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public LocalDateTime getDateAssigned() {
        return dateAssigned;
    }
    
    public void setDateAssigned(LocalDateTime dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
    
    @Override
    public String toString() {
        return String.format("AdminID: %d, Name: %s, Role: %s, Email: %s", 
            adminID, name, role, contactEmail);
    }
}

