package app;

import app.config.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Main JavaFX application entry point
 */
public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
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
            return;
        }
        
        // Load login view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1080, 720);
        scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        
        primaryStage.setTitle("Cybersecurity Incident Reporting System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
        System.out.println("System shutdown complete.");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

