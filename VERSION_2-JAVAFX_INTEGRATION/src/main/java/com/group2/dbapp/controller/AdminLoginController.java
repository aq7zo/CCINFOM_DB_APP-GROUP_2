package com.group2.dbapp.controller;

import com.group2.dbapp.model.Administrator;
import com.group2.dbapp.service.AdminAuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the **Administrator** login modal.
 */
public class AdminLoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;

    private final AdminAuthenticationService adminAuthService = new AdminAuthenticationService();

    @FXML private void initialize() {
        // optional: set default focus, etc.
        System.out.println("AdminLoginController initialized");
    }

    @FXML
    private void handleAdminLoginButton() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                    "Please enter both email and password.");
            return;
        }

        try {
            Administrator admin = adminAuthService.authenticate(email, password);
            if (admin != null) {
                showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                        "Welcome, " + admin.getName() + "!");
                loadAdminDashboard();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "Invalid admin email or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Login error: " + e.getMessage());
        }
    }

    /** Open the Admin Dashboard (replace path when you create the FXML) */
    private void loadAdminDashboard() {
        try {
            // <-- UPDATE THIS PATH when you add admin_dashboard.fxml -->
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/SceneBuilder/AdminDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 650));
            stage.setTitle("PhishNet – Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not load the admin dashboard.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Close the modal (used by "Back to User Login" button in FXML) */
    @FXML
    private void handleBackToUserLogin() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
}