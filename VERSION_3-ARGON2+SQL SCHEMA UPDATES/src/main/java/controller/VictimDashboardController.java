package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import model.Victim;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Victim Dashboard Controller
 * Loads 3 tabs: Submit Report, Upload Evidence, View Reports
 */
public class VictimDashboardController {

    @FXML private Label victimNameLabel;
    @FXML private TabPane tabPane;
    @FXML private ReportIncidentController reportIncidentTabController;
    @FXML private UploadEvidenceController uploadEvidenceTabController;
    @FXML private ViewMyReportsController viewMyReportsTabController;

    private Victim currentVictim;

    @FXML
    private void initialize() {
        // Loaded automatically
    }

    public Victim getCurrentVictim() {
        return currentVictim;
    }

    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        victimNameLabel.setText("Victim: " + victim.getName());

        if (reportIncidentTabController != null) {
            reportIncidentTabController.setCurrentVictim(victim);
            reportIncidentTabController.setIncidentCreatedCallback(incident -> {
                if (viewMyReportsTabController != null) {
                    viewMyReportsTabController.refreshReports();
                }
                if (uploadEvidenceTabController != null) {
                    uploadEvidenceTabController.refreshIncidentList();
                    uploadEvidenceTabController.selectIncident(incident);
                }
            });
        }

        if (viewMyReportsTabController != null) {
            viewMyReportsTabController.setCurrentVictim(victim);
            viewMyReportsTabController.refreshReports();
        }

        if (uploadEvidenceTabController != null) {
            uploadEvidenceTabController.setCurrentVictim(victim);
            uploadEvidenceTabController.refreshIncidentList();
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