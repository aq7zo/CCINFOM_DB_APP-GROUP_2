package view;

import controller.VictimController;
import model.Victim;
import java.util.List;
import java.util.Scanner;

/**
 * View class for Victim management console interface
 */
public class VictimView {
    private static Scanner scanner = new Scanner(System.in);
    
    /**
     * Display victim management menu
     */
    public static void showVictimMenu() {
        while (true) {
            System.out.println("\n=== VICTIM MANAGEMENT ===");
            System.out.println("1. Register New Victim");
            System.out.println("2. View All Victims");
            System.out.println("3. View Victim by ID");
            System.out.println("4. Update Victim Status");
            System.out.println("5. Delete Victim");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    registerVictim();
                    break;
                case 2:
                    viewAllVictims();
                    break;
                case 3:
                    viewVictimById();
                    break;
                case 4:
                    updateVictimStatus();
                    break;
                case 5:
                    deleteVictim();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private static void registerVictim() {
        System.out.println("\n--- Register New Victim ---");
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        VictimController.createVictim(name, email);
    }
    
    private static void viewAllVictims() {
        System.out.println("\n--- All Victims ---");
        List<Victim> victims = VictimController.getAllVictims();
        
        if (victims.isEmpty()) {
            System.out.println("No victims found.");
        } else {
            System.out.println(String.format("%-10s %-30s %-30s %-15s", 
                "ID", "Name", "Email", "Status"));
            System.out.println("-".repeat(85));
            for (Victim victim : victims) {
                System.out.println(String.format("%-10d %-30s %-30s %-15s",
                    victim.getVictimID(),
                    victim.getName(),
                    victim.getContactEmail(),
                    victim.getAccountStatus()));
            }
        }
    }
    
    private static void viewVictimById() {
        System.out.println("\n--- View Victim by ID ---");
        System.out.print("Enter Victim ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        Victim victim = VictimController.getVictimById(id);
        if (victim != null) {
            System.out.println("\nVictim Details:");
            System.out.println("ID: " + victim.getVictimID());
            System.out.println("Name: " + victim.getName());
            System.out.println("Email: " + victim.getContactEmail());
            System.out.println("Status: " + victim.getAccountStatus());
            System.out.println("Date Created: " + victim.getDateCreated());
        } else {
            System.out.println("Victim not found.");
        }
    }
    
    private static void updateVictimStatus() {
        System.out.println("\n--- Update Victim Status ---");
        System.out.print("Enter Victim ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        System.out.println("Select new status:");
        System.out.println("1. Active");
        System.out.println("2. Flagged");
        System.out.println("3. Suspended");
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        String status = switch (choice) {
            case 1 -> "Active";
            case 2 -> "Flagged";
            case 3 -> "Suspended";
            default -> null;
        };
        
        if (status != null) {
            System.out.print("Enter Admin ID (or 0 for auto-flag): ");
            int adminID = scanner.nextInt();
            scanner.nextLine();
            Integer admin = (adminID > 0) ? adminID : null;
            
            VictimController.updateVictimStatus(id, status, admin);
        } else {
            System.out.println("Invalid status choice.");
        }
    }
    
    private static void deleteVictim() {
        System.out.println("\n--- Delete Victim ---");
        System.out.print("Enter Victim ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Are you sure? (yes/no): ");
        String confirm = scanner.nextLine();
        if ("yes".equalsIgnoreCase(confirm)) {
            VictimController.deleteVictim(id);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
}

