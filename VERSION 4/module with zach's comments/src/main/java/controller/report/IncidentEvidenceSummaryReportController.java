package controller.report;

import dao.EvidenceDAO;
import dao.EvidenceDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Controller for the Incident Evidence Summary Report GUI.
 * Allows users to generate a table of evidence submissions filtered by month and year,
 * and export the table to a CSV file.
 */
public class IncidentEvidenceSummaryReportController {

    @FXML private ComboBox<Integer> yearCombo; // Dropdown for selecting year
    @FXML private ComboBox<Integer> monthCombo; // Dropdown for selecting month
    @FXML private TableView<EvidenceSummary> table; // Table to display evidence summary
    @FXML private TableColumn<EvidenceSummary, String> typeCol, statusCol, adminCol; // Table columns for type, status, admin
    @FXML private TableColumn<EvidenceSummary, String> dateCol; // Table column for submission date
    @FXML private Button generateButton; // Button to generate report
    @FXML private Button exportButton; // Button to export table to CSV

    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl(); // DAO for evidence operations

    /**
     * Initializes the controller. Sets up year/month dropdowns, table column bindings,
     * and validates the form initially.
     */
    @FXML
    private void initialize() {
        int year = java.time.Year.now().getValue(); // current year
        yearCombo.setItems(FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, year));
        yearCombo.setValue(year); // default selected year
        monthCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthCombo.setValue(java.time.LocalDate.now().getMonthValue()); // default selected month

        // Bind table columns to EvidenceSummary properties
        typeCol.setCellValueFactory(d -> d.getValue().typeProperty());
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());
        adminCol.setCellValueFactory(d -> d.getValue().adminProperty());
        dateCol.setCellValueFactory(d -> d.getValue().dateProperty());

        validate(); // Validate the form (enable/disable generate button)
    }

    /**
     * Validates the year and month selection.
     * Disables the generate button if either is null.
     */
    private void validate() {
        generateButton.setDisable(yearCombo.getValue() == null || monthCombo.getValue() == null);
    }

    /**
     * Handles the "Generate Report" button click.
     * Queries the database for evidence submitted in the selected month and year,
     * populates the table, and enables/disables the export button.
     */
    @FXML
    private void handleGenerate() {
        int year = yearCombo.getValue();
        int month = monthCombo.getValue();

        try {
            String sql = """
                SELECT e.EvidenceType, e.VerifiedStatus, a.Name as AdminName, e.SubmissionDate
                FROM EvidenceUpload e
                LEFT JOIN Administrators a ON e.AdminID = a.AdminID
                WHERE YEAR(e.SubmissionDate) = ? AND MONTH(e.SubmissionDate) = ?
                ORDER BY e.SubmissionDate DESC
                """;

            System.out.println("IncidentEvidenceSummaryReportController: Generating report for " + year + "-" + month);
            System.out.println("IncidentEvidenceSummaryReportController: Executing SQL: " + sql);

            List<EvidenceSummary> list = new ArrayList<>();

            // Connect to DB and execute query
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year); // set year parameter
                stmt.setInt(2, month); // set month parameter
                var rs = stmt.executeQuery();

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    // Get submission date as string (limit length to 16 chars if needed)
                    String submissionDateStr = rs.getString("SubmissionDate");
                    String dateStr = (submissionDateStr != null && submissionDateStr.length() >= 16) 
                            ? submissionDateStr.substring(0, 16) 
                            : submissionDateStr;

                    // Add row to list
                    list.add(new EvidenceSummary(
                            rs.getString("EvidenceType"),
                            rs.getString("VerifiedStatus"),
                            rs.getString("AdminName"),
                            dateStr
                    ));
                }
                System.out.println("IncidentEvidenceSummaryReportController: Query returned " + rowCount + " rows");
            }

            // Populate table and update export button
            table.setItems(FXCollections.observableArrayList(list));
            exportButton.setDisable(list.isEmpty());

            if (list.isEmpty()) {
                showAlert("No evidence records found for " + year + "-" + String.format("%02d", month) + 
                        ". Try selecting a different year/month or check if data exists in the database.");
            } else {
                showAlert(list.size() + " evidence records.");
            }

        } catch (Exception e) {
            System.err.println("IncidentEvidenceSummaryReportController: Error generating report: " + e.getMessage());
            e.printStackTrace();
            showError("Failed: " + e.getMessage());
        }
    }

    /**
     * Handles the "Export" button click.
     * Saves the table contents to a CSV file selected by the user.
     */
    @FXML
    private void handleExport() {
        File file = new FileChooser().showSaveDialog(null); // Open file save dialog
        if (file == null) return; // User cancelled
        try (PrintWriter pw = new PrintWriter(file)) {
            // Write CSV header
            pw.println("Type,Status,ReviewedBy,Submitted");
            // Write each table row
            for (var e : table.getItems()) {
                pw.println(e.getType() + "," + e.getStatus() + "," + e.getAdmin() + "," + e.getDate());
            }
            showAlert("Exported"); // Notify user
        } catch (Exception e) {
            showError("Export failed"); // Show error if writing fails
        }
    }

    /**
     * Shows an information alert with the specified message.
     */
    private void showAlert(String msg) { 
        new Alert(Alert.AlertType.INFORMATION, msg).show(); 
    }

    /**
     * Shows an error alert with the specified message.
     */
    private void showError(String msg) { 
        new Alert(Alert.AlertType.ERROR, msg).show(); 
    }

    /**
     * Inner class representing a row in the Evidence Summary table.
     */
    public static class EvidenceSummary {
        private final String type, status, admin, date;

        /**
         * Constructs an EvidenceSummary instance.
         * @param t Evidence type
         * @param s Verified status
         * @param a Admin name (null will be converted to "Pending")
         * @param d Submission date
         */
        public EvidenceSummary(String t, String s, String a, String d) { 
            type = t; 
            status = s; 
            admin = a == null ? "Pending" : a; 
            date = d; 
        }

        public String getType() { return type; }
        public String getStatus() { return status; }
        public String getAdmin() { return admin; }
        public String getDate() { return date; }

        // Property getters for JavaFX table binding
        public javafx.beans.property.StringProperty typeProperty() { return new javafx.beans.property.SimpleStringProperty(type); }
        public javafx.beans.property.StringProperty statusProperty() { return new javafx.beans.property.SimpleStringProperty(status); }
        public javafx.beans.property.StringProperty adminProperty() { return new javafx.beans.property.SimpleStringProperty(admin); }
        public javafx.beans.property.StringProperty dateProperty() { return new javafx.beans.property.SimpleStringProperty(date); }
    }
}
