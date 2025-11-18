package service;

import dao.VictimDAO;
import dao.VictimDAOImpl;
import model.Victim;
import util.SecurityUtils;
import util.ValidationUtils;

import java.sql.SQLException;

/**
 * Service class for Victim authentication operations
 * Handles victim registration and login
 */
public class VictimAuthenticationService {
    private final VictimDAO victimDAO;
    private Victim currentVictim;

    public VictimAuthenticationService() {
        this.victimDAO = new VictimDAOImpl();
        this.currentVictim = null;
    }

    /**
     * Register a new victim (public user signup)
     * @param name Victim's full name
     * @param email Victim's email
     * @param password Victim's password
     * @return true if registration successful
     */
    public boolean register(String name, String email, String password) {
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

        try {
            // Check if email already exists
            Victim existing = victimDAO.findByEmail(email);
            if (existing != null) {
                System.err.println("Email already registered");
                return false;
            }

            // Hash password with Argon2
            String passwordHash = SecurityUtils.hashPassword(password);
            if (passwordHash == null) {
                System.err.println("Failed to hash password. Please try again.");
                return false;
            }

            // Create new victim with password
            Victim newVictim = new Victim();
            newVictim.setName(name);
            newVictim.setContactEmail(email);
            newVictim.setPasswordHash(passwordHash);
            newVictim.setAccountStatus("Active");

            boolean created = victimDAO.create(newVictim);

            if (created) {
                System.out.println("Registration successful! VictimID: " + newVictim.getVictimID() + ", Email: " + email);
                return true;
            } else {
                System.err.println("Registration failed: victimDAO.create() returned false");
            }

        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration error: " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Authenticate victim with email and password
     * @param email Victim's email
     * @param password Victim's password
     * @return Victim object if authentication successful, null otherwise
     */
    public Victim login(String email, String password) {
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
            // Find victim by email
            Victim victim = victimDAO.findByEmail(email);

            if (victim == null) {
                System.err.println("No account found with this email");
                return null;
            }

            // Verify password using Argon2
            try {
                if (SecurityUtils.verifyPassword(password, victim.getPasswordHash())) {
                    currentVictim = victim;
                    System.out.println("Login successful! Welcome, " + victim.getName());
                    return victim;
                } else {
                    System.err.println("Invalid password");
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Password verification error: " + e.getMessage());
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logout current victim
     */
    public void logout() {
        if (currentVictim != null) {
            System.out.println("Logging out: " + currentVictim.getName());
            currentVictim = null;
        }
    }

    /**
     * Get currently logged in victim
     * @return Current Victim or null if not logged in
     */
    public Victim getCurrentVictim() {
        return currentVictim;
    }

    /**
     * Check if a victim is currently logged in
     * @return true if victim is logged in
     */
    public boolean isLoggedIn() {
        return currentVictim != null;
    }
}

