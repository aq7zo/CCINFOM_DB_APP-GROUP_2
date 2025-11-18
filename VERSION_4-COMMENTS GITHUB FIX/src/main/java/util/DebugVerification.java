package util;

import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

/**
 * Temporary debugging utility used to figure out why password verification
 * was failing with a specific Argon2id hash.
 * 
 * This class is NOT part of the real application - it's just a standalone
 * main() class for testing the password4j library behavior during development.
 */
public class DebugVerification {
    
    public static void main(String[] args) {
        // The plain-text password we're testing
        String password = "PhishNetAdmin124";
        
        // The stored hash that was causing issues (generated elsewhere)
        String hash = "$argon2id$v=19$m=65536,t=3,p=4$pq/DXvqbc5I1LyiuD+KyFw$uCZ6pixAem8bg4coXkWIEv9QKd9YUcl16DPQuJavCw";
        
        System.out.println("=== Debugging Password Verification ===\n");
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println();
        
        // Test 1: using the default Argon2 configuration (no explicit parameters)
        System.out.println("Test 1: Password.check().withArgon2()");
        try {
            boolean result1 = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + result1);
            System.out.println("Status: " + (result1 ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test 2: explicitly specify the exact Argon2 parameters that were used
        // when the hash was originally created (m=65536, t=3, p=4, salt length=16, type=ID)
        System.out.println("Test 2: Password.check().with(ARGON2_FUNCTION)");
        try {
            Argon2Function argon2 = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
            boolean result2 = Password.check(password, hash).with(argon2);
            System.out.println("Result: " + result2);
            System.out.println("Status: " + (result2 ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test 3: generate a fresh hash with known parameters and immediately verify it
        // This helps confirm that hashing + verification works correctly in our environment
        System.out.println("Test 3: Generate new hash and verify immediately");
        try {
            Argon2Function argon2 = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);
            String newHash = Password.hash(password)
                    .addRandomSalt(16)           // 16-byte salt = 128-bit, matches most stored hashes
                    .with(argon2)
                    .getResult();
            System.out.println("New hash: " + newHash);
            
            boolean result3a = Password.check(password, newHash).withArgon2();
            System.out.println("Verify with withArgon2(): " + result3a);
            
            boolean result3b = Password.check(password, newHash).with(argon2);
            System.out.println("Verify with with(argon2): " + result3b);
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}