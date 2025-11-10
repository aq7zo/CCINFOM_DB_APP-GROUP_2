package app.controllers;

import app.config.SessionManager;
import app.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for Login view
 */
public class LoginController {
    @FXML private TextField emailField;
    @FXML private Button loginAsUserButton;
    @FXML private Button loginAsAdminButton;
    
    private final AuthService authService = new AuthService();
    
    @FXML
    private void initialize() {
        // Initialize UI if needed
    }
    
    @FXML
    private void handleLoginAsUser() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert("Error", "Please enter your email address.");
            return;
        }
        
        if (authService.loginAsUser(email)) {
            loadDashboard("UserDashboard");
        } else {
            showAlert("Login Failed", "Email not found. Please check your email or register as a victim first.");
        }
    }
    
    @FXML
    private void handleLoginAsAdmin() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert("Error", "Please enter your email address.");
            return;
        }
        
        if (authService.loginAsAdmin(email)) {
            loadDashboard("AdminDashboard");
        } else {
            showAlert("Login Failed", "Email not found. Please check your email or register as an administrator first.");
        }
    }
    
    private void loadDashboard(String dashboardName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + dashboardName + ".fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Cybersecurity Incident Reporting System - " + 
                          (SessionManager.getInstance().isAdmin() ? "Admin Dashboard" : "User Dashboard"));
            stage.setResizable(true);
        } catch (Exception e) {
            showAlert("Error", "Failed to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

