package app.controllers;

import app.models.AttackType;
import app.models.Victim;
import app.services.AttackTypeService;
import app.services.EvidenceService;
import app.services.IncidentService;
import app.services.PerpetratorService;
import app.services.VictimService;
import app.utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Guided reporting controller for public victims filing incidents without logging in.
 */
public class VictimPortalController {
    private static final String[] IDENTIFIER_TYPES = {
            "Email Address",
            "Phone Number",
            "Website URL",
            "Social Media Account",
            "Messaging Handle",
            "Cryptocurrency Wallet",
            "IP Address"
    };

    @FXML private TabPane stepper;
    @FXML private Label stepTitleLabel;
    @FXML private Label portalSubtitle;

    // Step 1 controls
    @FXML private TextField victimNameField;
    @FXML private TextField victimEmailField;
    @FXML private TextField victimEmailConfirmField;
    @FXML private ProgressIndicator identityProgress;
    @FXML private Label identityFeedbackLabel;

    // Step 2 controls
    @FXML private ComboBox<String> identifierTypeField;
    @FXML private TextField identifierValueField;
    @FXML private TextField associatedAliasField;
    @FXML private ComboBox<AttackType> attackTypeField;
    @FXML private Label incidentFeedbackLabel;

    // Step 3 controls
    @FXML private TextArea incidentDescriptionField;
    @FXML private TextField evidencePathField;
    @FXML private Button submitWizardButton;
    @FXML private ProgressIndicator submissionProgress;
    @FXML private Label submissionFeedbackLabel;

    private final VictimService victimService = new VictimService();
    private final PerpetratorService perpetratorService = new PerpetratorService();
    private final AttackTypeService attackTypeService = new AttackTypeService();
    private final IncidentService incidentService = new IncidentService();
    private final EvidenceService evidenceService = new EvidenceService();

    private Victim registeredVictim;
    private File selectedEvidenceFile;
    private String cachedIdentifierType;
    private String cachedIdentifierValue;
    private String cachedAlias;
    private AttackType cachedAttackType;

    @FXML
    private void initialize() {
        configureStepper();
        configureControls();
        loadAttackTypes();
    }

    @FXML
    private void handleIdentityNext() {
        if (!validateIdentityStep()) {
            return;
        }

        identityProgress.setVisible(true);
        clearFeedback(identityFeedbackLabel);

        String name = victimNameField.getText().trim();
        String email = victimEmailField.getText().trim();

        Victim existingVictim = victimService.getVictimByEmail(email);
        if (existingVictim == null) {
            boolean created = victimService.createVictim(name, email);
            if (!created) {
                showFeedback(identityFeedbackLabel, "We could not register your profile. Please retry.", false);
                identityProgress.setVisible(false);
                return;
            }
            existingVictim = victimService.getVictimByEmail(email);
        }

        registeredVictim = existingVictim;
        portalSubtitle.setText("Reporting as " + registeredVictim.getName() + ". You will receive email updates for this case.");
        showFeedback(identityFeedbackLabel, "Identity confirmed. Continue with incident details.", true);
        enableStep(1);
        selectStep(1);
        identityProgress.setVisible(false);
    }

    @FXML
    private void handleIncidentBack() {
        selectStep(0);
    }

    @FXML
    private void handleIncidentNext() {
        if (!validateIncidentStep()) {
            return;
        }
        cachedIdentifierType = identifierTypeField.getValue();
        cachedIdentifierValue = identifierValueField.getText().trim();
        cachedAlias = associatedAliasField.getText() != null ? associatedAliasField.getText().trim() : "";
        cachedAttackType = attackTypeField.getValue();

        enableStep(2);
        selectStep(2);
        showFeedback(incidentFeedbackLabel, "", true);
    }

    @FXML
    private void handleEvidenceBack() {
        selectStep(1);
    }

    @FXML
    private void handleBrowseEvidence() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select evidence");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.txt")
        );
        Stage stage = (Stage) submitWizardButton.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            selectedEvidenceFile = file;
            evidencePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmitWizard() {
        if (!validateSubmissionStep()) {
            return;
        }
        if (registeredVictim == null) {
            showFeedback(submissionFeedbackLabel, "Please complete the identity step first.", false);
            return;
        }

        submissionProgress.setVisible(true);
        submitWizardButton.setDisable(true);

        try {
            int perpetratorId = perpetratorService.createOrGetPerpetrator(
                    cachedIdentifierValue,
                    cachedIdentifierType,
                    cachedAlias
            );
            if (perpetratorId <= 0) {
                showFeedback(submissionFeedbackLabel, "Unable to capture perpetrator details. Please try again.", false);
                return;
            }

            int incidentId = incidentService.createIncident(
                    registeredVictim.getVictimID(),
                    perpetratorId,
                    cachedAttackType.getAttackTypeID(),
                    null,
                    incidentDescriptionField.getText().trim()
            );

            if (incidentId > 0) {
                if (selectedEvidenceFile != null) {
                    String evidenceType = detectEvidenceType(selectedEvidenceFile.getName());
                    evidenceService.uploadEvidence(incidentId, evidenceType, selectedEvidenceFile.getAbsolutePath(), null);
                }
                showFeedback(submissionFeedbackLabel, "Report submitted successfully. Incident tracking ID: #" + incidentId, true);
                submitWizardButton.setDisable(true);
            } else {
                showFeedback(submissionFeedbackLabel, "We could not file the report. Please retry.", false);
                submitWizardButton.setDisable(false);
            }
        } finally {
            submissionProgress.setVisible(false);
        }
    }

    @FXML
    private void handleBackToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) stepper.getScene().getWindow();
        Scene scene = new Scene(root, 1080, 720);
        scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        stage.setScene(scene);
    }

    private void configureStepper() {
        List<Tab> tabs = stepper.getTabs();
        for (int i = 1; i < tabs.size(); i++) {
            tabs.get(i).setDisable(true);
        }
        stepper.getSelectionModel().selectedIndexProperty()
                .addListener((obs, oldIndex, newIndex) -> updateStepTitle(newIndex.intValue()));
        updateStepTitle(0);
    }

    private void configureControls() {
        identifierTypeField.setItems(FXCollections.observableArrayList(IDENTIFIER_TYPES));
        identifierTypeField.getSelectionModel().selectFirst();

        attackTypeField.setConverter(new StringConverter<>() {
            @Override
            public String toString(AttackType type) {
                return type != null ? type.getAttackName() : "";
            }

            @Override
            public AttackType fromString(String string) {
                return attackTypeField.getItems().stream()
                        .filter(type -> type.getAttackName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void loadAttackTypes() {
        attackTypeField.getItems().setAll(attackTypeService.getAllAttackTypes());
        if (!attackTypeField.getItems().isEmpty()) {
            attackTypeField.getSelectionModel().selectFirst();
        }
    }

    private boolean validateIdentityStep() {
        String name = victimNameField.getText();
        String email = victimEmailField.getText();
        String confirm = victimEmailConfirmField.getText();

        if (!ValidationUtils.isNotEmpty(name)) {
            showFeedback(identityFeedbackLabel, "Enter your full name to continue.", false);
            return false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showFeedback(identityFeedbackLabel, "Provide a valid contact email address.", false);
            return false;
        }
        if (!email.equalsIgnoreCase(confirm != null ? confirm.trim() : "")) {
            showFeedback(identityFeedbackLabel, "Email addresses do not match.", false);
            return false;
        }
        return true;
    }

    private boolean validateIncidentStep() {
        if (registeredVictim == null) {
            showFeedback(incidentFeedbackLabel, "Complete the identity step before detailing the incident.", false);
            return false;
        }
        if (identifierTypeField.getValue() == null) {
            showFeedback(incidentFeedbackLabel, "Select the identifier type.", false);
            return false;
        }
        if (!ValidationUtils.isNotEmpty(identifierValueField.getText())) {
            showFeedback(incidentFeedbackLabel, "Provide the perpetrator identifier.", false);
            return false;
        }
        if (attackTypeField.getValue() == null) {
            showFeedback(incidentFeedbackLabel, "Choose an attack classification.", false);
            return false;
        }
        return true;
    }

    private boolean validateSubmissionStep() {
        String description = incidentDescriptionField.getText();
        if (!ValidationUtils.isNotEmpty(description) || description.trim().length() < 25) {
            showFeedback(submissionFeedbackLabel, "Describe the incident with at least 25 characters.", false);
            return false;
        }
        return true;
    }

    private void updateStepTitle(int index) {
        switch (index) {
            case 0 -> stepTitleLabel.setText("Step 1 · Victim Identity");
            case 1 -> stepTitleLabel.setText("Step 2 · Incident Details");
            case 2 -> stepTitleLabel.setText("Step 3 · Evidence & Submit");
            default -> stepTitleLabel.setText("Malicious Attack Reporting");
        }
    }

    private void enableStep(int index) {
        if (index < stepper.getTabs().size()) {
            stepper.getTabs().get(index).setDisable(false);
        }
    }

    private void selectStep(int index) {
        if (index < stepper.getTabs().size()) {
            stepper.getSelectionModel().select(index);
        }
    }

    private void showFeedback(Label label, String message, boolean success) {
        if (label == null) return;
        label.getStyleClass().setAll(success ? "success-label" : "error-label");
        label.setText(message);
        label.setVisible(message != null && !message.isBlank());
    }

    private void clearFeedback(Label label) {
        if (label == null) return;
        label.setText("");
        label.setVisible(false);
    }

    private String detectEvidenceType(String filename) {
        if (filename == null) {
            return "Document";
        }
        String lower = filename.toLowerCase(Locale.ENGLISH);
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "Screenshot";
        }
        if (lower.endsWith(".pdf")) {
            return "PDF";
        }
        if (lower.endsWith(".mp4") || lower.endsWith(".mov")) {
            return "Video";
        }
        return "Document";
    }
}


