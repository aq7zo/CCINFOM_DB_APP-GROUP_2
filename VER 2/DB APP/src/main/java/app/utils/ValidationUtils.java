package app.utils;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate that a string is not null or empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Validate that an integer is positive
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }
    
    /**
     * Validate enum value
     */
    public static boolean isValidEnumValue(String value, String[] validValues) {
        if (value == null) return false;
        for (String valid : validValues) {
            if (valid.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}

