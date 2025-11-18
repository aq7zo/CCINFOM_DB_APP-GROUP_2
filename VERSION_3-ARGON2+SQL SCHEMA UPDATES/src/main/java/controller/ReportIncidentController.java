package controller;

import dao.*;
import model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

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
    private final VictimDAO victimDAO = new VictimDAOImpl();
    private final VictimStatusLogDAO victimStatusLogDAO = new VictimStatusLogDAOImpl();
    private boolean initialized = false;
    private Consumer<IncidentReport> incidentCreatedCallback;
    private static final int FLAG_THRESHOLD_MONTH = 5;
    private static final int MIN_DESCRIPTION_LENGTH = 10;

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
        
        // Set initial prompt text
        updateIdentifierPromptText("Phone Number");
        
        // Update prompt text when identifier type changes
        identifierTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateIdentifierPromptText(newVal);
            }
        });

        // Load attack types
        loadAttackTypes();

        // Real-time validation
        identifierField.textProperty().addListener((obs, old, newVal) -> validateForm());
        attackTypeCombo.valueProperty().addListener((obs, old, newVal) -> validateForm());
        descriptionArea.textProperty().addListener((obs, old, newVal) -> validateForm());

        validateForm();
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
            if (!names.isEmpty()) {
                attackTypeCombo.getSelectionModel().selectFirst();
            }
            System.out.println("Loaded " + names.size() + " attack types into ComboBox");
            validateForm();
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
            // Map UI identifier type to database ENUM value
            String dbIdentifierType = mapIdentifierTypeToDB(type);
            String name = associatedNameField.getText().trim();
            String attackName = attackTypeCombo.getValue();
            String desc = descriptionArea.getText().trim();

            // 1. Find or create perpetrator
            Perpetrator perp = perpDAO.findByIdentifier(identifier);
            if (perp == null) {
                perp = new Perpetrator();
                perp.setIdentifier(identifier);
                perp.setIdentifierType(dbIdentifierType);
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

            // 4. Auto-flag victim if threshold exceeded
            autoFlagVictimIfNeeded(report);

            if (incidentCreatedCallback != null) {
                incidentCreatedCallback.accept(report);
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
        String id = identifierField.getText() != null ? identifierField.getText().trim() : "";
        String attack = attackTypeCombo.getValue();
        String desc = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";

        boolean hasIdentifier = !id.isEmpty();
        boolean hasAttackSelection = attack != null && !attack.isBlank();
        boolean hasDescription = desc.length() >= MIN_DESCRIPTION_LENGTH;

        valid = hasIdentifier && hasAttackSelection && hasDescription;

        if (statusLabel != null) {
            if (valid) {
                statusLabel.setText("");
            } else if (!hasIdentifier) {
                statusLabel.setText("Identifier is required.");
            } else if (!hasAttackSelection) {
                statusLabel.setText("Please choose an attack type.");
            } else if (!hasDescription) {
                statusLabel.setText("Description must be at least " + MIN_DESCRIPTION_LENGTH + " characters.");
            }
        }

        submitButton.setDisable(!valid);
        return valid;
    }

    /**
     * Maps UI identifier type values to database ENUM values
     */
    private String mapIdentifierTypeToDB(String uiType) {
        if (uiType == null) return null;
        
        switch (uiType) {
            case "Phone Number":
                return "Phone Number";
            case "Email Address":
                return "Email Address";
            case "Social Media Account / Username":
                return "Social Media Account";
            case "Website URL / Domain":
                return "Website URL";
            case "IP Address":
                return "IP Address";
            default:
                return uiType; // Fallback to original value
        }
    }

    private void updateIdentifierPromptText(String identifierType) {
        if (identifierField == null) return;
        
        switch (identifierType) {
            case "Phone Number":
                identifierField.setPromptText("+63-912-345-6789");
                break;
            case "Email Address":
                identifierField.setPromptText("example@email.com");
                break;
            case "Social Media Account / Username":
                identifierField.setPromptText("@someone67");
                break;
            case "Website URL / Domain":
                identifierField.setPromptText("fake.website.com");
                break;
            case "IP Address":
                identifierField.setPromptText("162.42.93.207");
                break;
            default:
                identifierField.setPromptText("");
                break;
        }
    }

    private void clearForm() {
        identifierField.clear();
        associatedNameField.clear();
        descriptionArea.clear();
        if (attackTypeCombo.getItems() != null && !attackTypeCombo.getItems().isEmpty()) {
            attackTypeCombo.getSelectionModel().selectFirst();
        } else {
            attackTypeCombo.setValue(null);
        }
        // Restore prompt text after clearing
        if (identifierTypeCombo.getValue() != null) {
            updateIdentifierPromptText(identifierTypeCombo.getValue());
        }
        statusLabel.setText("");
        validateForm();
    }

    public void setIncidentCreatedCallback(Consumer<IncidentReport> incidentCreatedCallback) {
        this.incidentCreatedCallback = incidentCreatedCallback;
    }

    private void autoFlagVictimIfNeeded(IncidentReport report) {
        if (currentVictim == null) return;
        try {
            int incidentCountThisMonth = incidentDAO.countIncidentsLastMonth(currentVictim.getVictimID());
            if (incidentCountThisMonth > FLAG_THRESHOLD_MONTH && !"Flagged".equals(currentVictim.getAccountStatus())) {
                String oldStatus = currentVictim.getAccountStatus();
                String newStatus = "Flagged";
                victimDAO.updateAccountStatus(currentVictim.getVictimID(), newStatus);
                victimStatusLogDAO.logChange(currentVictim.getVictimID(), oldStatus, newStatus, null);
                currentVictim.setAccountStatus(newStatus);
                showAlert(Alert.AlertType.WARNING, "Account Flagged",
                        "Your account has been flagged for additional support.\n" +
                                "Incidents reported this month: " + incidentCountThisMonth);
            }
        } catch (Exception e) {
            System.err.println("Failed to auto-flag victim: " + e.getMessage());
        }
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