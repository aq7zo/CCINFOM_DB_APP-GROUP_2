package app.controllers;

import app.config.SessionManager;
import app.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller for User (Victim) Dashboard
 */
public class UserDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button submitIncidentButton;
    @FXML private Button uploadEvidenceButton;
    @FXML private Button viewReportsButton;
    @FXML private Button logoutButton;
    
    private final AuthService authService = new AuthService();
    
    @FXML
    private void initialize() {
        SessionManager session = SessionManager.getInstance();
        welcomeLabel.setText("Welcome, " + session.getName() + "!");
    }
    
    @FXML
    private void handleSubmitIncident() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubmitIncidentView.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Submit Incident Report");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to open incident form: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUploadEvidence() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UploadEvidenceView.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle("Upload Evidence");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to open evidence upload: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewMyReportsView.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("My Incident Reports");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to open reports view: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 600, 400);
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Cybersecurity Incident Reporting System");
        } catch (Exception e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
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

