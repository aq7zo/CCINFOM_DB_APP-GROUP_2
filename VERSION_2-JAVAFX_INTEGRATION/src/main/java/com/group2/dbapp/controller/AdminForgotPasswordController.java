package com.group2.dbapp.controller;

import com.group2.dbapp.service.AdminAuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the **Admin** password‑reset modal.
 * (Uses AdminAuthenticationService – it can reuse the same reset logic.)
 */
public class AdminForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button    resetButton;

    private final AdminAuthenticationService adminService = new AdminAuthenticationService();

    @FXML private void initialize() {
        System.out.println("AdminForgotPasswordController initialized");
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please enter the admin email address");
            return;
        }

        // The service does not have a reset method yet – stub it.
        // For now we just pretend it works.
        boolean success = adminService.sendPasswordReset(email);   // <-- implement in service

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Password Reset",
                    "A temporary password has been sent to " + email +
                            "\nCheck the console output.");
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No administrator account found with email: " + email);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) resetButton.getScene().getWindow();
        stage.close();
    }
}