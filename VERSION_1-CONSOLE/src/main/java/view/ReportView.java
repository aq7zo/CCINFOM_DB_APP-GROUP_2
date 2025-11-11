package view;

import controller.ReportController;
import java.util.Scanner;

/**
 * View class for Report generation console interface
 */
public class ReportView {
    private static Scanner scanner = new Scanner(System.in);
    
    /**
     * Display report generation menu
     */
    public static void showReportMenu() {
        while (true) {
            System.out.println("\n=== REPORT GENERATION ===");
            System.out.println("1. Monthly Attack Trends Report");
            System.out.println("2. Top Perpetrators Report");
            System.out.println("3. Victim Activity Report");
            System.out.println("4. Incident Evidence Summary Report");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    generateMonthlyAttackTrends();
                    break;
                case 2:
                    generateTopPerpetrators();
                    break;
                case 3:
                    generateVictimActivity();
                    break;
                case 4:
                    generateEvidenceSummary();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private static void generateMonthlyAttackTrends() {
        System.out.println("\n--- Monthly Attack Trends Report ---");
        System.out.println("1. Current Month");
        System.out.println("2. Specific Month");
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice == 1) {
            ReportController.generateMonthlyAttackTrends();
        } else if (choice == 2) {
            System.out.print("Enter year: ");
            int year = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter month (1-12): ");
            int month = scanner.nextInt();
            scanner.nextLine();
            ReportController.generateMonthlyAttackTrends(year, month);
        }
    }
    
    private static void generateTopPerpetrators() {
        System.out.println("\n--- Top Perpetrators Report ---");
        System.out.println("1. Current Month");
        System.out.println("2. Specific Month");
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice == 1) {
            ReportController.generateTopPerpetrators();
        } else if (choice == 2) {
            System.out.print("Enter year: ");
            int year = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter month (1-12): ");
            int month = scanner.nextInt();
            scanner.nextLine();
            ReportController.generateTopPerpetrators(year, month);
        }
    }
    
    private static void generateVictimActivity() {
        System.out.println("\n--- Victim Activity Report ---");
        System.out.println("1. Current Month");
        System.out.println("2. Specific Month");
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice == 1) {
            ReportController.generateVictimActivity();
        } else if (choice == 2) {
            System.out.print("Enter year: ");
            int year = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter month (1-12): ");
            int month = scanner.nextInt();
            scanner.nextLine();
            ReportController.generateVictimActivity(year, month);
        }
    }
    
    private static void generateEvidenceSummary() {
        System.out.println("\n--- Incident Evidence Summary Report ---");
        ReportController.generateEvidenceSummary();
    }
}

