package controller;

import dao.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import model.Administrator;
import model.Perpetrator;
import model.ThreatLevelLog;

import java.util.List;

/**
 * Transaction 3: Perpetrator Threat Level Update (Admin)
 *
 * Assigned To: Martin, Kurt Nehemiah Z.
 *
 * Core Records Used:
 * - Perpetrators Record Management
 * - Incident Reports Transaction
 * - Administrators Record Management
 *
 * Transaction Record: ThreatLevelLog
 * Attributes: LogID, PerpetratorID, OldThreatLevel, NewThreatLevel, ChangeDate, AdminID
 *
 * Services/Operations:
 * - Read perpetrator’s record
 * - Count unique victims (optional auto-escalation)
 * - Update ThreatLevel if threshold met
 * - Insert log entry
 * - Notify staff
 */
public class ReviewThreatLevelController {

    @FXML private TableView<Perpetrator> perpTable;
    @FXML private TableColumn<Perpetrator, String> idCol;
    @FXML private TableColumn<Perpetrator, String> typeCol;
    @FXML private TableColumn<Perpetrator, String> nameCol;
    @FXML private TableColumn<Perpetrator, String> levelCol;
    @FXML private Button escalateButton;
    @FXML private ComboBox<String> newLevelCombo;

    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
    private final ThreatLevelLogDAO logDAO = new ThreatLevelLogDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private Administrator currentAdmin;

    @FXML
    private void initialize() {
        // Populate combo box
        newLevelCombo.setItems(FXCollections.observableArrayList(
                "UnderReview", "Suspected", "Malicious", "Cleared"
        ));

        // Disable button until selection
        escalateButton.setDisable(true);
        perpTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            escalateButton.setDisable(newVal == null);
            if (newVal != null) {
                newLevelCombo.setValue(newVal.getThreatLevel()); // Pre-select current
            }
        });

        loadPerpetrators();
    }

    /**
     * Set the currently logged-in admin (passed from AdminDashboard)
     */
    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
    }

    /**
     * Load all perpetrators and bind table columns
     */
    private void loadPerpetrators() {
        try {
            List<Perpetrator> list = perpDAO.findAll();
            perpTable.setItems(FXCollections.observableArrayList(list));

            // Bind table columns to model properties
            idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdentifier()));
            typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdentifierType()));
            nameCol.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getAssociatedName() != null ? d.getValue().getAssociatedName() : ""
            ));
            levelCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getThreatLevel()));

            // Optional: Auto-highlight high-risk (≥3 victims in last 7 days)
            highlightHighRiskPerpetrators();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load perpetrators: " + e.getMessage());
        }
    }

    /**
     * Auto-detect and highlight perpetrators with ≥3 unique victims in last 7 days
     */
    private void highlightHighRiskPerpetrators() {
        perpTable.setRowFactory(tv -> new TableRow<Perpetrator>() {
            @Override
            protected void updateItem(Perpetrator item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    try {
                        int victimCount = incidentDAO.countUniqueVictimsLast7Days(item.getPerpetratorID());
                        if (victimCount >= 3 && !"Malicious".equals(item.getThreatLevel())) {
                            setStyle("-fx-background-color: #ffcccc; -fx-font-weight: bold;");
                            setTooltip(new Tooltip("HIGH RISK: " + victimCount + " victims in last 7 days"));
                        } else {
                            setStyle("");
                            setTooltip(null);
                        }
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });
    }

    /**
     * Handle manual escalation
     */
    @FXML
    private void handleEscalate() {
        Perpetrator selected = perpTable.getSelectionModel().getSelectedItem();
        String newLevel = newLevelCombo.getValue();

        if (selected == null || newLevel == null || currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Action", "Select a perpetrator and threat level.");
            return;
        }

        String oldLevel = selected.getThreatLevel();

        if (oldLevel.equals(newLevel)) {
            showAlert(Alert.AlertType.INFORMATION, "No Change", "Threat level is already " + newLevel);
            return;
        }

        try {
            // Update perpetrator
            selected.setThreatLevel(newLevel);
            perpDAO.update(selected);

            // Log the change
            ThreatLevelLog log = new ThreatLevelLog();
            log.setPerpetratorID(selected.getPerpetratorID());
            log.setOldThreatLevel(oldLevel);
            log.setNewThreatLevel(newLevel);
            log.setAdminID(currentAdmin.getAdminID());
            logDAO.insert(log);

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Threat level updated: " + oldLevel + " → " + newLevel + "\n" +
                            "Logged by: " + currentAdmin.getName());

            loadPerpetrators(); // Refresh

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update threat level: " + e.getMessage());
        }
    }

    /**
     * Reusable alert method
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}