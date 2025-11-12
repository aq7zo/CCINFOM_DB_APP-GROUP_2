package com.group2.dbapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.group2.dbapp.model.Victim;
import com.group2.dbapp.service.VictimAuthenticationService;

import java.io.IOException;

/**
 * Controller for the Login page
 * Handles victim authentication and login functionality
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Hyperlink adminLoginLink;

    private VictimAuthenticationService authService;
    private Victim currentVictim;

    @FXML
    public void initialize() {
        authService = new VictimAuthenticationService();
        System.out.println("LoginController initialized");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your email address");
            return;
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your password");
            return;
        }

        Victim victim = authService.login(email, password);

        if (victim != null) {
            currentVictim = victim;
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Welcome, " + victim.getName() + "!\nYou can now report incidents.");

            // TODO: Navigate to victim dashboard
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid email or password. Please try again.");
            passwordField.clear();
        }
    }

    @FXML
    private void handleSignUp() {
        navigateToScene("/SceneBuilder/login uis/SignUp.fxml", "Create Account");
    }

    @FXML
    private void handleAdminLogin() {
        navigateToScene("/SceneBuilder/login uis/AdminLogin.fxml", "Administrator Login");
    }

    private void navigateToScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to navigate to: " + title + "\nError: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }

    public Victim getCurrentVictim() {
        return currentVictim;
    }
}