package view;

import controller.*;
import java.util.Scanner;

/**
 * Main console menu for the Cybersecurity Incident Reporting System
 */
public class ConsoleMenu {
    private static Scanner scanner = new Scanner(System.in);
    
    /**
     * Display and handle main menu
     */
    public static void showMainMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║  CYBERSECURITY INCIDENT REPORTING SYSTEM              ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("\n1. Manage Victims");
            System.out.println("2. Manage Perpetrators");
            System.out.println("3. Manage Attack Types");
            System.out.println("4. Manage Administrators");
            System.out.println("5. Create Incident Report");
            System.out.println("6. Upload Evidence");
            System.out.println("7. View Incidents");
            System.out.println("8. Manage Evidence");
            System.out.println("9. Generate Reports");
            System.out.println("10. Exit");
            System.out.print("\nSelect option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    VictimView.showVictimMenu();
                    break;
                case 2:
                    PerpetratorView.showPerpetratorMenu();
                    break;
                case 3:
                    showAttackTypeMenu();
                    break;
                case 4:
                    showAdminMenu();
                    break;
                case 5:
                    createIncidentReport();
                    break;
                case 6:
                    uploadEvidence();
                    break;
                case 7:
                    viewIncidents();
                    break;
                case 8:
                    manageEvidence();
                    break;
                case 9:
                    ReportView.showReportMenu();
                    break;
                case 10:
                    System.out.println("\nThank you for using the Cybersecurity Incident Reporting System!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private static void showAttackTypeMenu() {
        while (true) {
            System.out.println("\n=== ATTACK TYPE MANAGEMENT ===");
            System.out.println("1. Add Attack Type");
            System.out.println("2. View All Attack Types");
            System.out.println("3. Update Attack Type");
            System.out.println("4. Delete Attack Type");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    System.out.print("Enter attack name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter description: ");
                    String description = scanner.nextLine();
                    System.out.println("Select severity:");
                    System.out.println("1. Low");
                    System.out.println("2. Medium");
                    System.out.println("3. High");
                    System.out.print("Choice: ");
                    int sevChoice = scanner.nextInt();
                    scanner.nextLine();
                    String severity = switch (sevChoice) {
                        case 1 -> "Low";
                        case 2 -> "Medium";
                        case 3 -> "High";
                        default -> "Low";
                    };
                    AttackTypeController.createAttackType(name, description, severity);
                    break;
                case 2:
                    System.out.println("\n--- All Attack Types ---");
                    var attackTypes = AttackTypeController.getAllAttackTypes();
                    if (attackTypes.isEmpty()) {
                        System.out.println("No attack types found.");
                    } else {
                        System.out.println(String.format("%-10s %-30s %-50s %-15s",
                            "ID", "Name", "Description", "Severity"));
                        System.out.println("-".repeat(105));
                        for (var at : attackTypes) {
                            String desc = at.getDescription();
                            if (desc != null && desc.length() > 50) {
                                desc = desc.substring(0, 47) + "...";
                            }
                            System.out.println(String.format("%-10d %-30s %-50s %-15s",
                                at.getAttackTypeID(),
                                at.getAttackName(),
                                desc != null ? desc : "N/A",
                                at.getSeverityLevel()));
                        }
                    }
                    break;
                case 3:
                    System.out.print("Enter Attack Type ID: ");
                    int atId = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Enter new attack name: ");
                    String newName = scanner.nextLine();
                    System.out.print("Enter new description: ");
                    String newDesc = scanner.nextLine();
                    System.out.println("Select new severity:");
                    System.out.println("1. Low");
                    System.out.println("2. Medium");
                    System.out.println("3. High");
                    System.out.print("Choice: ");
                    int newSev = scanner.nextInt();
                    scanner.nextLine();
                    String newSeverity = switch (newSev) {
                        case 1 -> "Low";
                        case 2 -> "Medium";
                        case 3 -> "High";
                        default -> "Low";
                    };
                    AttackTypeController.updateAttackType(atId, newName, newDesc, newSeverity);
                    break;
                case 4:
                    System.out.print("Enter Attack Type ID to delete: ");
                    int delId = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Are you sure? (yes/no): ");
                    String confirm = scanner.nextLine();
                    if ("yes".equalsIgnoreCase(confirm)) {
                        AttackTypeController.deleteAttackType(delId);
                    }
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    
    private static void showAdminMenu() {
        while (true) {
            System.out.println("\n=== ADMINISTRATOR MANAGEMENT ===");
            System.out.println("1. Register Administrator");
            System.out.println("2. View All Administrators");
            System.out.println("3. Authenticate (Login)");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    System.out.print("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.println("Select role:");
                    System.out.println("1. System Admin");
                    System.out.println("2. Cybersecurity Staff");
                    System.out.print("Choice: ");
                    int roleChoice = scanner.nextInt();
                    scanner.nextLine();
                    String role = (roleChoice == 1) ? "System Admin" : "Cybersecurity Staff";
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();
                    AdminController.createAdministrator(name, role, email);
                    break;
                case 2:
                    System.out.println("\n--- All Administrators ---");
                    var admins = AdminController.getAllAdministrators();
                    if (admins.isEmpty()) {
                        System.out.println("No administrators found.");
                    } else {
                        System.out.println(String.format("%-10s %-30s %-25s %-30s",
                            "ID", "Name", "Role", "Email"));
                        System.out.println("-".repeat(95));
                        for (var admin : admins) {
                            System.out.println(String.format("%-10d %-30s %-25s %-30s",
                                admin.getAdminID(),
                                admin.getName(),
                                admin.getRole(),
                                admin.getContactEmail()));
                        }
                    }
                    break;
                case 3:
                    System.out.print("Enter email to login: ");
                    String loginEmail = scanner.nextLine();
                    var admin = AdminController.authenticate(loginEmail);
                    if (admin != null) {
                        System.out.println("\n✓ Login successful!");
                        System.out.println("Welcome, " + admin.getName() + " (" + admin.getRole() + ")");
                    } else {
                        System.out.println("✗ Login failed. Email not found.");
                    }
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    
    private static void createIncidentReport() {
        System.out.println("\n--- Create Incident Report ---");
        System.out.print("Enter Victim ID: ");
        int victimID = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Enter Perpetrator Identifier: ");
        String identifier = scanner.nextLine();
        
        System.out.println("Select Identifier Type:");
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
        
        System.out.print("Enter Associated Name (optional): ");
        String associatedName = scanner.nextLine();
        if (associatedName.isEmpty()) {
            associatedName = null;
        }
        
        // Get or create perpetrator
        int perpetratorID = PerpetratorController.createOrGetPerpetrator(identifier, identifierType, associatedName);
        
        // Show attack types
        var attackTypes = AttackTypeController.getAllAttackTypes();
        if (attackTypes.isEmpty()) {
            System.out.println("No attack types available. Please add attack types first.");
            return;
        }
        
        System.out.println("\nSelect Attack Type:");
        for (var at : attackTypes) {
            System.out.println(at.getAttackTypeID() + ". " + at.getAttackName() + " (" + at.getSeverityLevel() + ")");
        }
        System.out.print("Enter Attack Type ID: ");
        int attackTypeID = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Enter Admin ID (or 0 for none): ");
        int adminID = scanner.nextInt();
        scanner.nextLine();
        Integer admin = (adminID > 0) ? adminID : null;
        
        System.out.print("Enter description: ");
        String description = scanner.nextLine();
        
        IncidentController.createIncidentReport(victimID, perpetratorID, attackTypeID, admin, description);
    }
    
    private static void uploadEvidence() {
        System.out.println("\n--- Upload Evidence ---");
        System.out.print("Enter Incident ID: ");
        int incidentID = scanner.nextInt();
        scanner.nextLine();
        
        System.out.println("Select Evidence Type:");
        System.out.println("1. Screenshot");
        System.out.println("2. Email");
        System.out.println("3. File");
        System.out.println("4. Chat Log");
        System.out.print("Choice: ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine();
        
        String evidenceType = switch (typeChoice) {
            case 1 -> "Screenshot";
            case 2 -> "Email";
            case 3 -> "File";
            case 4 -> "Chat Log";
            default -> null;
        };
        
        if (evidenceType == null) {
            System.out.println("Invalid evidence type.");
            return;
        }
        
        System.out.print("Enter file path: ");
        String filePath = scanner.nextLine();
        
        System.out.print("Enter Admin ID (or 0 for none): ");
        int adminID = scanner.nextInt();
        scanner.nextLine();
        Integer admin = (adminID > 0) ? adminID : null;
        
        EvidenceController.uploadEvidence(incidentID, evidenceType, filePath, admin);
    }
    
    private static void viewIncidents() {
        System.out.println("\n--- All Incident Reports ---");
        var incidents = IncidentController.getAllIncidentReports();
        
        if (incidents.isEmpty()) {
            System.out.println("No incidents found.");
        } else {
            System.out.println(String.format("%-12s %-10s %-15s %-12s %-10s %-20s %-15s",
                "Incident ID", "Victim ID", "Perpetrator ID", "Attack Type", "Admin ID", "Date Reported", "Status"));
            System.out.println("-".repeat(95));
            for (var incident : incidents) {
                System.out.println(String.format("%-12d %-10d %-15d %-12d %-10s %-20s %-15s",
                    incident.getIncidentID(),
                    incident.getVictimID(),
                    incident.getPerpetratorID(),
                    incident.getAttackTypeID(),
                    incident.getAdminID() != null ? incident.getAdminID().toString() : "N/A",
                    incident.getDateReported() != null ? incident.getDateReported().toString() : "N/A",
                    incident.getStatus()));
            }
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. Validate Incident Report");
        System.out.println("2. Back");
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        if (choice == 1) {
            System.out.print("Enter Incident ID to validate: ");
            int incidentID = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Enter Admin ID: ");
            int adminID = scanner.nextInt();
            scanner.nextLine();
            IncidentController.validateIncidentReport(incidentID, adminID);
        }
    }
    
    private static void manageEvidence() {
        while (true) {
            System.out.println("\n=== EVIDENCE MANAGEMENT ===");
            System.out.println("1. View All Evidence");
            System.out.println("2. View Evidence by Incident ID");
            System.out.println("3. Update Evidence Status");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    System.out.println("\n--- All Evidence ---");
                    var allEvidence = EvidenceController.getAllEvidence();
                    if (allEvidence.isEmpty()) {
                        System.out.println("No evidence found.");
                    } else {
                        System.out.println(String.format("%-12s %-12s %-15s %-40s %-20s %-15s",
                            "Evidence ID", "Incident ID", "Type", "File Path", "Submission Date", "Status"));
                        System.out.println("-".repeat(115));
                        for (var ev : allEvidence) {
                            String path = ev.getFilePath();
                            if (path.length() > 40) {
                                path = path.substring(0, 37) + "...";
                            }
                            System.out.println(String.format("%-12d %-12d %-15s %-40s %-20s %-15s",
                                ev.getEvidenceID(),
                                ev.getIncidentID(),
                                ev.getEvidenceType(),
                                path,
                                ev.getSubmissionDate() != null ? ev.getSubmissionDate().toString() : "N/A",
                                ev.getVerifiedStatus()));
                        }
                    }
                    break;
                case 2:
                    System.out.print("Enter Incident ID: ");
                    int incidentID = scanner.nextInt();
                    scanner.nextLine();
                    var evidence = EvidenceController.getEvidenceByIncident(incidentID);
                    if (evidence.isEmpty()) {
                        System.out.println("No evidence found for this incident.");
                    } else {
                        System.out.println(String.format("%-12s %-15s %-40s %-20s %-15s",
                            "Evidence ID", "Type", "File Path", "Submission Date", "Status"));
                        System.out.println("-".repeat(105));
                        for (var ev : evidence) {
                            String path = ev.getFilePath();
                            if (path.length() > 40) {
                                path = path.substring(0, 37) + "...";
                            }
                            System.out.println(String.format("%-12d %-15s %-40s %-20s %-15s",
                                ev.getEvidenceID(),
                                ev.getEvidenceType(),
                                path,
                                ev.getSubmissionDate() != null ? ev.getSubmissionDate().toString() : "N/A",
                                ev.getVerifiedStatus()));
                        }
                    }
                    break;
                case 3:
                    System.out.print("Enter Evidence ID: ");
                    int evID = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Select new status:");
                    System.out.println("1. Pending");
                    System.out.println("2. Verified");
                    System.out.println("3. Rejected");
                    System.out.print("Choice: ");
                    int statusChoice = scanner.nextInt();
                    scanner.nextLine();
                    String status = switch (statusChoice) {
                        case 1 -> "Pending";
                        case 2 -> "Verified";
                        case 3 -> "Rejected";
                        default -> null;
                    };
                    if (status != null) {
                        System.out.print("Enter Admin ID: ");
                        int adminID = scanner.nextInt();
                        scanner.nextLine();
                        EvidenceController.updateEvidenceStatus(evID, status, adminID);
                    }
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}

