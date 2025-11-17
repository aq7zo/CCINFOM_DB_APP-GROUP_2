package service;

import dao.AdministratorDAO;
import dao.AdministratorDAOImpl;
import model.Administrator;
import util.ValidationUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Administrator management operations
 * Handles business logic for Administrator CRUD operations
 */
public class AdministratorService {
    private final AdministratorDAO administratorDAO;
    
    public AdministratorService() {
        this.administratorDAO = new AdministratorDAOImpl();
    }
    
    /**
     * Get administrator by ID
     * @param adminID Administrator's ID
     * @return Administrator object or null if not found
     */
    public Administrator getAdministratorById(int adminID) {
        if (!ValidationUtils.isPositive(adminID)) {
            System.err.println("Invalid administrator ID");
            return null;
        }
        
        try {
            return administratorDAO.findById(adminID);
        } catch (SQLException e) {
            System.err.println("Error retrieving administrator: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get administrator by email
     * @param email Administrator's email
     * @return Administrator object or null if not found
     */
    public Administrator getAdministratorByEmail(String email) {
        if (!ValidationUtils.isValidEmail(email)) {
            System.err.println("Invalid email format");
            return null;
        }
        
        try {
            return administratorDAO.findByEmail(email);
        } catch (SQLException e) {
            System.err.println("Error retrieving administrator: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all administrators
     * @return List of all administrators
     */
    public List<Administrator> getAllAdministrators() {
        try {
            return administratorDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Error retrieving administrators: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Update administrator information
     * @param admin Administrator object with updated data
     * @return true if update successful
     */
    public boolean updateAdministrator(Administrator admin) {
        if (admin == null) {
            System.err.println("Administrator object cannot be null");
            return false;
        }
        
        if (!ValidationUtils.isPositive(admin.getAdminID())) {
            System.err.println("Invalid administrator ID");
            return false;
        }
        
        if (!ValidationUtils.isNotEmpty(admin.getName())) {
            System.err.println("Name cannot be empty");
            return false;
        }
        
        if (!ValidationUtils.isValidEmail(admin.getContactEmail())) {
            System.err.println("Invalid email format");
            return false;
        }
        
        try {
            boolean updated = administratorDAO.update(admin);
            if (updated) {
                System.out.println("Administrator updated successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating administrator: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Delete administrator
     * @param adminID Administrator's ID to delete
     * @return true if deletion successful
     */
    public boolean deleteAdministrator(int adminID) {
        if (!ValidationUtils.isPositive(adminID)) {
            System.err.println("Invalid administrator ID");
            return false;
        }
        
        try {
            boolean deleted = administratorDAO.delete(adminID);
            if (deleted) {
                System.out.println("Administrator deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting administrator: " + e.getMessage());
        }
        
        return false;
    }
}

