package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import model.Victim;

import java.io.IOException;

/**
 * Victim Dashboard Controller
 * Loads 3 tabs: Submit Report, Upload Evidence, View Reports
 */
public class VictimDashboardController {

    @FXML private Label victimNameLabel;
    @FXML private TabPane tabPane;
    @FXML private Parent reportIncidentTab;
    @FXML private Parent uploadEvidenceTab;
    @FXML private Parent viewMyReportsTab;

    private Victim currentVictim;

    @FXML
    private void initialize() {
        // Loaded automatically
    }

    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        victimNameLabel.setText("Victim: " + victim.getName());
        
        // Pass victim to included controllers after they're loaded
        // Use Platform.runLater to ensure FXML includes are fully loaded
        Platform.runLater(() -> {
            try {
                // Access controllers through FXMLLoader namespace
                if (reportIncidentTab != null) {
                    FXMLLoader loader = (FXMLLoader) reportIncidentTab.getProperties().get(FXMLLoader.class);
                    if (loader != null) {
                        Object controller = loader.getController();
                        if (controller instanceof ReportIncidentController) {
                            ((ReportIncidentController) controller).setCurrentVictim(victim);
                        }
                    }
                }
                
                if (viewMyReportsTab != null) {
                    FXMLLoader loader = (FXMLLoader) viewMyReportsTab.getProperties().get(FXMLLoader.class);
                    if (loader != null) {
                        Object controller = loader.getController();
                        if (controller instanceof ViewMyReportsController) {
                            ((ViewMyReportsController) controller).setCurrentVictim(victim);
                        }
                    }
                }
            } catch (Exception e) {
                // Fallback: try to find controllers by traversing the scene
                findAndSetControllers(victim);
            }
        });
    }
    
    private void findAndSetControllers(Victim victim) {
        // Traverse scene graph to find controllers
        if (tabPane != null && tabPane.getScene() != null) {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getContent() != null) {
                    // Try to get controller from the content's user data or properties
                    Object controller = tab.getContent().getUserData();
                    if (controller == null) {
                        // Check if there's a way to get the controller
                        // This is a fallback - may not always work
                    }
                    if (controller instanceof ReportIncidentController) {
                        ((ReportIncidentController) controller).setCurrentVictim(victim);
                    } else if (controller instanceof ViewMyReportsController) {
                        ((ViewMyReportsController) controller).setCurrentVictim(victim);
                    }
                }
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/login uis/LogIn.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) victimNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            stage.centerOnScreen();
            stage.show();

            showAlert("Logged out successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Logout failed: " + e.getMessage() + "\n\nPlease check console for details.");
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }
}