package util;

import dao.AdministratorDAOImpl;
import model.Administrator;

import java.sql.SQLException;

/**
 * One-time utility tool to generate a proper password4j Argon2 hash
 * for the admin account and (optionally) fix a broken/incompatible hash in the database.
 *
 * This was created because the original admin hash in the DB wasn't generated
 * with password4j, so login was failing even with the correct password.
 *
 * After running, it prints the correct hash + the exact SQL UPDATE command you need.
 */
public class GenerateAndUpdateHash {
    
    public static void main(String[] args) {
        // The real admin password we want to support
        String password = "PhishNetAdmin124";
        String EMAIL = "admin@phishnet.com";   // must match the record in the DB
        
        System.out.println("=== Generating Password4j-Compatible Hash ===\n");
        System.out.println("Password: " + password);
        System.out.println("Email: " + EMAIL);
        System.out.println();
        
        // Step 1: Generate a fresh, correct hash using our SecurityUtils (which uses password4j)
        System.out.println("1. Generating hash using password4j...");
        String hash = SecurityUtils.hashPassword(password);
        
        if (hash == null) {
            System.err.println("   Hash generation FAILED!");
            return;
        }
        
        System.out.println("   Hash generated successfully");
        System.out.println("   Hash: " + hash);
        System.out.println("   Hash length: " + hash.length());
        System.out.println();
        
        // Step 2: Double-check that verification works with the hash we just made
        System.out.println("2. Verifying hash...");
        boolean verified = SecurityUtils.verifyPassword(password, hash);
        
        if (!verified) {
            System.err.println("   Hash verification FAILED!");
            System.err.println("   Something is wrong with SecurityUtils - do not use this hash!");
            return;
        }
        
        System.out.println("   Hash verification SUCCESSFUL");
        System.out.println();
        
        // Step 3: Connect to the database and see what's currently stored
        System.out.println("3. Testing with database...");
        try {
            AdministratorDAOImpl adminDAO = new AdministratorDAOImpl();
            Administrator admin = adminDAO.findByEmail(EMAIL);
            
            if (admin == null) {
                System.err.println("   Admin account not found in database");
                System.err.println("   Run PhishNet-inserts.sql first to create the admin user");
            } else {
                System.out.println("   Admin account found: " + admin.getName());
                System.out.println("   Current hash length: " + admin.getPasswordHash().length());
                
                // Step 4: Test if the current stored hash actually works with the password
                System.out.println();
                System.out.println("4. Testing current database hash...");
                boolean currentHashWorks = SecurityUtils.verifyPassword(password, admin.getPasswordHash());
                System.out.println("   Current hash verification: " + (currentHashWorks ? "WORKS" : "FAILS"));
                
                if (!currentHashWorks) {
                    // The fix: give the exact SQL needed to update the bad hash
                    System.out.println();
                    System.out.println("5. Generating NEW hash to replace the current one...");
                    System.out.println("   New hash: " + hash);
                    System.out.println();
                    System.out.println("   SQL UPDATE command to fix this:");
                    System.out.println("   USE CybersecurityDB;");
                    System.out.println("   UPDATE Administrators");
                    System.out.println("   SET PasswordHash = '" + hash + "'");
                    System.out.println("   WHERE ContactEmail = '" + EMAIL + "';");
                    System.out.println();
                    System.out.println("   After running this SQL, the password 'PhishNetAdmin124' will work again.");
                } else {
                    System.out.println("   Current hash works! No update needed.");
                }
            }
        } catch (SQLException e) {
            System.err.println("   Database error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        System.out.println("=== Complete ===");
        System.out.println("Copy the hash above and run the printed SQL command if needed.");
    }
}