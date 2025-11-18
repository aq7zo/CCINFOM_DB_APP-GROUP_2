package controller;

import controller.report.PendingReportsReviewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import model.Administrator;

import java.io.IOException;

/**
 * Admin Dashboard Controller
 * Main hub for administrators with multiple report tabs.
 * Handles initialization, tab switching, and logout functionality.
 */
public class AdminDashboardController {

    @FXML private Label adminNameLabel; // Label to display the current admin's name
    @FXML private TabPane tabPane; // TabPane containing different report tabs
    @FXML private PendingReportsReviewController pendingReportsReviewController; // Nested controller for pending reports

    private Administrator currentAdmin; // Currently logged-in administrator

    /**
     * Initializes the dashboard controller.
     * Sets up tab selection listener and ensures nested controllers are ready.
     */
    @FXML
    private void initialize() {
        System.out.println("AdminDashboardController initialized");

        // Add listener to refresh pending reports when "Pending Reports Review" tab is selected
        if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null && "Pending Reports Review".equals(newTab.getText())) {
                    // Refresh data on JavaFX Application Thread
                    Platform.runLater(this::refreshPendingReviewData);
                }
            });
        }

        // Push current admin to nested controller after scene is fully loaded
        Platform.runLater(this::pushAdminToPendingController);
    }

    /**
     * Set the currently logged-in administrator.
     * Called from AdminLoginController after a successful login.
     *
     * @param admin The Administrator object representing the logged-in user.
     */
    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
        if (admin != null) {
            String name = admin.getName();
            // Truncate long names to 30 characters with ellipsis
            if (name != null && name.length() > 30) {
                name = name.substring(0, 27) + "...";
            }
            adminNameLabel.setText("Admin: " + (name != null ? name : "Unknown"));
            System.out.println("Admin set in dashboard: " + admin.getName());

            // Push admin to nested controller after UI is ready
            Platform.runLater(this::pushAdminToPendingController);
        }
    }

    /**
     * Push the current administrator to the PendingReportsReviewController.
     * Ensures that the nested controller receives the admin information.
     */
    private void pushAdminToPendingController() {
        if (pendingReportsReviewController != null && currentAdmin != null) {
            pendingReportsReviewController.setCurrentAdmin(currentAdmin);
        } else if (pendingReportsReviewController == null) {
            System.out.println("AdminDashboardController: PendingReportsReviewController not yet initialized");
        }
    }

    /**
     * Refresh the pending reports and evidence data in the nested controller.
     * Called when switching to the "Pending Reports Review" tab.
     */
    private void refreshPendingReviewData() {
        if (pendingReportsReviewController != null) {
            pendingReportsReviewController.refreshPendingReports();
            pendingReportsReviewController.refreshPendingEvidence();
        } else {
            System.err.println("AdminDashboardController: Cannot refresh pending reviews - controller unavailable");
        }
    }

    /**
     * Handle logout button click.
     * Loads the login scene and shows an informational alert.
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/login uis/LogIn.fxml"));
            Parent root = loader.load();

            // Switch to login scene
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            stage.centerOnScreen();
            stage.show();

            // Notify admin of successful logout
            showAlert(Alert.AlertType.INFORMATION, "Logged Out", "You have been logged out successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Show an informational or confirmation alert.
     *
     * @param type  Alert type (INFO, WARNING, etc.)
     * @param title Alert window title
     * @param msg   Message to display
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    /**
     * Show an error alert.
     *
     * @param msg Error message to display
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    /**
     * Get the current administrator.
     * Useful for child controllers that need admin context.
     *
     * @return currentAdmin The currently logged-in administrator.
     */
    public Administrator getCurrentAdmin() {
        return currentAdmin;
    }
}
