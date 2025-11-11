package main.java.com.group2.dbapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.group2.dbapp.util.DatabaseConnection;

import java.io.IOException;

/**
 * Main JavaFX Application class
 * Entry point for the Cybersecurity Incident Reporting System
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Test database connection
            if (!DatabaseConnection.testConnection()) {
                showErrorAndExit("Database Connection Failed", 
                    "Could not connect to the database.\n" +
                    "Please ensure MySQL is running and the database is configured correctly.");
                return;
            }
            
            System.out.println("Database connection successful!");
            
            // Load the Login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/LogIn.fxml"));
            Parent root = loader.load();
            
            // Set up the stage
            primaryStage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.setResizable(false);
            primaryStage.show();
            
            System.out.println("Application started successfully!");
            
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAndExit("Application Error", 
                "Failed to load the application interface.\n" +
                "Error: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when application closes
        DatabaseConnection.closeConnection();
        System.out.println("Application closed. Resources cleaned up.");
    }
    
    /**
     * Show error message and exit application
     */
    private void showErrorAndExit(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }
    
    /**
     * Main method - entry point
     */
    public static void main(String[] args) {
        launch(args);
    }
}

