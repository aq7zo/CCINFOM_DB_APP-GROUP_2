package controller.report;

import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.PerpetratorDAO;
import dao.PerpetratorDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Controller for generating the Top Perpetrators report.
 * Handles selection of year/month, generation of the report,
 * displaying in TableView, and exporting to CSV.
 */
public class TopPerpetratorsReportController {

    @FXML private ComboBox<Integer> yearCombo;      // Dropdown for selecting year
    @FXML private ComboBox<Integer> monthCombo;     // Dropdown for selecting month
    @FXML private TableView<TopPerp> table;         // Table for displaying top perpetrators
    @FXML private TableColumn<TopPerp, String> idCol;   // Column for identifier
    @FXML private TableColumn<TopPerp, String> typeCol; // Column for identifier type
    @FXML private TableColumn<TopPerp, String> nameCol; // Column for associated name
    @FXML private TableColumn<TopPerp, Number> attacksCol; // Column for incident count
    @FXML private Button generateButton;            // Button to generate report
    @FXML private Button exportButton;              // Button to export report to CSV

    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl(); // DAO for incident reports
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();           // DAO for perpetrators

    /**
     * Initializes the controller after FXML is loaded.
     * Populates year/month ComboBoxes and sets up TableView columns.
     */
    @FXML
    private void initialize() {
        int year = java.time.Year.now().getValue();

        // Populate year ComboBox with last few years + current year
        yearCombo.setItems(FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, year));
        yearCombo.setValue(year);

        // Populate month ComboBox with all 12 months
        monthCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthCombo.setValue(java.time.Month.from(java.time.LocalDate.now()).getValue());

        // Set up TableView columns to bind to TopPerp properties
        idCol.setCellValueFactory(d -> d.getValue().identifierProperty());
        typeCol.setCellValueFactory(d -> d.getValue().typeProperty());
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        attacksCol.setCellValueFactory(d -> d.getValue().countProperty());

        // Initial validation to enable/disable generate button
        validate();
    }

    /**
     * Validates the input selections and enables/disables generate button.
     */
    private void validate() {
        generateButton.setDisable(yearCombo.getValue() == null || monthCombo.getValue() == null);
    }

    /**
     * Handles generation of the Top Perpetrators report.
     * Queries the database and populates the TableView.
     */
    @FXML
    private void handleGenerate() {
        int year = yearCombo.getValue();
        int month = monthCombo.getValue();

        try {
            // SQL query to fetch top 10 perpetrators for the selected year/month
            String sql = """
                SELECT p.Identifier, p.IdentifierType, p.AssociatedName, COUNT(*) as cnt
                FROM IncidentReports i
                JOIN Perpetrators p ON i.PerpetratorID = p.PerpetratorID
                WHERE YEAR(i.DateReported) = ? AND MONTH(i.DateReported) = ?
                GROUP BY p.PerpetratorID
                ORDER BY cnt DESC LIMIT 10
                """;

            System.out.println("TopPerpetratorsReportController: Generating report for " + year + "-" + month);
            System.out.println("TopPerpetratorsReportController: Executing SQL: " + sql);

            List<TopPerp> list = new ArrayList<>();
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);
                stmt.setInt(2, month);
                var rs = stmt.executeQuery();

                int rowCount = 0;
                // Iterate through query results and add to list
                while (rs.next()) {
                    rowCount++;
                    list.add(new TopPerp(
                            rs.getString("Identifier"),
                            rs.getString("IdentifierType"),
                            rs.getString("AssociatedName"),
                            rs.getInt("cnt")
                    ));
                }
                System.out.println("TopPerpetratorsReportController: Query returned " + rowCount + " rows");
            }

            // Populate TableView with results
            table.setItems(FXCollections.observableArrayList(list));

            // Enable/disable export button based on whether data exists
            exportButton.setDisable(list.isEmpty());
            
            if (list.isEmpty()) {
                showAlert("No perpetrators found for " + year + "-" + String.format("%02d", month) + 
                        ". Try selecting a different year/month or check if data exists in the database.");
            } else {
                showAlert("Top " + list.size() + " Perpetrators loaded for " + year + "-" + String.format("%02d", month));
            }

        } catch (Exception e) {
            System.err.println("TopPerpetratorsReportController: Error generating report: " + e.getMessage());
            e.printStackTrace();
            showError("Failed: " + e.getMessage());
        }
    }

    /**
     * Handles exporting the TableView data to a CSV file.
     */
    @FXML
    private void handleExport() {
        File file = new FileChooser().showSaveDialog(null); // Open save file dialog
        if (file == null) return; // Cancelled by user

        try (PrintWriter pw = new PrintWriter(file)) {
            // Write CSV header
            pw.println("Identifier,Type,Name,IncidentCount");

            // Write each row from the TableView
            for (var row : table.getItems()) {
                pw.println(row.getIdentifier() + "," + row.getType() + "," + row.getName() + "," + row.getCount());
            }
            showAlert("Exported to " + file.getName());
        } catch (Exception e) {
            showError("Export failed");
        }
    }

    /**
     * Shows an information alert with a message.
     * @param msg the message to display
     */
    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }

    /**
     * Shows an error alert with a message.
     * @param msg the message to display
     */
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    /**
     * Represents a row in the Top Perpetrators TableView.
     */
    public static class TopPerp {
        private final SimpleStringProperty identifier; // Identifier of perpetrator
        private final SimpleStringProperty type;       // Type of identifier
        private final SimpleStringProperty name;       // Associated name
        private final SimpleIntegerProperty count;     // Number of incidents

        /**
         * Constructor for TopPerp.
         * @param i Identifier
         * @param t Identifier Type
         * @param n Associated Name
         * @param c Incident Count
         */
        public TopPerp(String i, String t, String n, int c) {
            this.identifier = new SimpleStringProperty(i);
            this.type = new SimpleStringProperty(t);
            this.name = new SimpleStringProperty(n == null ? "" : n);
            this.count = new SimpleIntegerProperty(c);
        }

        public String getIdentifier() { return identifier.get(); }
        public String getType() { return type.get(); }
        public String getName() { return name.get(); }
        public int getCount() { return count.get(); }

        public SimpleStringProperty identifierProperty() { return identifier; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty nameProperty() { return name; }
        public ObservableValue<Number> countProperty() { return count; }
    }
}
