package app.config;

import app.models.Victim;
import app.models.Administrator;

/**
 * SessionManager handles role-based authentication and session management
 * Supports both User (Victim) and Admin roles
 */
public class SessionManager {
    private static SessionManager instance;
    private String userRole; // "User" or "Admin"
    private int userId; // VictimID or AdminID
    private String name;
    private String email;
    private Object userObject; // Victim or Administrator object
    
    private SessionManager() {}
    
    /**
     * Get singleton instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Login as User (Victim)
     */
    public void loginAsUser(Victim victim) {
        this.userRole = "User";
        this.userId = victim.getVictimID();
        this.name = victim.getName();
        this.email = victim.getContactEmail();
        this.userObject = victim;
    }
    
    /**
     * Login as Admin
     */
    public void loginAsAdmin(Administrator admin) {
        this.userRole = "Admin";
        this.userId = admin.getAdminID();
        this.name = admin.getName();
        this.email = admin.getContactEmail();
        this.userObject = admin;
    }
    
    /**
     * Logout and clear session
     */
    public void logout() {
        this.userRole = null;
        this.userId = 0;
        this.name = null;
        this.email = null;
        this.userObject = null;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return userRole != null;
    }
    
    /**
     * Check if current user is Admin
     */
    public boolean isAdmin() {
        return "Admin".equals(userRole);
    }
    
    /**
     * Check if current user is User (Victim)
     */
    public boolean isUser() {
        return "User".equals(userRole);
    }
    
    // Getters
    public String getUserRole() {
        return userRole;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public Victim getVictim() {
        return isUser() ? (Victim) userObject : null;
    }
    
    public Administrator getAdmin() {
        return isAdmin() ? (Administrator) userObject : null;
    }
    
    /**
     * Get current user object (Victim or Administrator)
     */
    public Object getCurrentUser() {
        return userObject;
    }
}

