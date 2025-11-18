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
 * View My Reports Controller
 * Displays all incident reports for the current victim
 */
public class ViewMyReportsController {

    @FXML private TableView<IncidentReport> reportsTable;
    @FXML private TableColumn<IncidentReport, Integer> idCol;
    @FXML private TableColumn<IncidentReport, String> dateCol;
    @FXML private TableColumn<IncidentReport, String> attackTypeCol;
    @FXML private TableColumn<IncidentReport, String> perpetratorCol;
    @FXML private TableColumn<IncidentReport, String> statusCol;
    @FXML private TableColumn<IncidentReport, String> evidenceStatusCol;
    @FXML private TableColumn<IncidentReport, String> descriptionCol;
    @FXML private TableColumn<IncidentReport, String> reviewedByCol;

    private Victim currentVictim;
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final AttackTypeDAO attackDAO = new AttackTypeDAOImpl();
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
    private final dao.AdministratorDAO adminDAO = new dao.AdministratorDAOImpl();

    @FXML
    private void initialize() {
        // Configure table columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        
        dateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateReported();
            String dateStr = date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        
        attackTypeCol.setCellValueFactory(cellData -> {
            try {
                int attackTypeID = cellData.getValue().getAttackTypeID();
                AttackType type = attackDAO.findById(attackTypeID);
                String name = type != null ? type.getAttackName() : "Unknown";
                return new javafx.beans.property.SimpleStringProperty(name);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });
        
        perpetratorCol.setCellValueFactory(cellData -> {
            try {
                int perpID = cellData.getValue().getPerpetratorID();
                Perpetrator perp = perpDAO.findById(perpID);
                String identifier = perp != null ? perp.getIdentifier() : "Unknown";
                return new javafx.beans.property.SimpleStringProperty(identifier);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });
        
        statusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            // Make status more visible with styling indication
            String displayStatus = status != null ? status : "Pending";
            return new javafx.beans.property.SimpleStringProperty(displayStatus);
        });
        
        // Custom cell factory for status to add colors
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
        
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Show who reviewed the report
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
                    // If can't find admin, show ID
                    return new javafx.beans.property.SimpleStringProperty("Admin #" + adminID);
                }
            }
            return new javafx.beans.property.SimpleStringProperty("Not reviewed");
        });
        
        // Set column widths
        idCol.setPrefWidth(80);
        dateCol.setPrefWidth(150);
        attackTypeCol.setPrefWidth(150);
        perpetratorCol.setPrefWidth(200);
        statusCol.setPrefWidth(120);
        descriptionCol.setPrefWidth(250);
        reviewedByCol.setPrefWidth(150);
    }

    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        refreshReports();
    }

    public void refreshReports() {
        if (currentVictim == null) return;

        try {
            List<IncidentReport> reports = incidentDAO.findByVictimID(currentVictim.getVictimID());
            ObservableList<IncidentReport> observableReports = FXCollections.observableArrayList(reports);
            reportsTable.setItems(observableReports);
        } catch (Exception e) {
            showError("Failed to load reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

