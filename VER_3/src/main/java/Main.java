import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.DatabaseConnection;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1️⃣ Check database connection first
        try (var conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                showInfoAlert("Database Status", "✓ Database connection established successfully.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Connection Failed",
                "Please ensure:\n" +
                "1. MySQL server is running\n" +
                "2. Database 'CybersecurityDB' exists\n" +
                "3. Credentials in DatabaseConnection.java are correct\n\n" +
                "Error: " + e.getMessage());
            return;
        }

        // 2️⃣ Load the GUI main menu instead of ConsoleMenu
        Scene scene = new Scene(new MainMenuView().getView(), 800, 600);
        primaryStage.setTitle("Cybersecurity Incident Reporting System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
