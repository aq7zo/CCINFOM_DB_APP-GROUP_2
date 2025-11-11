import view.ConsoleMenu;
import model.DatabaseConnection;
import java.sql.SQLException;

/**
 * Main entry point for the Cybersecurity Incident Reporting System
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Cybersecurity Incident Reporting System...");
        
        // Test database connection
        try {
            var conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Database connection established successfully.");
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            System.err.println("Please ensure:");
            System.err.println("  1. MySQL server is running");
            System.err.println("  2. Database 'CybersecurityDB' exists");
            System.err.println("  3. Database credentials in DatabaseConnection.java are correct");
            System.err.println("\nYou can create the database by running DB_SCHEMA.sql");
            return;
        }
        
        // Start the console menu
        ConsoleMenu.showMainMenu();
        
        // Cleanup
        DatabaseConnection.closeConnection();
        System.out.println("\nSystem shutdown complete.");
    }
}

