package controller;

import model.Administrator;
import service.AdminAuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the Administrator login modal.
 */
public class AdminLoginController {

    @FXML private TextField     emailField1;
    @FXML private PasswordField passwordField1;
    @FXML private Button        loginButton1;
    @FXML private Button        signUpButton;
    @FXML private Hyperlink     adminLoginLink;

    private final AdminAuthenticationService adminAuthService = new AdminAuthenticationService();

    @FXML private void initialize() {
        System.out.println("AdminLoginController initialized");
    }

    @FXML
    private void handleLogin() {
        String email    = emailField1.getText().trim();
        String password = passwordField1.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                    "Please enter both email and password.");
            return;
        }

        try {
            System.out.println("Attempting admin login for: " + email);
            Administrator admin = adminAuthService.authenticate(email, password);
            if (admin != null) {
                System.out.println("Login successful for: " + admin.getName());
                showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                        "Welcome, " + admin.getName() + "!");
                loadAdminDashboard(admin);
            } else {
                System.err.println("Login failed - admin is null");
                showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "Invalid admin email or password.");
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Cannot connect to database.\n\nError: " + e.getMessage() +
                            "\n\nPlease check:\n" +
                            "- MySQL server is running\n" +
                            "- Database connection settings in DatabaseConnection.java");
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Login error: " + e.getMessage() + "\n\nCheck console for details.");
        }
    }

    private void loadAdminDashboard(Administrator admin) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/SceneBuilder/login uis/dashboards/AdminDashboard.fxml"));
            Parent root = loader.load();

            // Get the controller and pass admin data
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass()
                            .getMethod("setCurrentAdmin", Administrator.class)
                            .invoke(controller, admin);
                } catch (Exception e) {
                    System.err.println("Warning: Could not set admin in dashboard: " + e.getMessage());
                }
            }

            Stage stage = (Stage) loginButton1.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 750));
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

    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/login uis/SignUp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton1.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("Create Account");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to navigate to sign up screen.");
        }
    }

    @FXML
    private void handleAdminLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SceneBuilder/login uis/LogIn.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton1.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 450));
            stage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to navigate to login screen.");
        }
    }
}