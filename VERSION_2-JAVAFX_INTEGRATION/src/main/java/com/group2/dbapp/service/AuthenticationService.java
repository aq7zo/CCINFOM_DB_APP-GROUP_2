package main.java.com.group2.dbapp.service;

import main.java.com.group2.dbapp.dao.AdministratorDAO;
import main.java.com.group2.dbapp.dao.AdministratorDAOImpl;
import main.java.com.group2.dbapp.model.Administrator;
import main.java.com.group2.dbapp.util.SecurityUtils;
import main.java.com.group2.dbapp.util.ValidationUtils;

import java.sql.SQLException;

/**
 * Service class for authentication operations
 * Handles login and authentication logic
 */
public class AuthenticationService {
    private final AdministratorDAO administratorDAO;
    private Administrator currentUser;
    
    public AuthenticationService() {
        this.administratorDAO = new AdministratorDAOImpl();
        this.currentUser = null;
    }
    
    /**
     * Authenticate user with email and password
     * @param email User's email
     * @param password User's password
     * @return Administrator object if authentication successful, null otherwise
     */
    public Administrator login(String email, String password) {
        // Validate input
        if (!ValidationUtils.isValidEmail(email)) {
            System.err.println("Invalid email format");
            return null;
        }
        
        if (!ValidationUtils.isNotEmpty(password)) {
            System.err.println("Password cannot be empty");
            return null;
        }
        
        try {
            // Find administrator by email
            Administrator admin = administratorDAO.findByEmail(email);
            
            if (admin == null) {
                System.err.println("Administrator not found");
                return null;
            }
            
            // Verify password
            if (SecurityUtils.verifyPassword(password, admin.getPasswordHash())) {
                currentUser = admin;
                System.out.println("Login successful for: " + admin.getName());
                return admin;
            } else {
                System.err.println("Invalid password");
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("Logging out: " + currentUser.getName());
            currentUser = null;
        }
    }
    
    /**
     * Get currently logged in user
     * @return Current Administrator or null if not logged in
     */
    public Administrator getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if a user is currently logged in
     * @return true if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Register a new administrator
     * @param name Administrator's name
     * @param role Administrator's role
     * @param email Administrator's email
     * @param password Administrator's password
     * @return true if registration successful
     */
    public boolean register(String name, String role, String email, String password) {
        // Validate inputs
        if (!ValidationUtils.isNotEmpty(name)) {
            System.err.println("Name cannot be empty");
            return false;
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            System.err.println("Invalid email format");
            return false;
        }
        
        if (!ValidationUtils.isValidPassword(password)) {
            System.err.println("Password must be at least 6 characters");
            return false;
        }
        
        if (!ValidationUtils.isValidEnumValue(role, new String[]{"System Admin", "Cybersecurity Staff"})) {
            System.err.println("Invalid role. Must be: System Admin or Cybersecurity Staff");
            return false;
        }
        
        try {
            // Check if email already exists
            Administrator existing = administratorDAO.findByEmail(email);
            if (existing != null) {
                System.err.println("Email already exists");
                return false;
            }
            
            // Hash password
            String passwordHash = SecurityUtils.hashPassword(password);
            
            // Create new administrator
            Administrator newAdmin = new Administrator(name, role, email, passwordHash);
            boolean created = administratorDAO.create(newAdmin);
            
            if (created) {
                System.out.println("Administrator registered successfully");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Change password for current user
     * @param oldPassword Current password
     * @param newPassword New password
     * @return true if password change successful
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!isLoggedIn()) {
            System.err.println("No user logged in");
            return false;
        }
        
        if (!ValidationUtils.isValidPassword(newPassword)) {
            System.err.println("New password must be at least 6 characters");
            return false;
        }
        
        try {
            // Verify old password
            if (!SecurityUtils.verifyPassword(oldPassword, currentUser.getPasswordHash())) {
                System.err.println("Incorrect current password");
                return false;
            }
            
            // Hash new password
            String newPasswordHash = SecurityUtils.hashPassword(newPassword);
            currentUser.setPasswordHash(newPasswordHash);
            
            // Update in database
            boolean updated = administratorDAO.update(currentUser);
            
            if (updated) {
                System.out.println("Password changed successfully");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during password change: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Send a password reset link or temporary password to the given email.
     * Currently simulates reset logic (no real email sending).
     * @param email Administrator's email
     * @return true if the reset process succeeded, false otherwise
     */
    public boolean sendPasswordReset(String email) {
        if (!ValidationUtils.isValidEmail(email)) {
            System.err.println("Invalid email format");
            return false;
        }

        try {
            Administrator admin = administratorDAO.findByEmail(email);

            if (admin == null) {
                System.err.println("No administrator found with that email");
                return false;
            }

            // Generate a temporary random password
            String tempPassword = "Temp" + (int)(Math.random() * 10000);
            String tempHash = SecurityUtils.hashPassword(tempPassword);

            // Update admin record with the new password hash
            admin.setPasswordHash(tempHash);
            boolean updated = administratorDAO.update(admin);

            if (updated) {
                // Simulate sending an email by printing to console
                System.out.println("Password reset successful for " + email);
                System.out.println("Temporary password: " + tempPassword);
                return true;
            } else {
                System.err.println("Failed to update password for reset");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database error during password reset: " + e.getMessage());
            return false;
        }
    }
}

