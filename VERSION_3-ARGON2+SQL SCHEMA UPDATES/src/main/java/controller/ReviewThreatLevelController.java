package controller;

import dao.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Perpetrator;
import model.ThreatLevelLog;

import java.util.List;

/**
 * Transaction 3: Perpetrator Threat Level Update (Admin)
 */
public class ReviewThreatLevelController {

    @FXML private TableView<Perpetrator> perpTable;
    @FXML private TableColumn<Perpetrator, String> idCol, typeCol, nameCol, levelCol;
    @FXML private Button escalateButton;
    @FXML private ComboBox<String> newLevelCombo;

    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
    private final ThreatLevelLogDAO logDAO = new ThreatLevelLogDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private Administrator currentAdmin;

    @FXML
    private void initialize() {
        newLevelCombo.setItems(FXCollections.observableArrayList(
                "Suspected", "Malicious", "Cleared"
        ));

        perpTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            escalateButton.setDisable(newVal == null);
        });

        loadPerpetrators();
    }

    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
    }

    private void loadPerpetrators() {
        try {
            List<Perpetrator> list = perpDAO.findAll();
            perpTable.setItems(FXCollections.observableArrayList(list));

            idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdentifier()));
            typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdentifierType()));
            nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAssociatedName()));
            levelCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getThreatLevel()));
        } catch (Exception e) {
            showError("Load failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleEscalate() {
        Perpetrator selected = perpTable.getSelectionModel().getSelectedItem();
        String newLevel = newLevelCombo.getValue();
        if (selected == null || newLevel == null) return;

        try {
            String oldLevel = selected.getThreatLevel();
            if (oldLevel.equals(newLevel)) {
                showAlert("No change needed");
                return;
            }

            selected.setThreatLevel(newLevel);
            perpDAO.update(selected);
            logDAO.logChange(selected.getPerpetratorID(), oldLevel, newLevel, currentAdmin.getAdminID());

            showAlert(Alert.AlertType.INFORMATION, "Updated",
                    "Threat level changed: " + oldLevel + " → " + newLevel);
            loadPerpetrators();
        } catch (Exception e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }
}