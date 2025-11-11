package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations
 */
public class DateUtils {
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Convert LocalDateTime to MySQL DATETIME string format
     */
    public static String toDatabaseFormat(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DB_FORMATTER);
    }
    
    /**
     * Convert MySQL DATETIME string to LocalDateTime
     */
    public static LocalDateTime fromDatabaseFormat(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DB_FORMATTER);
    }
    
    /**
     * Format LocalDateTime for display
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_FORMATTER);
    }
    
    /**
     * Get current timestamp as LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}

