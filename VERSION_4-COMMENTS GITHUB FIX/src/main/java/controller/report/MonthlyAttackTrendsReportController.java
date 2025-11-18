package controller.report;

import dao.AttackTypeDAO;
import dao.AttackTypeDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.YearMonth;
import java.util.*;

/**
 * Controller for the "Monthly Attack Trends" report.
 * Displays hourly incident counts per attack type and allows exporting to CSV.
 */
public class MonthlyAttackTrendsReportController {

    // FXML UI components
    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Integer> monthCombo;
    @FXML private BarChart<String, Number> chart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Button generateButton;
    @FXML private Button exportButton;

    // DAO objects for database access
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final AttackTypeDAO attackDAO = new AttackTypeDAOImpl();

    /**
     * Initializes the UI components after FXML is loaded.
     * Sets default year/month values and prepares the chart axes.
     */
    @FXML
    private void initialize() {
        // Populate year ComboBox from 2020 to current year
        int currentYear = YearMonth.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int y = 2020; y <= currentYear; y++) years.add(y);
        yearCombo.setItems(FXCollections.observableArrayList(years));
        yearCombo.setValue(currentYear);

        // Populate month ComboBox (1-12)
        List<Integer> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) months.add(m);
        monthCombo.setItems(FXCollections.observableArrayList(months));
        monthCombo.setValue(YearMonth.now().getMonthValue());

        // Configure chart axes
        xAxis.setLabel("Hour of Day (0-23)");
        yAxis.setLabel("Number of Incidents");
        chart.setTitle("Attack Trends by Hour");

        // Add listeners to enable/disable Generate button
        yearCombo.valueProperty().addListener((obs, old, val) -> validate());
        monthCombo.valueProperty().addListener((obs, old, val) -> validate());
        validate();
    }

    /**
     * Validates that year and month are selected to enable the Generate button.
     */
    private void validate() {
        generateButton.setDisable(yearCombo.getValue() == null || monthCombo.getValue() == null);
    }

    /**
     * Generates the monthly attack trends report based on selected year and month.
     * Queries the database for hourly incidents per attack type and populates the chart.
     */
    @FXML
    private void handleGenerate() {
        int year = yearCombo.getValue();
        int month = monthCombo.getValue();

        try {
            // Map to store hourly incident counts per attack type
            Map<String, int[]> hourlyData = new HashMap<>();
            List<model.AttackType> types = attackDAO.findAll();

            System.out.println("MonthlyAttackTrendsReportController: Generating report for " + year + "-" + month);
            System.out.println("MonthlyAttackTrendsReportController: Found " + types.size() + " attack types");

            // Initialize hourly arrays for each attack type
            for (model.AttackType type : types) {
                hourlyData.put(type.getAttackName(), new int[24]);
            }

            // SQL query to fetch incidents for the selected month/year
            String sql = """
                SELECT a.AttackName, HOUR(i.DateReported) as hour
                FROM IncidentReports i
                JOIN AttackTypes a ON i.AttackTypeID = a.AttackTypeID
                WHERE YEAR(i.DateReported) = ? AND MONTH(i.DateReported) = ?
                """;

            System.out.println("MonthlyAttackTrendsReportController: Executing SQL: " + sql);
            int totalRows = 0;

            // Execute SQL query
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);
                stmt.setInt(2, month);
                var rs = stmt.executeQuery();

                while (rs.next()) {
                    totalRows++;
                    String attack = rs.getString("AttackName");
                    int hour = rs.getInt("hour");
                    if (hourlyData.containsKey(attack)) {
                        hourlyData.get(attack)[hour]++;
                    }
                }
                System.out.println("MonthlyAttackTrendsReportController: Query returned " + totalRows + " rows");
            }

            // Build chart data series
            chart.getData().clear();
            int seriesCount = 0;
            for (Map.Entry<String, int[]> entry : hourlyData.entrySet()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(entry.getKey());
                boolean hasData = false;
                for (int h = 0; h < 24; h++) {
                    if (entry.getValue()[h] > 0) {
                        series.getData().add(new XYChart.Data<>(String.valueOf(h), entry.getValue()[h]));
                        hasData = true;
                    }
                }
                if (hasData) {
                    chart.getData().add(series);
                    seriesCount++;
                }
            }

            // Enable export button if there is data
            exportButton.setDisable(seriesCount == 0);

            // Notify user
            if (totalRows == 0) {
                showAlert("No incidents found for " + YearMonth.of(year, month) +
                        ". Try selecting a different year/month or check if data exists in the database.");
            } else {
                showAlert("Report generated for " + YearMonth.of(year, month) +
                        ". Showing " + seriesCount + " attack type(s) with data.");
            }

        } catch (Exception e) {
            System.err.println("MonthlyAttackTrendsReportController: Error generating report: " + e.getMessage());
            e.printStackTrace();
            showError("Generate failed: " + e.getMessage());
        }
    }

    /**
     * Exports the current chart data to a CSV file using a file chooser.
     */
    @FXML
    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(null);
        if (file == null) return; // user canceled

        try (PrintWriter pw = new PrintWriter(file)) {
            // Write CSV header
            pw.println("AttackType,Hour0,Hour1,...,Hour23");

            // Write each series as a row
            for (var series : chart.getData()) {
                String name = series.getName();
                StringBuilder row = new StringBuilder(name);
                for (int h = 0; h < 24; h++) {
                    row.append(",").append(getCount(series, h));
                }
                pw.println(row);
            }
            showAlert("Exported to " + file.getName());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    /**
     * Returns the count of incidents for a given hour in the series.
     *
     * @param series the chart series
     * @param hour   the hour (0-23)
     * @return count of incidents for that hour
     */
    private int getCount(XYChart.Series<String, Number> series, int hour) {
        for (var data : series.getData()) {
            if (data.getXValue().equals(String.valueOf(hour))) {
                return data.getYValue().intValue();
            }
        }
        return 0;
    }

    /**
     * Shows an information alert with the given message.
     *
     * @param msg the message to display
     */
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.show();
    }

    /**
     * Shows an error alert with the given message.
     *
     * @param msg the message to display
     */
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.show();
    }
}
