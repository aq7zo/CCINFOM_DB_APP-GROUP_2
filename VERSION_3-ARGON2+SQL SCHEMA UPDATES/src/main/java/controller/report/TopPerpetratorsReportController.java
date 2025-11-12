package controller.report;

import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.PerpetratorDAO;
import dao.PerpetratorDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Report: Top Perpetrators
 * Ranks by incident count in selected month
 */
public class TopPerpetratorsReportController {

    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Integer> monthCombo;
    @FXML private TableView<TopPerp> table;
    @FXML private TableColumn<TopPerp, String> idCol, typeCol, nameCol, attacksCol;
    @FXML private Button generateButton;
    @FXML private Button exportButton;

    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();

    @FXML
    private void initialize() {
        int year = java.time.Year.now().getValue();
        yearCombo.setItems(FXCollections.observableArrayList(2020, 2021, 2022, 2023, 2024, year));
        yearCombo.setValue(year);
        monthCombo.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        monthCombo.setValue(java.time.Month.from(java.time.LocalDate.now()).getValue());

        idCol.setCellValueFactory(d -> d.getValue().identifierProperty());
        typeCol.setCellValueFactory(d -> d.getValue().typeProperty());
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        attacksCol.setCellValueFactory(d -> d.getValue().countProperty().asObject());

        validate();
    }

    private void validate() {
        generateButton.setDisable(yearCombo.getValue() == null || monthCombo.getValue() == null);
    }

    @FXML
    private void handleGenerate() {
        int year = yearCombo.getValue();
        int month = monthCombo.getValue();

        try {
            String sql = """
                SELECT p.Identifier, p.IdentifierType, p.AssociatedName, COUNT(*) as cnt
                FROM IncidentReports i
                JOIN Perpetrators p ON i.PerpetratorID = p.PerpetratorID
                WHERE YEAR(i.DateReported) = ? AND MONTH(i.DateReported) = ?
                GROUP BY p.PerpetratorID
                ORDER BY cnt DESC LIMIT 10
                """;

            List<TopPerp> list = new ArrayList<>();
            try (var conn = util.DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);
                stmt.setInt(2, month);
                var rs = stmt.executeQuery();

                while (rs.next()) {
                    list.add(new TopPerp(
                            rs.getString("Identifier"),
                            rs.getString("IdentifierType"),
                            rs.getString("AssociatedName"),
                            rs.getInt("cnt")
                    ));
                }
            }

            table.setItems(FXCollections.observableArrayList(list));
            exportButton.setDisable(false);
            showAlert("Top 10 Perpetrators loaded for " + year + "-" + String.format("%02d", month));

        } catch (Exception e) {
            showError("Failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        File file = new FileChooser().showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("Identifier,Type,Name,IncidentCount");
            for (var row : table.getItems()) {
                pw.println(row.getIdentifier() + "," + row.getType() + "," + row.getName() + "," + row.getCount());
            }
            showAlert("Exported to " + file.getName());
        } catch (Exception e) {
            showError("Export failed");
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    public static class TopPerp {
        private final String identifier, type, name;
        private final int count;
        public TopPerp(String i, String t, String n, int c) {
            this.identifier = i; this.type = t; this.name = n; this.count = c;
        }
        public String getIdentifier() { return identifier; }
        public String getType() { return type; }
        public String getName() { return name == null ? "" : name; }
        public int getCount() { return count; }
        public javafx.beans.property.StringProperty identifierProperty() { return new javafx.beans.property.SimpleStringProperty(identifier); }
        public javafx.beans.property.StringProperty typeProperty() { return new javafx.beans.property.SimpleStringProperty(type); }
        public javafx.beans.property.StringProperty nameProperty() { return new javafx.beans.property.SimpleStringProperty(getName()); }
        public javafx.beans.property.IntegerProperty countProperty() { return new javafx.beans.property.SimpleIntegerProperty(count); }
    }
}