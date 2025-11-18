package util;

import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

/**
 * Standalone test class to verify that the password4j library works correctly
 * on its own, bypassing our SecurityUtils wrapper completely.
 */
public class TestPassword4jDirect {
    
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("=== Direct password4j Test ===\n");
        System.out.println("Password: " + password);
        System.out.println();
        
        // Build the exact same Argon2 configuration we use in SecurityUtils
        Argon2Function argon2 = Argon2Function.getInstance(
            65536,      // memory (64 MB)
            3,          // iterations
            4,          // parallelism (4 threads)
            32,         // output hash length in bytes
            Argon2.ID   // Argon2id variant (most secure)
        );
        
        // Step 1: Generate a fresh hash with known-good parameters
        System.out.println("1. Generating hash...");
        String hash = Password.hash(password)
                .addRandomSalt(16)   // 16-byte salt → standard for Argon2id
                .with(argon2)
                .getResult();
        
        System.out.println("Hash: " + hash);
        System.out.println("Hash length: " + hash.length());
        System.out.println();
        
        // Step 2: Verify using the simple .withArgon2() method (no config needed)
        System.out.println("2. Testing verification method 1: Password.check().withArgon2()");
        try {
            boolean verified1 = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + (verified1 ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Step 3: Verify by passing the exact Argon2Function instance
        System.out.println();
        System.out.println("3. Testing verification method 2: Password.check().with(argon2)");
        try {
            boolean verified2 = Password.check(password, hash).with(argon2);
            System.out.println("Result: " + (verified2 ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Step 4: Let password4j auto-extract parameters from the hash string
        // (this is the recommended way — no need to pass Argon2Function manually)
        System.out.println();
        System.out.println("4. Testing verification method 3: Auto-extract parameters from hash");
        try {
            boolean verified3 = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + (verified3 ? "SUCCESS" : "FAILED"));
            if (verified3) {
                System.out.println("   → password4j correctly read the parameters from the hash string!");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}