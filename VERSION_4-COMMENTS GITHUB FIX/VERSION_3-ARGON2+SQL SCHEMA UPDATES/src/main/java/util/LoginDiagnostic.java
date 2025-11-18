package util;

import dao.AdministratorDAOImpl;
import model.Administrator;
import service.AdminAuthenticationService;

import java.sql.SQLException;

/**
 * Comprehensive diagnostic tool for troubleshooting admin login issues.
 *
 * This utility walks through every step of the login process in order:
 * 1. Database connectivity
 * 2. Admin account lookup by email
 * 3. Password hash verification using SecurityUtils
 * 4. Full authentication via AdminAuthenticationService
 *
 * If everything shows SUCCESS → login should work perfectly.
 * If any step fails → this tool tells you exactly what to fix.
 */
public class LoginDiagnostic {
    
    public static void main(String[] args) {
        System.out.println("=== Admin Login Diagnostic Tool ===\n");
        
        String testEmail = "admin@phishnet.com";
        String testPassword = "PhishNetAdmin124";
        
        // Step 1: Can we even talk to the database?
        System.out.println("1. Testing database connection...");
        if (DatabaseConnection.testConnection()) {
            System.out.println("   Database connection successful\n");
        } else {
            System.out.println("   Database connection FAILED\n");
            System.out.println("   Fix: Check MySQL is running and credentials in DatabaseConnection.java are correct");
            return;
        }
        
        // Step 2: Does the admin account actually exist?
        System.out.println("2. Testing admin lookup by email: " + testEmail);
        AdministratorDAOImpl adminDAO = new AdministratorDAOImpl();
        try {
            Administrator admin = adminDAO.findByEmail(testEmail);
            if (admin != null) {
                System.out.println("   Admin found!");
                System.out.println("   - Name: " + admin.getName());
                System.out.println("   - Role: " + admin.getRole());
                System.out.println("   - Email: " + admin.getContactEmail());
                System.out.println("   - Hash length: " + admin.getPasswordHash().length());
                System.out.println("   - Hash format: " + 
                    (admin.getPasswordHash().startsWith("$argon2id$") ? "Valid Argon2id" : "Invalid or legacy"));
                System.out.println("   - Hash preview: " + 
                    admin.getPasswordHash().substring(0, Math.min(50, admin.getPasswordHash().length())) + "...\n");
                
                // Step 3: Does the stored hash match the known password?
                System.out.println("3. Testing password verification...");
                System.out.println("   Password: " + testPassword);
                boolean verified = SecurityUtils.verifyPassword(testPassword, admin.getPasswordHash());
                if (verified) {
                    System.out.println("   Password verification SUCCESSFUL\n");
                } else {
                    System.out.println("   Password verification FAILED\n");
                    System.out.println("   Common causes:");
                    System.out.println("   • The stored hash was generated with different Argon2 parameters");
                    System.out.println("   • The hash is corrupted or truncated");
                    System.out.println("   • It's an old SHA-256 hash that needs migration");
                    System.out.println("   Fix: Run GenerateAndUpdateHash.java or TestDatabaseHash.java to generate a new valid hash");
                }
                
            } else {
                System.out.println("   Admin NOT FOUND in database\n");
                System.out.println("   Fix: Run the SQL insert script to create admin accounts:");
                System.out.println("   → PhishNet-inserts.sql");
                System.out.println("   Or manually insert the admin record into the Administrators table");
            }
        } catch (SQLException e) {
            System.out.println("   Database error during lookup: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Step 4: Full end-to-end test using the real authentication service
        System.out.println("4. Testing full authentication service...");
        AdminAuthenticationService authService = new AdminAuthenticationService();
        try {
            Administrator authenticatedAdmin = authService.authenticate(testEmail, testPassword);
            if (authenticatedAdmin != null) {
                System.out.println("   Full authentication SUCCESSFUL!");
                System.out.println("   - Logged in as: " + authenticatedAdmin.getName() + 
                                " (" + authenticatedAdmin.getRole() + ")");
            } else {
                System.out.println("   Full authentication FAILED");
                System.out.println("   → Check the steps above to see which part failed");
            }
        } catch (Exception e) {
            System.out.println("   Unexpected error in authentication service: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Diagnostic Complete ===");
        System.out.println("If all steps show SUCCESS → admin login should work perfectly in the app.");
        System.out.println("If any step failed → the message tells you exactly what to fix.");
    }
}