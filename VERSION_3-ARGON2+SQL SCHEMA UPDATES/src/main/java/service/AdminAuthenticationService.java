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
        Administrator admin = adminDAO.findByEmail(email);

        if (admin != null) {
            try {
                // Verify password using Argon2
                if (SecurityUtils.verifyPassword(password, admin.getPasswordHash())) {
                    return admin;
                }
            } catch (Exception e) {
                System.err.println("Password verification error: " + e.getMessage());
                // Don't leak information about whether email exists
                return null;
            }
        }

        return null;
    }
}

