package controller;

import controller.report.PendingReportsReviewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Administrator;

import java.io.IOException;

/**
 * Admin Dashboard Controller
 * Main hub for administrators with 4 report tabs
 */
public class AdminDashboardController {

    @FXML private Label adminNameLabel;
    @FXML private TabPane tabPane;
    @FXML private BorderPane rootPane;

    private Administrator currentAdmin;
    private PendingReportsReviewController pendingReportsReviewController;

    @FXML
    private void initialize() {
        System.out.println("AdminDashboardController initialized");
        
        // Listen for tab changes to refresh pending reports when tab is selected
        if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null && "Pending Reports Review".equals(newTab.getText())) {
                    // Refresh when this tab is selected
                    Platform.runLater(() -> {
                        // Find the controller if not already found
                        if (pendingReportsReviewController == null) {
                            findAndSetPendingReportsReviewController();
                        }
                        // Always refresh data when tab is selected
                        if (pendingReportsReviewController != null) {
                            pendingReportsReviewController.refreshPendingReports();
                            pendingReportsReviewController.refreshPendingEvidence();
                        } else {
                            System.err.println("AdminDashboardController: Cannot refresh - controller not found");
                        }
                    });
                }
            });
        }
        
        // Try to find the controller after scene is ready
        Platform.runLater(() -> {
            if (currentAdmin != null) {
                findAndSetPendingReportsReviewController();
            }
        });
    }

    /**
     * Set the currently logged-in administrator
     * Called from AdminLoginController after successful login
     */
    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
        if (admin != null) {
            adminNameLabel.setText("Admin: " + admin.getName());
            System.out.println("Admin set in dashboard: " + admin.getName());
            
            // Find and set admin in pending reports review controller
            // Use Platform.runLater to ensure scene is fully initialized
            Platform.runLater(() -> {
                findAndSetPendingReportsReviewController();
            });
        }
    }
    
    /**
     * Find the PendingReportsReviewController from the scene and set admin
     */
    private void findAndSetPendingReportsReviewController() {
        if (currentAdmin == null) {
            System.out.println("AdminDashboardController: currentAdmin is null, cannot set controller");
            return;
        }
        
        try {
            // Use JavaFX lookup with fx:id from the FXML
            if (rootPane != null && rootPane.getScene() != null && tabPane != null) {
                // Search in tab content
                for (Tab tab : tabPane.getTabs()) {
                    if ("Pending Reports Review".equals(tab.getText())) {
                        System.out.println("AdminDashboardController: Found 'Pending Reports Review' tab");
                        if (tab.getContent() != null && tab.getContent() instanceof Parent) {
                            Parent tabContent = (Parent) tab.getContent();
                            System.out.println("AdminDashboardController: Tab content is Parent");
                            
                            // The controller should be accessible via user data
                            Object userData = tabContent.getProperties().get("controller");
                            if (userData instanceof PendingReportsReviewController) {
                                System.out.println("AdminDashboardController: Found controller via userData");
                                pendingReportsReviewController = (PendingReportsReviewController) userData;
                                pendingReportsReviewController.setCurrentAdmin(currentAdmin);
                                return;
                            }
                            
                            // Try finding by traversing
                            pendingReportsReviewController = findControllerInParent(tabContent);
                            if (pendingReportsReviewController != null) {
                                System.out.println("AdminDashboardController: Found controller via traversal");
                                pendingReportsReviewController.setCurrentAdmin(currentAdmin);
                                return;
                            }
                            
                            System.out.println("AdminDashboardController: Controller not found in tab content");
                        } else {
                            System.out.println("AdminDashboardController: Tab content is null or not Parent");
                        }
                    }
                }
            } else {
                System.out.println("AdminDashboardController: rootPane, scene, or tabPane is null");
            }
        } catch (Exception e) {
            System.err.println("Could not find PendingReportsReviewController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Recursively search for PendingReportsReviewController in parent and its children
     */
    private PendingReportsReviewController findControllerInParent(Parent parent) {
        if (parent == null) return null;
        
        // Check user data properties
        Object controller = parent.getProperties().get("controller");
        if (controller instanceof PendingReportsReviewController) {
            return (PendingReportsReviewController) controller;
        }
        
        // Recursively check children
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                PendingReportsReviewController found = findControllerInParent((Parent) child);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }

    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/login uis/LogIn.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            stage.centerOnScreen();
            stage.show();

            showAlert(Alert.AlertType.INFORMATION, "Logged Out", "You have been logged out successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Show information alert
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    /**
     * Show error alert
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    /**
     * Get the current admin (for child controllers if needed)
     */
    public Administrator getCurrentAdmin() {
        return currentAdmin;
    }
}