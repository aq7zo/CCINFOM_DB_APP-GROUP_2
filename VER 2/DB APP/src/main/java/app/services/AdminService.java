package app.services;

import app.dao.AdministratorDAO;
import app.models.Administrator;
import java.util.List;

/**
 * Service for Administrator business logic
 */
public class AdminService {
    private final AdministratorDAO adminDAO = new AdministratorDAO();
    
    public boolean createAdministrator(String name, String role, String email) {
        Administrator admin = new Administrator(name, role, email);
        int id = adminDAO.insert(admin);
        return id > 0;
    }
    
    public List<Administrator> getAllAdministrators() {
        return adminDAO.findAll();
    }
    
    public Administrator getAdministratorById(int adminID) {
        return adminDAO.findById(adminID);
    }
    
    public Administrator getAdministratorByEmail(String email) {
        return adminDAO.findByEmail(email);
    }
    
    public boolean deleteAdministrator(int adminID) {
        return adminDAO.delete(adminID);
    }
}

