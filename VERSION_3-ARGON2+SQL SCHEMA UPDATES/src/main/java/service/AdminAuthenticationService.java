package service;

import dao.AdministratorDAO;
import dao.AdministratorDAOImpl;
import model.Administrator;
import util.SecurityUtils;

import java.sql.SQLException;

/**
 * Service class for Administrator authentication operations
 * Handles administrator login with Argon2 password verification
 */
public class AdminAuthenticationService {

    private final AdministratorDAO adminDAO = new AdministratorDAOImpl();

    /**
     * Authenticate administrator with email and password
     * @param email Administrator's email
     * @param password Administrator's password
     * @return Administrator object if authentication successful, null otherwise
     * @throws Exception if database error occurs
     */
    public Administrator authenticate(String email, String password) throws Exception {
        // Trim email to handle whitespace issues
        email = email != null ? email.trim() : "";
        
        if (email.isEmpty()) {
            System.err.println("Admin authentication: Email is empty");
            return null;
        }
        
        Administrator admin = adminDAO.findByEmail(email);

        if (admin == null) {
            System.err.println("Admin authentication: No account found with email: " + email);
            System.err.println("Hint: Make sure PhishNet-inserts.sql has been run to populate admin accounts");
            return null;
        }

        try {
            // Debug: Print hash info
            String hashFromDB = admin.getPasswordHash();
            System.out.println("DEBUG: Verifying password for: " + email);
            System.out.println("DEBUG: Hash length from DB: " + (hashFromDB != null ? hashFromDB.length() : "null"));
            System.out.println("DEBUG: Hash preview: " + (hashFromDB != null && hashFromDB.length() > 50 ? hashFromDB.substring(0, 50) + "..." : hashFromDB));
            
            // Verify password using Argon2
            if (SecurityUtils.verifyPassword(password, hashFromDB)) {
                System.out.println("Admin authentication successful for: " + admin.getName());
                return admin;
            } else {
                System.err.println("Admin authentication: Invalid password for email: " + email);
                System.err.println("DEBUG: Password provided length: " + (password != null ? password.length() : "null"));
                return null;
            }
        } catch (Exception e) {
            System.err.println("Password verification error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

