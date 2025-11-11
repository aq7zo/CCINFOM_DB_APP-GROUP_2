package main.java.com.group2.dbapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import main.java.com.group2.dbapp.model.Administrator;
import main.java.com.group2.dbapp.service.AuthenticationService;

/**
 * Controller for the Login page
 * Handles user authentication and login functionality
 */
public class LoginController {
    
    @FXML
    private PasswordField emailField;  // Note: Named as PasswordField in FXML but used for email
    
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
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        authService = new AuthenticationService();
        System.out.println("LoginController initialized");
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validate inputs
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your email address");
            return;
        }
        
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your password");
            return;
        }
        
        // Attempt login
        Administrator admin = authService.login(email, password);
        
        if (admin != null) {
            currentUser = admin;
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", 
                "Welcome, " + admin.getName() + "!\nRole: " + admin.getRole());
            
            // TODO: Navigate to main dashboard
            // For now, just close the login window
            closeWindow();
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
        showAlert(Alert.AlertType.INFORMATION, "Sign Up", 
            "Sign up functionality will be implemented in the next phase.\n" +
            "Please contact the system administrator to create an account.");
    }
    
    /**
     * Handle forgot password button click
     */
    @FXML
    private void handleForgotPassword() {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password", 
            "Password recovery functionality will be implemented in the next phase.\n" +
            "Please contact the system administrator to reset your password.");
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Close the login window
     */
    private void closeWindow() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Get the currently logged in user
     */
    public Administrator getCurrentUser() {
        return currentUser;
    }
}

