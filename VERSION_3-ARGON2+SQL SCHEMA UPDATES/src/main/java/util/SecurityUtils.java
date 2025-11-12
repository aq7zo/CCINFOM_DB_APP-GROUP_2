package util;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Utility class for security operations (password hashing with Argon2)
 * Complies with RA 10173 (Philippine Data Privacy Act)
 * 
 * Uses Argon2id for password hashing, which provides protection against
 * both side-channel attacks and GPU cracking attempts.
 */
public class SecurityUtils {
    // Argon2 configuration parameters
    private static final int ITERATIONS = 3;              // Time cost (number of iterations)
    private static final int MEMORY_COST = 65536;         // Memory cost (64 MB in KB)
    private static final int PARALLELISM = 4;            // Degree of parallelism (threads)
    private static final int SALT_LENGTH = 16;           // Salt length in bytes
    private static final int HASH_LENGTH = 32;           // Hash length in bytes
    
    // Argon2 function instance with configured parameters (Argon2id variant)
    // Initialize as static final for better performance
    private static final Argon2Function ARGON2_FUNCTION;
    
    static {
        // Initialize Argon2Function with Argon2id variant
        ARGON2_FUNCTION = Argon2Function.getInstance(
                MEMORY_COST,      // Memory cost in KB
                ITERATIONS,       // Iterations
                PARALLELISM,      // Parallelism
                HASH_LENGTH,      // Hash length
                Argon2.ID         // Argon2id variant
        );
    }
    
    /**
     * Hash password using Argon2id
     * 
     * Creates an Argon2id hash with secure parameters. The encoded string
     * contains all metadata (algorithm, version, parameters, salt, hash).
     * 
     * @param password Plain text password to hash
     * @return Encoded Argon2 hash string (contains all metadata), or null if error
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        // Validate password length to prevent DoS via excessive hashing
        if (password.length() > 128) {
            System.err.println("Password exceeds maximum length of 128 characters");
            return null;
        }
        
        try {
            // Hash password with Argon2id using configured parameters
            // Password4j automatically handles salt generation and encoding
            String encodedHash = Password.hash(password)
                    .addRandomSalt(SALT_LENGTH)  // Generate random salt
                    .with(ARGON2_FUNCTION)       // Use configured Argon2 function
                    .getResult();
            
            return encodedHash;
            
        } catch (Exception e) {
            System.err.println("Argon2 hashing error: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Verify password against stored Argon2 hash
     * 
     * Extracts parameters from the encoded hash string and verifies the password.
     * Uses constant-time comparison internally to prevent timing attacks.
     * 
     * @param password Plain text password to verify
     * @param encodedHash Stored Argon2 encoded hash string
     * @return true if password matches hash, false otherwise
     */
    public static boolean verifyPassword(String password, String encodedHash) {
        if (password == null || encodedHash == null || encodedHash.isEmpty()) {
            return false;
        }
        
        // Validate password length
        if (password.length() > 128) {
            return false;
        }
        
        try {
            // Check if it's a legacy SHA-256 hash (for migration support)
            if (!encodedHash.startsWith("$argon2id$") && !encodedHash.startsWith("$argon2i$") && 
                !encodedHash.startsWith("$argon2d$")) {
                // Legacy SHA-256 hash support (for migration)
                return verifyLegacyHash(password, encodedHash);
            }
            
            // Verify password using Argon2
            // Password4j automatically extracts parameters from encoded hash
            // When verifying, we can use withArgon2() without parameters
            // as it will extract them from the encoded hash string
            boolean matches = Password.check(password, encodedHash)
                    .withArgon2();
            
            return matches;
            
        } catch (Exception e) {
            System.err.println("Argon2 verification error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Legacy SHA-256 verification for backward compatibility during migration
     * This method supports old SHA-256 hashes during the transition period
     * 
     * @param password Plain text password
     * @param hash SHA-256 hash (hexadecimal string)
     * @return true if password matches hash
     */
    private static boolean verifyLegacyHash(String password, String hash) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] passwordHash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : passwordHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Constant-time comparison to prevent timing attacks
            return constantTimeEquals(hexString.toString(), hash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks
     * 
     * @param a First string
     * @param b Second string
     * @return true if strings are equal
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    /**
     * Simple encryption for sensitive data (email addresses)
     * In production, use stronger encryption like AES
     */
    public static String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        try {
            // Simple Base64 encoding (for demonstration)
            // In production, use proper encryption
            return java.util.Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Encryption error: " + e.getMessage());
            return data;
        }
    }
    
    /**
     * Decrypt data
     */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(encryptedData);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Decryption error: " + e.getMessage());
            return encryptedData;
        }
    }
    
    /**
     * Anonymize data for reports (RA 10173 compliance)
     */
    public static String anonymizeEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String localPart = email.substring(0, Math.min(2, atIndex));
            return localPart + "***@" + email.substring(atIndex + 1);
        }
        return "***@***";
    }
    
    /**
     * Anonymize name (show only first letter)
     */
    public static String anonymizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() > 1) {
            return name.charAt(0) + "***";
        }
        return "***";
    }
}

