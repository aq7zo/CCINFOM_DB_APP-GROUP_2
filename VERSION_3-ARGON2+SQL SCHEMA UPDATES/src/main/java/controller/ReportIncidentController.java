package controller;

import dao.*;
import model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.DateUtils;
import util.SecurityUtils;
import util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction 1: Incident Reports
 */
public class ReportIncidentController {

    @FXML private TextField identifierField;
    @FXML private ComboBox<String> identifierTypeCombo;
    @FXML private TextField associatedNameField;
    @FXML private ComboBox<String> attackTypeCombo;
    @FXML private TextArea descriptionArea;
    @FXML private Button submitButton;
    @FXML private Label statusLabel;

    private Victim currentVictim;
    private final PerpetratorDAO perpDAO = new PerpetratorDAOImpl();
    private final AttackTypeDAO attackDAO = new AttackTypeDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final ThreatLevelLogDAO threatLogDAO = new ThreatLevelLogDAOImpl();
    private boolean initialized = false;

    @FXML
    private void initialize() {
        // Prevent multiple initializations
        if (initialized) {
            return;
        }
        initialized = true;

        // Load identifier types
        identifierTypeCombo.getItems().clear();
        identifierTypeCombo.setItems(FXCollections.observableArrayList(
                "Phone Number", "Email Address", "Social Media Account / Username",
                "Website URL / Domain", "IP Address"
        ));
        identifierTypeCombo.setValue("Phone Number");

        // Load attack types
        loadAttackTypes();

        // Real-time validation
        identifierField.textProperty().addListener((obs, old, newVal) -> validateForm());
        attackTypeCombo.valueProperty().addListener((obs, old, newVal) -> validateForm());
        descriptionArea.textProperty().addListener((obs, old, newVal) -> validateForm());
    }

    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
    }

    private void loadAttackTypes() {
        try {
            // Clear existing items first to prevent duplicates
            if (attackTypeCombo.getItems() != null) {
                attackTypeCombo.getItems().clear();
            }
            
            List<AttackType> types = attackDAO.findAll();
            // Use distinct to ensure no duplicates (in case database has duplicates)
            List<String> names = types.stream()
                    .map(AttackType::getAttackName)
                    .distinct()
                    .toList();
            
            attackTypeCombo.setItems(FXCollections.observableArrayList(names));
            System.out.println("Loaded " + names.size() + " attack types into ComboBox");
        } catch (Exception e) {
            showError("Failed to load attack types: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        try {
            String identifier = identifierField.getText().trim();
            String type = identifierTypeCombo.getValue();
            String name = associatedNameField.getText().trim();
            String attackName = attackTypeCombo.getValue();
            String desc = descriptionArea.getText().trim();

            // 1. Find or create perpetrator
            Perpetrator perp = perpDAO.findByIdentifier(identifier);
            if (perp == null) {
                perp = new Perpetrator();
                perp.setIdentifier(identifier);
                perp.setIdentifierType(type);
                perp.setAssociatedName(name.isEmpty() ? null : name);
                perp.setThreatLevel("UnderReview");
                perp.setLastIncidentDate(LocalDateTime.now());
                perpDAO.create(perp);
            } else {
                // Update last incident
                perp.setLastIncidentDate(LocalDateTime.now());
                perpDAO.update(perp);
            }

            // 2. Create incident
            IncidentReport report = new IncidentReport();
            report.setVictimID(currentVictim.getVictimID());
            report.setPerpetratorID(perp.getPerpetratorID());
            report.setAttackTypeID(attackDAO.findByName(attackName).getAttackTypeID());
            report.setDateReported(LocalDateTime.now());
            report.setDescription(desc);
            report.setStatus("Pending");
            incidentDAO.create(report);

            // 3. Auto-escalate threat level
            int victimCount = incidentDAO.countVictimsLast7Days(perp.getPerpetratorID());
            if (victimCount >= 3 && !"Malicious".equals(perp.getThreatLevel())) {
                String oldLevel = perp.getThreatLevel();
                perp.setThreatLevel("Malicious");
                perpDAO.update(perp);
                threatLogDAO.logChange(perp.getPerpetratorID(), oldLevel, "Malicious", 1); // Admin 1
                showAlert(Alert.AlertType.WARNING, "Perpetrator Escalated",
                        "Identifier: " + identifier + "\nNow marked as MALICIOUS (" + victimCount + " victims in 7 days)");
            }

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Incident reported successfully!\nID: " + report.getIncidentID());
            clearForm();

        } catch (Exception e) {
            showError("Submit failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        String id = identifierField.getText().trim();
        String attack = attackTypeCombo.getValue();
        String desc = descriptionArea.getText().trim();

        if (id.isEmpty() || attack == null || desc.isEmpty() || desc.length() < 10) {
            valid = false;
        }

        submitButton.setDisable(!valid);
        return valid;
    }

    private void clearForm() {
        identifierField.clear();
        associatedNameField.clear();
        descriptionArea.clear();
        attackTypeCombo.setValue(null);
        statusLabel.setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String msg) {
        showAlert(Alert.AlertType.ERROR, "Error", msg);
    }

    @FXML private void handleCancel() {
        ((Stage) submitButton.getScene().getWindow()).close();
    }
}