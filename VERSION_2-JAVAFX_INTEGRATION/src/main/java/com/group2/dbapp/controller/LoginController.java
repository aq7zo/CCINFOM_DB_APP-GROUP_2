package main.java.com.group2.dbapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.com.group2.dbapp.model.Administrator;
import main.java.com.group2.dbapp.service.AuthenticationService;

import java.io.IOException;

/**
 * Controller for the Login page
 * Handles user authentication and login functionality
 */
public class LoginController {

    @FXML
    private TextField emailField;  // ✅ Use TextField, not PasswordField

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Button forgotPasswordButton;

    private AuthenticationService authService;
    private Administrator currentUser;

    @FXML
    public void initialize() {
        authService = new AuthenticationService();
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

        Administrator admin = authService.login(email, password);

        if (admin != null) {
            currentUser = admin;
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Welcome, " + admin.getName() + "!\nRole: " + admin.getRole());
            closeWindow(); // TODO: Navigate to main dashboard instead
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid email or password. Please try again.");
            passwordField.clear();
        }
    }

    /**
     * Handle sign up button click
     */
    @FXML
    private void handleSignUp() {
        openModalWindow("/SceneBuilder/SignUp.fxml", "Create Account");
    }

    /**
     * Handle forgot password button click
     */
    @FXML
    private void handleForgotPassword() {
        openModalWindow("/SceneBuilder/ForgotPassword.fxml", "Reset Password");
    }

    /**
     * Helper: open new modal window (SignUp / ForgotPassword)
     */
    private void openModalWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle(title);
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(loginButton.getScene().getWindow());
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to open window: " + title);
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

    public Administrator getCurrentUser() {
        return currentUser;
    }
}
