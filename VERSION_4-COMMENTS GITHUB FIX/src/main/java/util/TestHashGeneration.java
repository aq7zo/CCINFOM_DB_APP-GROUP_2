package util;

/**
 * Simple standalone test to confirm that our SecurityUtils class (which wraps password4j)
 * can correctly generate an Argon2id hash AND verify it immediately.
 *
 * This was used during development to make sure the hashing pipeline worked end-to-end
 * before putting the hash into the database. If this test passes and prints a valid hash,
 * you can safely copy it into your SQL script or UPDATE statement.
 */
public class TestHashGeneration {
    
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("=== Testing Hash Generation and Verification ===\n");
        System.out.println("Password: " + password + "\n");
        
        // Step 1: Generate the hash using our centralized SecurityUtils helper
        System.out.println("1. Generating hash using SecurityUtils.hashPassword()...");
        String hash = SecurityUtils.hashPassword(password);
        
        if (hash == null) {
            System.err.println("   Hash generation FAILED!");
            return;
        }
        
        System.out.println("   Hash generated successfully");
        System.out.println("   Hash: " + hash);
        System.out.println("   Hash length: " + hash.length());
        System.out.println("   Hash format: " + (hash.startsWith("$argon2id$") ? "Valid Argon2id" : "Invalid"));
        System.out.println();
        
        // Step 2: Immediately verify that the hash we just created actually works
        System.out.println("2. Verifying hash with SecurityUtils.verifyPassword()...");
        boolean verified = SecurityUtils.verifyPassword(password, hash);
        
        if (verified) {
            System.out.println("   Password verification SUCCESSFUL");
            System.out.println();
            System.out.println("=== All good! You can safely use this hash in the database ===");
            System.out.println(hash);
            System.out.println();
            System.out.println("SQL UPDATE command (copy-paste ready):");
            System.out.println("UPDATE Administrators SET PasswordHash = '" + hash + "' WHERE ContactEmail = 'admin@phishnet.com';");
        } else {
            System.err.println("   Password verification FAILED!");
            System.err.println("   Something is wrong with the hashing/verification logic — do NOT use this hash!");
        }
    }
}