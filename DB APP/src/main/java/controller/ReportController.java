package controller;

import utils.ReportGenerator;
import java.time.LocalDateTime;

/**
 * Controller for Report generation operations
 */
public class ReportController {
    
    /**
     * Generate monthly attack trends report
     */
    public static void generateMonthlyAttackTrends(int year, int month) {
        String report = ReportGenerator.generateMonthlyAttackTrends(year, month);
        System.out.println(report);
    }
    
    /**
     * Generate monthly attack trends for current month
     */
    public static void generateMonthlyAttackTrends() {
        LocalDateTime now = LocalDateTime.now();
        generateMonthlyAttackTrends(now.getYear(), now.getMonthValue());
    }
    
    /**
     * Generate top perpetrators report
     */
    public static void generateTopPerpetrators(int year, int month) {
        String report = ReportGenerator.generateTopPerpetrators(year, month);
        System.out.println(report);
    }
    
    /**
     * Generate top perpetrators for current month
     */
    public static void generateTopPerpetrators() {
        LocalDateTime now = LocalDateTime.now();
        generateTopPerpetrators(now.getYear(), now.getMonthValue());
    }
    
    /**
     * Generate victim activity report
     */
    public static void generateVictimActivity(int year, int month) {
        String report = ReportGenerator.generateVictimActivity(year, month);
        System.out.println(report);
    }
    
    /**
     * Generate victim activity for current month
     */
    public static void generateVictimActivity() {
        LocalDateTime now = LocalDateTime.now();
        generateVictimActivity(now.getYear(), now.getMonthValue());
    }
    
    /**
     * Generate evidence summary report
     */
    public static void generateEvidenceSummary() {
        String report = ReportGenerator.generateEvidenceSummary();
        System.out.println(report);
    }
}

