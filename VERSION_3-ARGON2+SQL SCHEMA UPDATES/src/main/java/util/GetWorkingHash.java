package util;

/**
 * One-click utility to generate a 100% working Argon2id hash for the main admin password.
 *
 * This was created as the final, foolproof fix during the login crisis.
 * Running this class:
 * - Generates a fresh hash using the exact same SecurityUtils method as the real login
 * - Immediately verifies it works
 * - Prints the exact SQL UPDATE command needed to fix the admin account
 */
public class GetWorkingHash {
    
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("========================================");
        System.out.println("Generating password4j-compatible hash");
        System.out.println("Password: " + password);
        System.out.println("========================================");
        System.out.println();
        
        // Generate the hash exactly the same way the login system does
        String hash = SecurityUtils.hashPassword(password);
        
        if (hash == null) {
            System.err.println("ERROR: Failed to generate hash!");
            System.err.println("Check SecurityUtils and password4j dependency.");
            System.exit(1);
            return;
        }
        
        // Double-check: make sure the hash actually verifies
        boolean verified = SecurityUtils.verifyPassword(password, hash);
        
        if (!verified) {
            System.err.println("ERROR: Generated hash does NOT verify!");
            System.err.println("Something is seriously wrong with the hashing pipeline.");
            System.exit(1);
            return;
        }
        
        System.out.println("SUCCESS! Hash generated and verified correctly.");
        System.out.println();
        System.out.println("Full hash:");
        System.out.println(hash);
        System.out.println();
        System.out.println("========================================");
        System.out.println("SQL UPDATE COMMAND (copy & paste):");
        System.out.println("========================================");
        System.out.println();
        System.out.println("USE CybersecurityDB;");
        System.out.println();
        System.out.println("UPDATE Administrators");
        System.out.println("SET PasswordHash = '" + hash + "'");
        System.out.println("WHERE ContactEmail = 'admin@phishnet.com';");
        System.out.println();
        System.out.println("========================================");
        System.out.println("After running this SQL, admin login will work immediately.");
    }
}