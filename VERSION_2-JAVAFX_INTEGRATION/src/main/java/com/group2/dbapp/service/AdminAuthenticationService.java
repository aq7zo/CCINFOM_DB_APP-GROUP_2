package com.group2.dbapp.service;

import com.group2.dbapp.dao.AdministratorDAO;
import com.group2.dbapp.dao.AdministratorDAOImpl;
import com.group2.dbapp.model.Administrator;
import com.group2.dbapp.util.SecurityUtils;

import java.sql.SQLException;

public class AdminAuthenticationService {

    private final AdministratorDAO adminDAO = new AdministratorDAOImpl();

    public Administrator authenticate(String email, String password) throws Exception {
        Administrator admin = adminDAO.findByEmail(email);

        if (admin != null && SecurityUtils.verifyPassword(password, admin.getPasswordHash())) {
            return admin;
        }

        return null;
    }

    /**
     * Sends a temporary password to the admin (for password reset)
     * @param email Admin email
     * @return true if email exists, false otherwise
     */
    public boolean sendPasswordReset(String email) {
        try {
            Administrator admin = adminDAO.findByEmail(email);
            if (admin != null) {
                String tempPassword = "adminTemp123"; // In real app: generate random
                System.out.println("TEMP PASSWORD for admin " + email + ": " + tempPassword);
                // TODO: In future, send via email
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}