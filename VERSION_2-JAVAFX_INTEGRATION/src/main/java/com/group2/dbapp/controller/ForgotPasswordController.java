package main.java.com.group2.dbapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.java.com.group2.dbapp.service.AuthenticationService;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Button resetButton;

    private AuthenticationService authService;

    @FXML
    public void initialize() {
        authService = new AuthenticationService();
        System.out.println("ForgotPasswordController initialized");
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your email address");
            return;
        }

        boolean success = authService.sendPasswordReset(email); // You need to implement this

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Password Reset",
                    "A password reset link has been sent to " + email);
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No account found with the email " + email);
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
        Stage stage = (Stage) resetButton.getScene().getWindow();
        stage.close();
    }
}
