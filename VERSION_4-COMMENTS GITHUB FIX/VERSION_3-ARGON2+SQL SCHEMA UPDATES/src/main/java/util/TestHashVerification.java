package util;

import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

/**
 * Simple diagnostic tool used during development to test password4j hash generation
 * and verification directly. It helps confirm whether the library itself is working
 * or if the problem lies elsewhere.
 *
 * This class was part of the troubleshooting process when admin login wasn't working.
 * It generates a fresh hash and runs multiple verification tests (including a wrong password).
 */
public class TestHashVerification {
    
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        
        System.out.println("=== Testing Hash Generation and Verification ===\n");
        System.out.println("Password: " + password);
        System.out.println();
        
        // Use exactly the same Argon2 parameters as SecurityUtils for a fair test
        Argon2Function argon2 = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
        
        // Step 1: Generate a hash (note: no explicit salt added here — password4j generates one automatically)
        System.out.println("1. Generating hash...");
        String hash = Password.hash(password)
                .with(argon2)
                .getResult();
        
        System.out.println("Hash: " + hash);
        System.out.println("Hash length: " + hash.length());
        System.out.println();
        
        // Step 2: Verify using the default .withArgon2() — password4j should auto-detect parameters from the hash
        System.out.println("2. Testing verification with withArgon2()...");
        try {
            boolean verified1 = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + verified1);
            System.out.println("Status: " + (verified1 ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Step 3: Verify by explicitly passing the Argon2Function instance
        System.out.println("3. Testing verification with with(argon2)...");
        try {
            boolean verified2 = Password.check(password, hash).with(argon2);
            System.out.println("Result: " + verified2);
            System.out.println("Status: " + (verified2 ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Step 4: Sanity check — make sure a wrong password is correctly rejected
        System.out.println("4. Testing with wrong password (should fail)...");
        try {
            boolean verified3 = Password.check("wrongpassword", hash).withArgon2();
            System.out.println("Result: " + verified3);
            System.out.println("Status: " + (!verified3 ? "Correctly rejected" : "Should have failed"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}