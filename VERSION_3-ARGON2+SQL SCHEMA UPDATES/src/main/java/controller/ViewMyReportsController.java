package controller;

import dao.AttackTypeDAO;
import dao.AttackTypeDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.PerpetratorDAO;
import dao.PerpetratorDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.AttackType;
import model.IncidentReport;
import model.Perpetrator;
import model.Victim;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the "View My Reports" page.
 * Handles displaying all incident reports associated with the currently logged-in victim.
 */
public class ViewMyReportsController {

    @FXML private TableView<IncidentReport> reportsTable; // Table to display incident reports
    @FXML private TableColumn<IncidentReport, Integer> idCol; // Column for incident ID
    @FXML private TableColumn<IncidentReport, String> dateCol; // Column for date reported
    @FXML private TableColumn<IncidentReport, String> attackTypeCol; // Column for attack type
    @FXML private TableColumn<IncidentReport, String> perpetratorCol; // Column for perpetrator identifier
    @FXML private TableColumn<IncidentReport, String> statusCol; // Column for report status
    @FXML private TableColumn<IncidentReport, String> descriptionCol; // Column for report description
    @FXML private TableColumn<IncidentReport, String> reviewedByCol; // Column for administrator who reviewed

    // Currently logged-in victim
    private Victim currentVictim;

    // DAO objects for accessing database
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final AttackTypeDAO attackDAO = new AttackTypeDAOImpl();
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
    private final dao.AdministratorDAO adminDAO = new dao.AdministratorDAOImpl();

    /**
     * Initializes the controller and configures the table columns.
     * This method is automatically called by JavaFX after FXML is loaded.
     */
    @FXML
    private void initialize() {
        // Map incident ID to table column
        idCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));

        // Map date reported to table column and format it as "yyyy-MM-dd HH:mm"
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateReported();
            String dateStr = date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });

        // Map attack type ID to attack type name
        attackTypeCol.setCellValueFactory(cellData -> {
            try {
                int attackTypeID = cellData.getValue().getAttackTypeID();
                AttackType type = attackDAO.findById(attackTypeID);
                String name = type != null ? type.getAttackName() : "Unknown";
                return new javafx.beans.property.SimpleStringProperty(name);
            } catch (Exception e) {
                // Return "Error" if DAO lookup fails
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        // Map perpetrator ID to identifier string
        perpetratorCol.setCellValueFactory(cellData -> {
            try {
                int perpID = cellData.getValue().getPerpetratorID();
                Perpetrator perp = perpDAO.findById(perpID);
                String identifier = perp != null ? perp.getIdentifier() : "Unknown";
                return new javafx.beans.property.SimpleStringProperty(identifier);
            } catch (Exception e) {
                // Return "Error" if DAO lookup fails
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        // Map report status and provide default value if null
        statusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            String displayStatus = status != null ? status : "Pending";
            return new javafx.beans.property.SimpleStringProperty(displayStatus);
        });

        // Customize status column with colors: green for "Validated", yellow for "Pending"
        statusCol.setCellFactory(column -> new javafx.scene.control.TableCell<IncidentReport, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("Validated".equals(status)) {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else if ("Pending".equals(status)) {
                        setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Map description directly
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Map admin ID to reviewer name
        reviewedByCol.setCellValueFactory(cellData -> {
            IncidentReport report = cellData.getValue();
            Integer adminID = report.getAdminID();
            if (adminID != null && adminID > 0) {
                try {
                    model.Administrator admin = adminDAO.findById(adminID);
                    if (admin != null) {
                        return new javafx.beans.property.SimpleStringProperty(admin.getName());
                    }
                } catch (Exception e) {
                    // Fallback: show admin ID if lookup fails
                    return new javafx.beans.property.SimpleStringProperty("Admin #" + adminID);
                }
            }
            // Default if not reviewed yet
            return new javafx.beans.property.SimpleStringProperty("Not reviewed");
        });

        // Set preferred widths for table columns
        idCol.setPrefWidth(80);
        dateCol.setPrefWidth(150);
        attackTypeCol.setPrefWidth(150);
        perpetratorCol.setPrefWidth(200);
        statusCol.setPrefWidth(120);
        descriptionCol.setPrefWidth(250);
        reviewedByCol.setPrefWidth(150);
    }

    /**
     * Sets the currently logged-in victim and refreshes their reports in the table.
     * @param victim the logged-in victim
     */
    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        refreshReports();
    }

    /**
     * Refreshes the incident reports table for the current victim.
     * Fetches data from the database and populates the table.
     */
    public void refreshReports() {
        if (currentVictim == null) return;

        try {
            // Fetch reports from database
            List<IncidentReport> reports = incidentDAO.findByVictimID(currentVictim.getVictimID());
            ObservableList<IncidentReport> observableReports = FXCollections.observableArrayList(reports);
            // Populate table
            reportsTable.setItems(observableReports);
        } catch (Exception e) {
            // Show error alert if loading fails
            showError("Failed to load reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays an error alert with the specified message.
     * @param msg the message to show in the alert
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
