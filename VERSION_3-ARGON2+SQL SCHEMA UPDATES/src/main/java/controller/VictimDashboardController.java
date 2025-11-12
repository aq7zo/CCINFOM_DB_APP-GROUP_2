package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Victim;

import java.io.IOException;

/**
 * Victim Dashboard Controller
 * Loads 3 tabs: Submit Report, Upload Evidence, View Reports
 */
public class VictimDashboardController {

    @FXML private Label victimNameLabel;

    private Victim currentVictim;

    @FXML
    private void initialize() {
        // Loaded automatically
    }

    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        victimNameLabel.setText("Victim: " + victim.getName());
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login uis/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) victimNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("PhishNet - Login");
            stage.centerOnScreen();
            stage.show();

            showAlert("Logged out successfully.");
        } catch (IOException e) {
            showError("Logout failed: " + e.getMessage());
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