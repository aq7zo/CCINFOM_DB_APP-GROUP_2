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
 *
 * Section 5 Integration:
 * - On successful login, opens AdminDashboard with 4 reports:
 *   1. Monthly Attack Trends Report (Martin, Kurt Nehemiah Z.)
 *   2. Top Perpetrators Report (Hallare, Zach Benedict I.)
 *   3. Victim Activity Report (Campo, Benette Enzo V.)
 *   4. Incident Evidence Summary Report (Ravelo, Georgina Karylle P.)
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
                        "Welcome, " + admin.getName() + "!\nYou can now access all reports.");
                loadAdminDashboard(admin);  // Pass admin to dashboard
            } else {
                System.err.println("Login failed - invalid credentials");
                showAlert(Alert.AlertType.ERROR, "Login Failed",
                        "Invalid admin email or password.\n\n" +
                                "Please verify:\n" +
                                "- Email: admin@phishnet.com\n" +
                                "- Password: PhishNetAdmin124\n" +
                                "- Database has been populated with PhishNet-inserts.sql");
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

    /**
     * Open the Admin Dashboard with 4 Reports (Section 5)
     * Reports:
     * 1. Monthly Attack Trends Report – Martin, Kurt Nehemiah Z.
     * 2. Top Perpetrators Report – Hallare, Zach Benedict I.
     * 3. Victim Activity Report – Campo, Benette Enzo V.
     * 4. Incident Evidence Summary Report – Ravelo, Georgina Karylle P.
     */
    private void loadAdminDashboard(Administrator admin) {
        try {
            // CORRECT PATH: /login uis/AdminDashboard.fxml
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/login uis/AdminDashboard.fxml"));
            Parent root = loader.load();

            // Pass admin to dashboard controller
            AdminDashboardController dashboardCtrl = loader.getController();
            dashboardCtrl.setCurrentAdmin(admin);

            Stage stage = (Stage) loginButton1.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 750));
            stage.setTitle("PhishNet – Admin Dashboard (Section 5 Reports)");
            stage.centerOnScreen();
            stage.show();

            System.out.println("Admin Dashboard loaded for: " + admin.getName());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not load the admin dashboard.\n\n" +
                            "Check:\n" +
                            "- FXML file exists at /login uis/AdminDashboard.fxml\n" +
                            "- Report FXMLs in /login uis/report/\n" +
                            "- All controllers are in controller.report package");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login uis/SignUp.fxml"));
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

    /** Navigate back to User Login */
    @FXML
    private void handleAdminLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login uis/LogIn.fxml"));
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