package util;

import dao.AdministratorDAOImpl;
import model.Administrator;

import java.sql.SQLException;

/**
 * Practical debugging tool to test whether the password hash currently stored
 * in the database actually works with our current SecurityUtils verification code.
 *
 * Super useful when an admin says "my password stopped working" — you can run this
 * directly against the live (or dev) database and instantly see if the stored hash
 * is compatible or needs to be replaced.
 *
 * Usage:
 *   mvn compile exec:java -Dexec.mainClass="util.TestDatabaseHash" -Dexec.args="admin@phishnet.com PhishNetAdmin124"
 *
 * If verification fails, it automatically generates a correct new hash and prints
 * the exact SQL UPDATE command you need to fix the account.
 */
public class TestDatabaseHash {
    
    public static void main(String[] args) {
        // Expect email and password as command-line arguments
        if (args.length < 2) {
            System.err.println("Usage: TestDatabaseHash <email> <password>");
            System.err.println("Example: TestDatabaseHash benette_campo@dlsu.edu.ph PhishNetAdmin124");
            System.exit(1);
        }
        
        String email = args[0];
        String password = args[1];
        
        System.out.println("========================================");
        System.out.println("Testing Database Hash");
        System.out.println("========================================");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        System.out.println();
        
        try {
            AdministratorDAOImpl adminDAO = new AdministratorDAOImpl();
            Administrator admin = adminDAO.findByEmail(email);
            
            if (admin == null) {
                System.err.println("Admin not found in database!");
                System.err.println("Check the email address or run the SQL inserts first.");
                return;
            }
            
            String hashFromDB = admin.getPasswordHash();
            System.out.println("Admin found: " + admin.getName());
            System.out.println("Hash from DB:");
            System.out.println("  Length: " + hashFromDB.length());
            System.out.println("  Preview: " + (hashFromDB.length() > 60 ? hashFromDB.substring(0, 60) + "..." : hashFromDB));
            System.out.println("  Full hash: " + hashFromDB);
            System.out.println();
            
            // The real test — does the stored hash work with the password?
            System.out.println("Testing verification...");
            boolean verified = SecurityUtils.verifyPassword(password, hashFromDB);
            
            if (verified) {
                System.out.println("VERIFICATION SUCCESSFUL!");
                System.out.println("The stored hash is correct — login should work fine.");
            } else {
                System.out.println("VERIFICATION FAILED!");
                System.out.println("The stored hash is outdated or corrupted.");
                System.out.println();
                System.out.println("Generating a fresh, correct hash for you...");
                String newHash = SecurityUtils.hashPassword(password);
                
                if (newHash != null) {
                    System.out.println("New hash generated successfully:");
                    System.out.println("  Length: " + newHash.length());
                    System.out.println("  Preview: " + (newHash.length() > 60 ? newHash.substring(0, 60) + "..." : newHash));
                    System.out.println();
                    
                    // Double-check the new hash works (sanity check)
                    boolean newHashVerified = SecurityUtils.verifyPassword(password, newHash);
                    System.out.println("New hash verification: " + (newHashVerified ? "SUCCESS" : "FAILED"));
                    System.out.println();
                    
                    // Give the exact SQL needed to fix the broken account
                    System.out.println("=== Run this SQL to fix the account ===");
                    System.out.println("UPDATE Administrators");
                    System.out.println("SET PasswordHash = '" + newHash + "'");
                    System.out.println("WHERE ContactEmail = '" + email + "';");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}