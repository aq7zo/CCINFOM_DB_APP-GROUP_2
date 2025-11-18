// No package - default package
import util.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application class
 * Entry point for the Cybersecurity Incident Reporting System
 */
public class Main extends Application {

    private static final String APP_ICON_PATH = "/SceneBuilder/assets/ccinfom phishnet logo no name.png";

    @Override
    public void start(Stage primaryStage) {
        try {
            // Ensure application exits when window is closed
            Platform.setImplicitExit(true);
            
            // When the user clicks the X button, close everything properly
            primaryStage.setOnCloseRequest(e -> {
                stop();
                Platform.exit();
                System.exit(0);
            });
            
            // Test database connection
            if (!DatabaseConnection.testConnection()) {
                showErrorAndExit("Database Connection Failed",
                        "Could not connect to the database.\n" +
                                "Please ensure MySQL is running and the database is configured correctly.");
                return;
            }

            System.out.println("Database connection successful!");

            // Load the Victim Login FXML (LogIn.fxml)
            // Use ClassLoader to get resource (works better with default package)
            // Note: No leading slash when using ClassLoader
            java.net.URL fxmlUrl = Main.class.getClassLoader().getResource("SceneBuilder/login uis/LogIn.fxml");
            if (fxmlUrl == null) {
                // Try alternative path with leading slash
                fxmlUrl = Main.class.getClassLoader().getResource("/SceneBuilder/login uis/LogIn.fxml");
            }
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found. Searched for:\n" +
                        "- SceneBuilder/login uis/LogIn.fxml\n" +
                        "- /SceneBuilder/login uis/LogIn.fxml\n" +
                        "Make sure file exists in src/resources/ and run 'mvn clean compile'");
            }
            System.out.println("Loading FXML from: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Set up the stage
            primaryStage.setTitle("PhishNet - Cybersecurity Incident Reporting System");
            setApplicationIcon(primaryStage);
            primaryStage.setScene(new Scene(root, 600, 450));
            primaryStage.setResizable(false);
            primaryStage.show();

            System.out.println("Victim login screen loaded successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            String errorMsg = "Failed to load LogIn.fxml.\n\n";
            errorMsg += "Expected path: /SceneBuilder/login uis/LogIn.fxml\n";
            errorMsg += "Check that:\n";
            errorMsg += "1. File exists in src/resources/SceneBuilder/login uis/\n";
            errorMsg += "2. Resources are copied to target/classes/\n";
            errorMsg += "3. Run 'mvn clean compile' to rebuild\n\n";
            errorMsg += "Error: " + e.getMessage();
            showErrorAndExit("Application Error", errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndExit("Unexpected Error",
                    "An unexpected error occurred:\n" + e.getMessage());
        }
    }

    /**
     * Load and assign the PhishNet logo as the application icon.
     */
    private void setApplicationIcon(Stage stage) {
        java.net.URL iconUrl = Main.class.getResource(APP_ICON_PATH);
        if (iconUrl == null) {
            System.err.println("Warning: Application icon not found at " + APP_ICON_PATH);
            return;
        }

        Image appIcon = new Image(iconUrl.toExternalForm());
        stage.getIcons().add(appIcon);
    }

    @Override
    public void stop() {
        // Clean up database connection
        DatabaseConnection.closeConnection();
        System.out.println("Application closed. Database connection terminated.");
    }

    /**
     * Show error dialog and exit
     */
    private void showErrorAndExit(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }

    /**
     * Main method - entry point
     */
    public static void main(String[] args) {
        launch(args);
    }
}

