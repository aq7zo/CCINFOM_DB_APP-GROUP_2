package app.controllers;

import app.config.SessionManager;
import app.services.AuthService;
import app.utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

/**
 * Controller for the cyber incident login view.
 * Provides role-aware validation and quick access to the victim reporting portal.
 */
public class LoginController {
    private static final String ROLE_VICTIM = "Victim Reporter";
    private static final String ROLE_ADMIN = "Administrator";

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleSelector;
    @FXML private Label feedbackLabel;
    @FXML private Button victimLoginButton;
    @FXML private Button adminLoginButton;
    @FXML private Button reportAttackButton;
    @FXML private Hyperlink intelBriefLink;
    @FXML private Hyperlink contactSupportLink;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        roleSelector.setItems(FXCollections.observableArrayList(ROLE_VICTIM, ROLE_ADMIN));
        roleSelector.getSelectionModel().select(ROLE_VICTIM);

        roleSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldRole, newRole) -> {
            if (newRole != null) {
                updateButtonStates(newRole);
            }
        });

        updateButtonStates(ROLE_VICTIM);
    }

    @FXML
    private void handleLoginAsUser() {
        roleSelector.getSelectionModel().select(ROLE_VICTIM);
        if (!validateCredentials(false)) {
            return;
        }

        String email = emailField.getText().trim();
        if (authService.loginAsUser(email)) {
            clearFeedback();
            loadDashboard("UserDashboard");
        } else {
            showFeedback("We cannot locate that victim record. Please verify the email or submit a new report.");
        }
    }

    @FXML
    private void handleLoginAsAdmin() {
        roleSelector.getSelectionModel().select(ROLE_ADMIN);
        if (!validateCredentials(true)) {
            return;
        }

        String email = emailField.getText().trim();
        if (authService.loginAsAdmin(email)) {
            clearFeedback();
            loadDashboard("AdminDashboard");
        } else {
            showFeedback("Administrator access denied. Confirm your operational email and passphrase.");
        }
    }

    @FXML
    private void handleReportAttack() {
        openViewInNewStage("/fxml/VictimPortalView.fxml",
                "Report a Malicious Attack",
                1100,
                760);
    }

    @FXML
    private void handleIntelBrief() {
        openExternalLink("https://www.cisa.gov/stopransomware");
    }

    @FXML
    private void handleContactSupport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cyber Desk Support");
        alert.setHeaderText("Need assistance with a report?");
        alert.setContentText("""
                Reach our cyber response desk:
                  • Email: cyberdesk@agency.gov
                  • Hotline: +1-800-555-CYBR (24/7)

                Include your incident ID for faster triage.""");
        alert.showAndWait();
    }

    private boolean validateCredentials(boolean enforcePassphrase) {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (!ValidationUtils.isValidEmail(email)) {
            showFeedback("Enter a valid email address (example: analyst@agency.gov).");
            return false;
        }

        if (enforcePassphrase) {
            String passphrase = passwordField.getText() != null ? passwordField.getText().trim() : "";
            if (passphrase.length() < 8) {
                showFeedback("Administrator logins require an 8+ character passphrase.");
                return false;
            }
        }

        clearFeedback();
        return true;
    }

    private void updateButtonStates(String role) {
        boolean isAdmin = ROLE_ADMIN.equals(role);
        passwordField.setPromptText(isAdmin ? "Enter your admin passphrase" : "Passphrase (optional)");
        adminLoginButton.setDefaultButton(isAdmin);
        victimLoginButton.setDefaultButton(!isAdmin);
    }

    private void loadDashboard(String dashboardName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + dashboardName + ".fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Cybersecurity Incident Reporting System - " +
                    (SessionManager.getInstance().isAdmin() ? "Administrator Command Center" : "Victim Reporting Portal"));
            stage.setResizable(true);
        } catch (Exception e) {
            showAlert("Error", "Failed to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openViewInNewStage(String resourcePath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Unable to open view: " + e.getMessage());
        }
    }

    private void openExternalLink(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showAlert("Notice", "External browsing is not supported on this device.");
            }
        } catch (Exception e) {
            showAlert("Notice", "Unable to open the external guidance page.");
        }
    }

    private void showFeedback(String message) {
        if (feedbackLabel != null) {
            feedbackLabel.setText(message);
            feedbackLabel.setVisible(true);
        } else {
            showAlert("Notice", message);
        }
    }

    private void clearFeedback() {
        if (feedbackLabel != null) {
            feedbackLabel.setText("");
            feedbackLabel.setVisible(false);
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
