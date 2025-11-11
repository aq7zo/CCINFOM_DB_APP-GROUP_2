package view;

import controller.PerpetratorController;
import model.Perpetrator;
import java.util.List;
import java.util.Scanner;

/**
 * View class for Perpetrator management console interface
 */
public class PerpetratorView {
    private static Scanner scanner = new Scanner(System.in);
    
    /**
     * Display perpetrator management menu
     */
    public static void showPerpetratorMenu() {
        while (true) {
            System.out.println("\n=== PERPETRATOR MANAGEMENT ===");
            System.out.println("1. Add New Perpetrator");
            System.out.println("2. View All Perpetrators");
            System.out.println("3. View Perpetrator by ID");
            System.out.println("4. Update Threat Level");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    addPerpetrator();
                    break;
                case 2:
                    viewAllPerpetrators();
                    break;
                case 3:
                    viewPerpetratorById();
                    break;
                case 4:
                    updateThreatLevel();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private static void addPerpetrator() {
        System.out.println("\n--- Add New Perpetrator ---");
        System.out.print("Enter identifier: ");
        String identifier = scanner.nextLine();
        
        System.out.println("Select identifier type:");
        System.out.println("1. Phone Number");
        System.out.println("2. Email Address");
        System.out.println("3. Social Media Account");
        System.out.println("4. Website URL");
        System.out.println("5. IP Address");
        System.out.print("Choice: ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine();
        
        String identifierType = switch (typeChoice) {
            case 1 -> "Phone Number";
            case 2 -> "Email Address";
            case 3 -> "Social Media Account";
            case 4 -> "Website URL";
            case 5 -> "IP Address";
            default -> null;
        };
        
        if (identifierType == null) {
            System.out.println("Invalid identifier type.");
            return;
        }
        
        System.out.print("Enter associated name (optional): ");
        String associatedName = scanner.nextLine();
        if (associatedName.isEmpty()) {
            associatedName = null;
        }
        
        PerpetratorController.createOrGetPerpetrator(identifier, identifierType, associatedName);
    }
    
    private static void viewAllPerpetrators() {
        System.out.println("\n--- All Perpetrators ---");
        List<Perpetrator> perpetrators = PerpetratorController.getAllPerpetrators();
        
        if (perpetrators.isEmpty()) {
            System.out.println("No perpetrators found.");
        } else {
            System.out.println(String.format("%-10s %-30s %-25s %-20s %-15s",
                "ID", "Identifier", "Type", "Associated Name", "Threat Level"));
            System.out.println("-".repeat(100));
            for (Perpetrator p : perpetrators) {
                System.out.println(String.format("%-10d %-30s %-25s %-20s %-15s",
                    p.getPerpetratorID(),
                    p.getIdentifier(),
                    p.getIdentifierType(),
                    p.getAssociatedName() != null ? p.getAssociatedName() : "N/A",
                    p.getThreatLevel()));
            }
        }
    }
    
    private static void viewPerpetratorById() {
        System.out.println("\n--- View Perpetrator by ID ---");
        System.out.print("Enter Perpetrator ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        Perpetrator perpetrator = PerpetratorController.getPerpetratorById(id);
        if (perpetrator != null) {
            System.out.println("\nPerpetrator Details:");
            System.out.println("ID: " + perpetrator.getPerpetratorID());
            System.out.println("Identifier: " + perpetrator.getIdentifier());
            System.out.println("Type: " + perpetrator.getIdentifierType());
            System.out.println("Associated Name: " + 
                (perpetrator.getAssociatedName() != null ? perpetrator.getAssociatedName() : "N/A"));
            System.out.println("Threat Level: " + perpetrator.getThreatLevel());
            System.out.println("Last Incident Date: " + 
                (perpetrator.getLastIncidentDate() != null ? perpetrator.getLastIncidentDate() : "N/A"));
        } else {
            System.out.println("Perpetrator not found.");
        }
    }
    
    private static void updateThreatLevel() {
        System.out.println("\n--- Update Threat Level ---");
        System.out.print("Enter Perpetrator ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        System.out.println("Select new threat level:");
        System.out.println("1. UnderReview");
        System.out.println("2. Suspected");
        System.out.println("3. Malicious");
        System.out.println("4. Cleared");
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        String threatLevel = switch (choice) {
            case 1 -> "UnderReview";
            case 2 -> "Suspected";
            case 3 -> "Malicious";
            case 4 -> "Cleared";
            default -> null;
        };
        
        if (threatLevel != null) {
            System.out.print("Enter Admin ID (or 0 for auto-escalation): ");
            int adminID = scanner.nextInt();
            scanner.nextLine();
            Integer admin = (adminID > 0) ? adminID : null;
            
            PerpetratorController.updateThreatLevel(id, threatLevel, admin);
        } else {
            System.out.println("Invalid threat level choice.");
        }
    }
}

