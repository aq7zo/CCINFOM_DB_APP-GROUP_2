package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for security operations (encryption, hashing)
 * Complies with RA 10173 (Philippine Data Privacy Act)
 */
public class SecurityUtils {
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
            return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
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
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Decryption error: " + e.getMessage());
            return encryptedData;
        }
    }
    
    /**
     * Hash password (for future admin authentication)
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Hashing error: " + e.getMessage());
            return password;
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

