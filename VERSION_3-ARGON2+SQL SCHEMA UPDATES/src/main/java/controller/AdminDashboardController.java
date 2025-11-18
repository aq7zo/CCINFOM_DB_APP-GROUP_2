package controller;

import controller.report.PendingReportsReviewController;
import controller.report.EvaluateReportsController;
import controller.report.AdminCreateReportController;
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
 * Main hub for administrators with report tabs
 */
public class AdminDashboardController {

    @FXML private Label adminNameLabel;
    @FXML private TabPane tabPane;
    @FXML private PendingReportsReviewController pendingReportsReviewController;
    @FXML private EvaluateReportsController evaluateReportsController;
    @FXML private AdminCreateReportController adminCreateReportController;

    private Administrator currentAdmin; // Currently logged-in administrator

    /**
     * Initializes the dashboard controller.
     * Sets up tab selection listener and ensures nested controllers are ready.
     */
    @FXML
    private void initialize() {
        System.out.println("AdminDashboardController initialized");
        
        // Listen for tab changes to refresh data when tabs are selected
        if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    if ("Pending Reports Review".equals(newTab.getText())) {
                        // Refresh when this tab is selected
                        Platform.runLater(() -> {
                            refreshPendingReviewData();
                        });
                    } else if ("Evaluate Reports".equals(newTab.getText())) {
                        // Refresh when Evaluate Reports tab is selected
                        Platform.runLater(() -> {
                            refreshEvaluateReportsData();
                        });
                    } else if ("Manual Report".equals(newTab.getText())) {
                        // Setup admin for Manual Report tab
                        Platform.runLater(() -> {
                            pushAdminToCreateReportController();
                        });
                    }
                }
            });
        }
        
        // Try to find the controller after scene is ready
        Platform.runLater(() -> {
            pushAdminToPendingController();
            pushAdminToEvaluateController();
            pushAdminToCreateReportController();
        });
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
            
            // Find and set admin in child controllers
            // Use Platform.runLater to ensure scene is fully initialized
            Platform.runLater(() -> {
                pushAdminToPendingController();
                pushAdminToEvaluateController();
                pushAdminToCreateReportController();
            });
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
    
    private void pushAdminToEvaluateController() {
        if (evaluateReportsController != null && currentAdmin != null) {
            evaluateReportsController.setCurrentAdmin(currentAdmin);
        } else if (evaluateReportsController == null) {
            System.out.println("AdminDashboardController: EvaluateReportsController not yet initialized");
        }
    }
    
    private void pushAdminToCreateReportController() {
        if (adminCreateReportController != null && currentAdmin != null) {
            adminCreateReportController.setCurrentAdmin(currentAdmin);
        } else if (adminCreateReportController == null) {
            System.out.println("AdminDashboardController: AdminCreateReportController not yet initialized");
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
    
    private void refreshEvaluateReportsData() {
        if (evaluateReportsController != null) {
            evaluateReportsController.refreshReports();
        } else {
            System.err.println("AdminDashboardController: Cannot refresh evaluate reports - controller unavailable");
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
