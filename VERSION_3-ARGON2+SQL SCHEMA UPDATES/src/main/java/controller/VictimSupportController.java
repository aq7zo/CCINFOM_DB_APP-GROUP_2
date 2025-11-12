package controller;

import dao.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Victim;
import model.VictimStatusLog;

import java.util.List;

/**
 * Transaction 4: Victim Account Status Update (Admin)
 */
public class VictimSupportController {

    @FXML private TableView<Victim> victimTable;
    @FXML private TableColumn<Victim, String> nameCol, emailCol, statusCol;
    @FXML private Button flagButton;
    @FXML private TextArea notesArea;

    private final VictimDAO victimDAO = new VictimDAOImpl();
    private final VictimStatusLogDAO logDAO = new VictimStatusLogDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private Administrator currentAdmin;

    @FXML
    private void initialize() {
        victimTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            flagButton.setDisable(newVal == null);
        });
        loadVictims();
    }

    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
    }

    private void loadVictims() {
        try {
            List<Victim> list = victimDAO.findAll();
            victimTable.setItems(FXCollections.observableArrayList(list));

            nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
            emailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContactEmail()));
            statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccountStatus()));
        } catch (Exception e) {
            showError("Load failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleFlagVictim() {
        Victim selected = victimTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            int count = incidentDAO.countIncidentsLastMonth(selected.getVictimID());
            if (count <= 5 && !"Flagged".equals(selected.getAccountStatus())) {
                showAlert("Victim has only " + count + " incidents. Not flagging.");
                return;
            }

            String oldStatus = selected.getAccountStatus();
            selected.setAccountStatus("Flagged");
            victimDAO.update(selected);
            logDAO.logChange(selected.getVictimID(), oldStatus, "Flagged", currentAdmin.getAdminID());

            showAlert(Alert.AlertType.WARNING, "Victim Flagged",
                    selected.getName() + " is now FLAGGED for support.\nIncidents last month: " + count);
            loadVictims();
        } catch (Exception e) {
            showError("Flag failed: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    private void showError(String msg) {
        showAlert(Alert.AlertType.ERROR, "Error", msg);
    }

    private void showAlert(String msg) {
        showAlert(Alert.AlertType.INFORMATION, "Info", msg);
    }
}