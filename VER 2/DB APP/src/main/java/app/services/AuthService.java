package app.services;

import app.config.SessionManager;
import app.dao.AdministratorDAO;
import app.dao.VictimDAO;
import app.models.Administrator;
import app.models.Victim;

/**
 * Service for authentication and login operations
 */
public class AuthService {
    private final VictimDAO victimDAO = new VictimDAO();
    private final AdministratorDAO adminDAO = new AdministratorDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    /**
     * Login as User (Victim) by email
     * For now, password is not required (can be added later)
     */
    public boolean loginAsUser(String email) {
        Victim victim = victimDAO.findByEmail(email);
        if (victim != null) {
            sessionManager.loginAsUser(victim);
            return true;
        }
        return false;
    }
    
    /**
     * Login as Admin by email
     * For now, password is not required (can be added later)
     */
    public boolean loginAsAdmin(String email) {
        Administrator admin = adminDAO.findByEmail(email);
        if (admin != null) {
            sessionManager.loginAsAdmin(admin);
            return true;
        }
        return false;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        sessionManager.logout();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }
    
    /**
     * Check if current user is Admin
     */
    public boolean isAdmin() {
        return sessionManager.isAdmin();
    }
    
    /**
     * Check if current user is User (Victim)
     */
    public boolean isUser() {
        return sessionManager.isUser();
    }
}

