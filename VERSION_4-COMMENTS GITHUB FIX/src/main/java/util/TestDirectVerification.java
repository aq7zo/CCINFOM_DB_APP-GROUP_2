package util;

import com.password4j.Password;

/**
 * Final debugging tool used to pinpoint why admin login was failing.
 * 
 * This class tests password verification in three different ways:
 * 1. Directly with password4j (no wrapper)
 * 2. Through our own SecurityUtils wrapper
 * 3. By generating a fresh hash and verifying it immediately
 * 
 * It was critical in confirming that:
 * - password4j itself works perfectly
 * - SecurityUtils works correctly when given a properly generated hash
 * - The real problem was usually an old/incompatible hash stored in the database
 */
public class TestDirectVerification {
    
    public static void main(String[] args) {
        String password = "PhishNetAdmin124";
        // This is the hash that was originally in the database (and failing login)
        String hash = "$argon2id$v=19$m=65536,t=3,p=4$OC12rXF4qT5BVigv61onEQ$wy1XG9i6aAB47LZN2RmsD7QOZfYxR7Ssz2QZ6QUAmRM";
        
        System.out.println("=== Direct Verification Test ===\n");
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println();
        
        // Test 1: Verify directly using password4j — bypasses our code entirely
        System.out.println("Test 1: Direct Password.check().withArgon2()");
        try {
            boolean result = Password.check(password, hash).withArgon2();
            System.out.println("Result: " + result);
            System.out.println("Status: " + (result ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test 2: Verify using our own SecurityUtils wrapper (the one used in login)
        System.out.println("Test 2: Using SecurityUtils.verifyPassword()");
        try {
            boolean result = SecurityUtils.verifyPassword(password, hash);
            System.out.println("Result: " + result);
            System.out.println("Status: " + (result ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        
        // Test 3: Generate a brand new hash and verify it right away
        // This proves that hashing + verification works when everything is done correctly
        System.out.println("Test 3: Generate new hash and verify immediately");
        try {
            String newHash = SecurityUtils.hashPassword(password);
            System.out.println("New hash: " + newHash);
            boolean result = SecurityUtils.verifyPassword(password, newHash);
            System.out.println("Verification: " + (result ? "SUCCESS" : "FAILED"));
            if (result) {
                System.out.println("   → Our hashing/verification pipeline works perfectly!");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}