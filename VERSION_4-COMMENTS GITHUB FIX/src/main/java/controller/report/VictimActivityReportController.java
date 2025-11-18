package controller.report;

import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.VictimDAO;
import dao.VictimDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Controller for the "Victim Activity" report.
 * Displays victims who have more than 3 incidents in a selected month.
 */
public class VictimActivityReportController {

    @FXML private ComboBox<Integer> yearCombo; // Dropdown for selecting year
    @FXML private ComboBox<Integer> monthCombo; // Dropdown for selecting month
    @FXML private TableView<VictimActivity> table; // Table showing report results
    @FXML private TableColumn<VictimActivity, String> nameCol, emailCol; // Table columns for name and email
    @FXML private TableColumn<VictimActivity, Integer> countCol; // Table column for incident count
    @FXML private Button generateButton; // Button to generate report
    @FXML private Button exportButton; // Button to export report to CSV

    // DAO instances for accessing database
    private final VictimDAO victimDAO = new VictimDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();

    /**
     * Initializes the controller.
     * Sets up year/month combos, table columns, and validates initial state.
     */
    @FXML
    private void initialize() {
        int year = java.time.Year.now().getValue();

        // Populate year combo with recent years including current
        yearCombo.setItems(FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, year));
        yearCombo.setValue(year);

        // Populate month combo with 1-12
        monthCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthCombo.setValue(java.time.LocalDate.now().getMonthValue());

        // Setup table columns to use VictimActivity properties
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        emailCol.setCellValueFactory(d -> d.getValue().emailProperty());
        countCol.setCellValueFactory(d -> d.getValue().countProperty().asObject());

        validate(); // Enable/disable buttons based on combo selection
    }

    /**
     * Validates if generate button should be enabled.
     * Disables the button if either year or month is not selected.
     */
    private void validate() {
        generateButton.setDisable(yearCombo.getValue() == null || monthCombo.getValue() == null);
    }

    /**
     * Handles the "Generate Report" button click.
     * Queries database for victims with >3 incidents in the selected month and displays results.
     */
    @FXML
    private void handleGenerate() {
        int year = yearCombo.getValue();
        int month = monthCombo.getValue();

        try {
            // SQL query to fetch victims with >3 incidents in a month
            String sql = """
                SELECT v.Name, v.ContactEmail, COUNT(*) as cnt
                FROM IncidentReports i
                JOIN Victims v ON i.VictimID = v.VictimID
                WHERE YEAR(i.DateReported) = ? AND MONTH(i.DateReported) = ?
                GROUP BY v.VictimID
                HAVING cnt > 3
                ORDER BY cnt DESC
                """;

            System.out.println("VictimActivityReportController: Generating report for " + year + "-" + month);
            System.out.println("VictimActivityReportController: Executing SQL: " + sql);

            List<VictimActivity> list = new ArrayList<>();
            
            // Execute query using database connection
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);
                stmt.setInt(2, month);
                var rs = stmt.executeQuery();

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    // Add each row to the list as VictimActivity object
                    list.add(new VictimActivity(
                            rs.getString("Name"),
                            rs.getString("ContactEmail"),
                            rs.getInt("cnt")
                    ));
                }
                System.out.println("VictimActivityReportController: Query returned " + rowCount + " rows");
            }

            // Display results in table
            table.setItems(FXCollections.observableArrayList(list));
            exportButton.setDisable(list.isEmpty()); // Disable export if no results
            
            if (list.isEmpty()) {
                showAlert("No high-risk victims found for " + year + "-" + String.format("%02d", month) + 
                        ". (Victims with >3 incidents in the selected month)\n" +
                        "Try selecting a different year/month or check if data exists in the database.");
            } else {
                showAlert(list.size() + " high-risk victims found.");
            }

        } catch (Exception e) {
            System.err.println("VictimActivityReportController: Error generating report: " + e.getMessage());
            e.printStackTrace();
            showError("Failed: " + e.getMessage());
        }
    }

    /**
     * Handles the "Export" button click.
     * Exports current table data to a CSV file.
     */
    @FXML
    private void handleExport() {
        File file = new FileChooser().showSaveDialog(null);
        if (file == null) return; // User cancelled

        try (PrintWriter pw = new PrintWriter(file)) {
            // Write CSV header
            pw.println("Name,Email,IncidentCount");
            // Write each table row
            for (var v : table.getItems()) {
                pw.println(v.getName() + "," + v.getEmail() + "," + v.getCount());
            }
            showAlert("Exported"); // Notify success
        } catch (Exception e) {
            showError("Export failed");
        }
    }

    /**
     * Shows an information alert.
     * @param msg Message to display
     */
    private void showAlert(String msg) { 
        new Alert(Alert.AlertType.INFORMATION, msg).show(); 
    }

    /**
     * Shows an error alert.
     * @param msg Error message to display
     */
    private void showError(String msg) { 
        new Alert(Alert.AlertType.ERROR, msg).show(); 
    }

    /**
     * Model class representing a victim's activity for the report.
     */
    public static class VictimActivity {
        private final String name, email;
        private final int count;

        /**
         * Constructor
         * @param n Victim name
         * @param e Victim email
         * @param c Incident count
         */
        public VictimActivity(String n, String e, int c) { 
            name = n; 
            email = e; 
            count = c; 
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getCount() { return count; }

        // JavaFX properties for table binding
        public javafx.beans.property.StringProperty nameProperty() { 
            return new javafx.beans.property.SimpleStringProperty(name); 
        }

        public javafx.beans.property.StringProperty emailProperty() { 
            return new javafx.beans.property.SimpleStringProperty(email); 
        }

        public javafx.beans.property.IntegerProperty countProperty() { 
            return new javafx.beans.property.SimpleIntegerProperty(count); 
        }
    }
}
