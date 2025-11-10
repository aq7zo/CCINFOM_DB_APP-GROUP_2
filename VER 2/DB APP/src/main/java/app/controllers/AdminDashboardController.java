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
 * Controller for Admin Dashboard
 */
public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button manageVictimsButton;
    @FXML private Button managePerpetratorsButton;
    @FXML private Button manageAttackTypesButton;
    @FXML private Button viewAllIncidentsButton;
    @FXML private Button createIncidentButton;
    @FXML private Button validateReportsButton;
    @FXML private Button generateReportsButton;
    @FXML private Button logoutButton;
    
    private final AuthService authService = new AuthService();
    
    @FXML
    private void initialize() {
        SessionManager session = SessionManager.getInstance();
        welcomeLabel.setText("Welcome, " + session.getName() + " (" + session.getAdmin().getRole() + ")");
    }
    
    @FXML
    private void handleManageVictims() {
        loadView("ManageVictimsView", "Manage Victims", 900, 600);
    }
    
    @FXML
    private void handleManagePerpetrators() {
        loadView("ManagePerpetratorsView", "Manage Perpetrators", 900, 600);
    }
    
    @FXML
    private void handleManageAttackTypes() {
        loadView("ManageAttackTypesView", "Manage Attack Types", 800, 500);
    }
    
    @FXML
    private void handleViewAllIncidents() {
        loadView("ViewAllIncidentsView", "All Incident Reports", 1000, 700);
    }
    
    @FXML
    private void handleCreateIncident() {
        loadView("CreateIncidentView", "Create Incident Report", 800, 600);
    }
    
    @FXML
    private void handleValidateReports() {
        loadView("ValidateReportsView", "Validate Reports", 900, 600);
    }
    
    @FXML
    private void handleGenerateReports() {
        loadView("GenerateReportsView", "Generate Reports", 800, 600);
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
    
    private void loadView(String viewName, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + viewName + ".fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to load " + title + ": " + e.getMessage());
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

