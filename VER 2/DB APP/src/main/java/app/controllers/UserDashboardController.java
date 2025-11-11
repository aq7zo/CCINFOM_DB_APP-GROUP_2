package app.controllers;

import app.config.SessionManager;
import app.models.AttackType;
import app.models.IncidentReport;
import app.models.Perpetrator;
import app.models.Victim;
import app.services.AttackTypeService;
import app.services.AuthService;
import app.services.EvidenceService;
import app.services.IncidentService;
import app.services.PerpetratorService;
import app.utils.DateUtils;
import app.utils.ValidationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Revised controller for the Victim (public user) dashboard.
 * Presents a guided reporting experience, live status tracker, and alert center.
 */
public class UserDashboardController {
    private static final String[] IDENTIFIER_OPTIONS = {
            "Email Address",
            "Phone Number",
            "Website URL",
            "Social Media Account",
            "Messaging Handle",
            "Cryptocurrency Wallet",
            "IP Address"
    };

    private static final String[] STATUS_FILTERS = {
            "All Statuses",
            "Pending",
            "Validated",
            "Resolved",
            "Escalated"
    };

    @FXML private Label welcomeLabel;
    @FXML private Label accountStatusBadge;
    @FXML private Label lastLoginLabel;
    @FXML private Label openReportsValue;
    @FXML private Label openReportsTrend;
    @FXML private Label validatedReportsValue;
    @FXML private Label validatedReportsTrend;
    @FXML private Label escalationAlertsValue;
    @FXML private Label escalationAlertsTrend;
    @FXML private Label statusSummaryLabel;
    @FXML private Label historyStatsLabel;
    @FXML private Label notificationSummaryLabel;
    @FXML private Label reportFeedbackLabel;

    @FXML private TextField reportNameField;
    @FXML private TextField reportEmailField;
    @FXML private ComboBox<String> identifierTypeCombo;
    @FXML private TextField identifierField;
    @FXML private TextField associatedNameField;
    @FXML private ComboBox<AttackType> attackTypeCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField evidencePathField;
    @FXML private ComboBox<String> statusFilterCombo;

    @FXML private ProgressIndicator submitProgressIndicator;
    @FXML private ProgressIndicator statusLoadingIndicator;

    @FXML private Button submitReportButton;
    @FXML private Button browseEvidenceButton;
    @FXML private Button refreshDashboardButton;
    @FXML private Button refreshStatusButton;
    @FXML private Button logoutButton;

    @FXML private TableView<IncidentRow> statusTable;
    @FXML private TableColumn<IncidentRow, Number> incidentIdColumn;
    @FXML private TableColumn<IncidentRow, String> reportedOnColumn;
    @FXML private TableColumn<IncidentRow, String> attackColumn;
    @FXML private TableColumn<IncidentRow, String> statusColumn;
    @FXML private TableColumn<IncidentRow, String> threatLevelColumn;
    @FXML private TableColumn<IncidentRow, String> lastUpdatedColumn;

    @FXML private TableView<IncidentRow> historyTable;
    @FXML private TableColumn<IncidentRow, Number> historyIdColumn;
    @FXML private TableColumn<IncidentRow, String> historyAttackColumn;
    @FXML private TableColumn<IncidentRow, String> historyStatusColumn;
    @FXML private TableColumn<IncidentRow, String> historyDateColumn;
    @FXML private TableColumn<IncidentRow, String> historySummaryColumn;

    @FXML private ListView<String> notificationsList;

    private final AuthService authService = new AuthService();
    private final IncidentService incidentService = new IncidentService();
    private final AttackTypeService attackTypeService = new AttackTypeService();
    private final PerpetratorService perpetratorService = new PerpetratorService();
    private final EvidenceService evidenceService = new EvidenceService();

    private final ObservableList<IncidentRow> masterIncidentRows = FXCollections.observableArrayList();
    private final ObservableList<IncidentRow> filteredIncidentRows = FXCollections.observableArrayList();
    private final ObservableList<IncidentRow> historyRows = FXCollections.observableArrayList();
    private final ObservableList<String> notificationItems = FXCollections.observableArrayList();
    private final Map<Integer, AttackType> attackTypeIndex = new HashMap<>();

    private File selectedEvidenceFile;
    private LocalDateTime sessionLoginTime;

    @FXML
    private void initialize() {
        sessionLoginTime = DateUtils.now();
        configureControls();
        populateStaticData();
        loadAttackTypes();
        loadVictimContext();
        refreshDashboard();
    }

    @FXML
    private void refreshDashboard() {
        toggleStatusLoading(true);
        try {
            loadIncidentData();
            applyStatusFilter();
            updateSummaries();
            updateNotifications();
        } finally {
            toggleStatusLoading(false);
        }
    }

    @FXML
    private void refreshIncidentStatus() {
        refreshDashboard();
    }

    @FXML
    private void handleBrowseEvidence() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select evidence file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.txt")
        );

        Stage stage = (Stage) browseEvidenceButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedEvidenceFile = file;
            evidencePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmitReport() {
        if (!validateReportForm()) {
            return;
        }

        Victim victim = SessionManager.getInstance().getVictim();
        if (victim == null) {
            showAlert("Session Error", "We cannot determine your victim profile. Please log in again.");
            return;
        }

        submitProgressIndicator.setVisible(true);
        submitReportButton.setDisable(true);

        try {
            String identifierType = identifierTypeCombo.getValue();
            String identifier = identifierField.getText().trim();
            String alias = associatedNameField.getText() != null ? associatedNameField.getText().trim() : "";
            AttackType attackType = attackTypeCombo.getValue();
            String description = descriptionArea.getText().trim();

            int perpetratorId = perpetratorService.createOrGetPerpetrator(identifier, identifierType, alias);
            if (perpetratorId <= 0) {
                showReportFeedback("We could not register the perpetrator details. Please verify the identifier.", false);
                return;
            }

            int incidentId = incidentService.createIncident(
                    victim.getVictimID(),
                    perpetratorId,
                    attackType.getAttackTypeID(),
                    null,
                    description
            );

            if (incidentId > 0) {
                if (selectedEvidenceFile != null) {
                    String evidenceType = detectEvidenceType(selectedEvidenceFile.getName());
                    evidenceService.uploadEvidence(incidentId, evidenceType, selectedEvidenceFile.getAbsolutePath(), null);
                }

                showReportFeedback("Incident submitted successfully. Tracking ID: #" + incidentId, true);
                clearReportForm();
                refreshDashboard();
            } else {
                showReportFeedback("Unable to submit the incident right now. Please try again shortly.", false);
            }
        } finally {
            submitProgressIndicator.setVisible(false);
            submitReportButton.setDisable(false);
        }
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 1080, 720);
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Cybersecurity Incident Reporting System");
        } catch (Exception e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void configureControls() {
        identifierTypeCombo.setItems(FXCollections.observableArrayList(IDENTIFIER_OPTIONS));
        identifierTypeCombo.getSelectionModel().selectFirst();

        statusFilterCombo.setItems(FXCollections.observableArrayList(STATUS_FILTERS));
        statusFilterCombo.getSelectionModel().selectFirst();
        statusFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> applyStatusFilter());

        attackTypeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(AttackType attackType) {
                return attackType != null ? attackType.getAttackName() : "";
            }

            @Override
            public AttackType fromString(String string) {
                return attackTypeCombo.getItems().stream()
                        .filter(type -> type.getAttackName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        statusTable.setPlaceholder(new Label("No incident reports submitted yet."));
        historyTable.setPlaceholder(new Label("Your incident history will appear here."));
        notificationsList.setPlaceholder(new Label("No alerts or guidance at the moment."));

        configureTableColumns();
    }

    private void populateStaticData() {
        Victim victim = SessionManager.getInstance().getVictim();
        if (victim != null) {
            reportNameField.setText(victim.getName());
            reportEmailField.setText(victim.getContactEmail());
        }

        lastLoginLabel.setText("Session started: " + DateUtils.formatForDisplay(sessionLoginTime));
    }

    private void configureTableColumns() {
        incidentIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIncidentId()));
        reportedOnColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReportedOn()));
        attackColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAttackType()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        threatLevelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getThreatLevel()));
        lastUpdatedColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastUpdated()));

        historyIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIncidentId()));
        historyAttackColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAttackType()));
        historyStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        historyDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReportedOn()));
        historySummaryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSummary()));

        statusColumn.setCellFactory(column -> new StatusChipCell());
        historyStatusColumn.setCellFactory(column -> new StatusChipCell());
        threatLevelColumn.setCellFactory(column -> new ThreatLevelCell());
    }

    private void loadAttackTypes() {
        List<AttackType> attackTypes = attackTypeService.getAllAttackTypes();
        attackTypeIndex.clear();
        for (AttackType type : attackTypes) {
            attackTypeIndex.put(type.getAttackTypeID(), type);
        }
        attackTypeCombo.getItems().setAll(attackTypes);
        if (!attackTypes.isEmpty()) {
            attackTypeCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadVictimContext() {
        Victim victim = SessionManager.getInstance().getVictim();
        if (victim == null) {
            welcomeLabel.setText("Welcome – guest session");
            return;
        }

        welcomeLabel.setText("Welcome, " + victim.getName());
        updateAccountStatusBadge(victim.getAccountStatus());
    }

    private void loadIncidentData() {
        Victim victim = SessionManager.getInstance().getVictim();
        if (victim == null) {
            masterIncidentRows.clear();
            historyRows.clear();
            return;
        }

        List<IncidentReport> incidents = incidentService.getIncidentsByVictim(victim.getVictimID());

        masterIncidentRows.setAll(
                incidents.stream()
                        .map(this::toIncidentRow)
                        .collect(Collectors.toList())
        );
        historyRows.setAll(masterIncidentRows);
        historyTable.setItems(historyRows);
    }

    private void applyStatusFilter() {
        String selected = statusFilterCombo.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("All Statuses")) {
            filteredIncidentRows.setAll(masterIncidentRows);
        } else {
            filteredIncidentRows.setAll(
                    masterIncidentRows.stream()
                            .filter(row -> row.getStatus().equalsIgnoreCase(selected))
                            .collect(Collectors.toList())
            );
        }
        statusTable.setItems(filteredIncidentRows);
        statusSummaryLabel.setText(filteredIncidentRows.isEmpty()
                ? "No incidents match the selected filter."
                : filteredIncidentRows.size() + " incident(s) currently displayed.");
    }

    private void updateSummaries() {
        if (masterIncidentRows.isEmpty()) {
            openReportsValue.setText("0");
            validatedReportsValue.setText("0");
            escalationAlertsValue.setText("0");
            openReportsTrend.setText("Start by submitting your first report.");
            validatedReportsTrend.setText("Validation metrics appear after submissions.");
            escalationAlertsTrend.setText("No escalations detected");
            historyStatsLabel.setText("No archived incidents yet.");
            return;
        }

        long pendingCount = masterIncidentRows.stream()
                .filter(row -> row.getStatus().equalsIgnoreCase("Pending"))
                .count();
        long validatedCount = masterIncidentRows.stream()
                .filter(row -> row.getStatus().equalsIgnoreCase("Validated"))
                .count();
        long escalatedCount = masterIncidentRows.stream()
                .filter(row -> row.getThreatLevel().equalsIgnoreCase("Malicious"))
                .count();

        openReportsValue.setText(String.valueOf(pendingCount));
        validatedReportsValue.setText(String.valueOf(validatedCount));
        escalationAlertsValue.setText(String.valueOf(escalatedCount));

        openReportsTrend.setText(pendingCount > 0
                ? "Pending items awaiting analyst validation."
                : "All reported incidents are in review or resolved.");
        validatedReportsTrend.setText(validatedCount > 0
                ? validatedCount + " case(s) validated to date."
                : "No validated cases yet.");
        escalationAlertsTrend.setText(escalatedCount > 0
                ? "Stay vigilant. Threat escalation detected."
                : "No escalations detected");

        historyStatsLabel.setText(masterIncidentRows.size() + " total submissions tracked.");
    }

    private void updateNotifications() {
        notificationItems.clear();
        if (masterIncidentRows.isEmpty()) {
            notificationSummaryLabel.setText("We will notify you if any repeated targeting or escalations are detected.");
            notificationsList.setItems(notificationItems);
            return;
        }

        List<String> alerts = new ArrayList<>();
        masterIncidentRows.stream()
                .filter(row -> row.getStatus().equalsIgnoreCase("Validated"))
                .findFirst()
                .ifPresent(row -> alerts.add("Incident #" + row.getIncidentId() + " has been validated. Expect further guidance via email."));

        masterIncidentRows.stream()
                .filter(row -> row.getThreatLevel().equalsIgnoreCase("Malicious"))
                .forEach(row -> alerts.add("Threat escalation: Perpetrator in incident #" + row.getIncidentId() + " flagged as malicious."));

        if (alerts.isEmpty()) {
            alerts.add("No active alerts right now. We will update you if analysts flag related threats.");
            notificationSummaryLabel.setText("Monitoring continues. No escalation patterns detected.");
        } else {
            notificationSummaryLabel.setText("Review the latest intelligence for your submissions.");
        }

        notificationItems.setAll(alerts);
        notificationsList.setItems(notificationItems);
    }

    private boolean validateReportForm() {
        String name = reportNameField.getText();
        String email = reportEmailField.getText();
        String identifier = identifierField.getText();
        AttackType attackType = attackTypeCombo.getValue();
        String description = descriptionArea.getText();

        if (!ValidationUtils.isNotEmpty(name)) {
            showReportFeedback("Enter the full name for the reporting party.", false);
            return false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showReportFeedback("Provide a valid contact email so our analysts can reach you.", false);
            return false;
        }
        if (!ValidationUtils.isNotEmpty(identifier)) {
            showReportFeedback("Supply the perpetrator identifier (email, number, URL, etc.).", false);
            return false;
        }
        if (attackType == null) {
            showReportFeedback("Select the attack classification so we can route the report.", false);
            return false;
        }
        if (!ValidationUtils.isNotEmpty(description) || description.trim().length() < 25) {
            showReportFeedback("Describe the incident with at least 25 characters to assist the investigation.", false);
            return false;
        }
        clearReportFeedback();
        return true;
    }

    private void showReportFeedback(String message, boolean success) {
        reportFeedbackLabel.getStyleClass().setAll(success ? "success-label" : "error-label");
        reportFeedbackLabel.setText(message);
        reportFeedbackLabel.setVisible(true);
    }

    private void clearReportFeedback() {
        reportFeedbackLabel.setText("");
        reportFeedbackLabel.setVisible(false);
    }

    private void clearReportForm() {
        identifierField.clear();
        associatedNameField.clear();
        descriptionArea.clear();
        evidencePathField.clear();
        selectedEvidenceFile = null;
        identifierTypeCombo.getSelectionModel().selectFirst();
        if (!attackTypeCombo.getItems().isEmpty()) {
            attackTypeCombo.getSelectionModel().selectFirst();
        }
    }

    private void toggleStatusLoading(boolean isLoading) {
        statusLoadingIndicator.setVisible(isLoading);
        statusTable.setDisable(isLoading);
    }

    private void updateAccountStatusBadge(String status) {
        if (status == null) {
            status = "Active";
        }
        String normalized = status.substring(0, 1).toUpperCase(Locale.ENGLISH) + status.substring(1).toLowerCase(Locale.ENGLISH);
        accountStatusBadge.getStyleClass().setAll("tag");
        switch (normalized) {
            case "Flagged" -> accountStatusBadge.getStyleClass().add("badge-warning");
            case "Suspended" -> accountStatusBadge.getStyleClass().add("badge-danger");
            default -> accountStatusBadge.getStyleClass().add("badge-success");
        }
        accountStatusBadge.setText("Account " + normalized);
    }

    private IncidentRow toIncidentRow(IncidentReport report) {
        AttackType attackType = attackTypeIndex.get(report.getAttackTypeID());
        Perpetrator perpetrator = perpetratorService.getPerpetratorById(report.getPerpetratorID());

        String attackName = attackType != null ? attackType.getAttackName() : "Unclassified";
        String severity = attackType != null ? attackType.getSeverityLevel() : "Unknown";
        String reportedOn = DateUtils.formatForDisplay(report.getDateReported());
        String threatLevel = perpetrator != null ? perpetrator.getThreatLevel() : "UnderReview";

        return new IncidentRow(
                report.getIncidentID(),
                reportedOn,
                attackName,
                report.getStatus(),
                threatLevel,
                reportedOn,
                buildSummary(report.getDescription(), severity)
        );
    }

    private String buildSummary(String description, String severity) {
        if (description == null || description.isBlank()) {
            return "No description recorded.";
        }
        String sanitized = description.replaceAll("\\s+", " ").trim();
        String prefix = "(" + severity + ") ";
        return sanitized.length() > 110
                ? prefix + sanitized.substring(0, 107) + "..."
                : prefix + sanitized;
    }

    private String detectEvidenceType(String filename) {
        if (filename == null) {
            return "Document";
        }
        String lowercase = filename.toLowerCase(Locale.ENGLISH);
        if (lowercase.endsWith(".png") || lowercase.endsWith(".jpg") || lowercase.endsWith(".jpeg")) {
            return "Screenshot";
        } else if (lowercase.endsWith(".pdf")) {
            return "PDF";
        } else if (lowercase.endsWith(".mp4") || lowercase.endsWith(".mov")) {
            return "Video";
        }
        return "Document";
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Table row projection for incident summaries.
     */
    public static class IncidentRow {
        private final int incidentId;
        private final String reportedOn;
        private final String attackType;
        private final String status;
        private final String threatLevel;
        private final String lastUpdated;
        private final String summary;

        public IncidentRow(int incidentId,
                           String reportedOn,
                           String attackType,
                           String status,
                           String threatLevel,
                           String lastUpdated,
                           String summary) {
            this.incidentId = incidentId;
            this.reportedOn = reportedOn;
            this.attackType = attackType;
            this.status = status;
            this.threatLevel = threatLevel;
            this.lastUpdated = lastUpdated;
            this.summary = summary;
        }

        public int getIncidentId() {
            return incidentId;
        }

        public String getReportedOn() {
            return reportedOn;
        }

        public String getAttackType() {
            return attackType;
        }

        public String getStatus() {
            return status;
        }

        public String getThreatLevel() {
            return threatLevel;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public String getSummary() {
            return summary;
        }
    }

    private static class StatusChipCell extends TableCell<IncidentRow, String> {
        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            if (empty || status == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            Label chip = new Label(status);
            chip.getStyleClass().add("status-chip");
            switch (status.toLowerCase(Locale.ENGLISH)) {
                case "validated" -> chip.getStyleClass().add("validated");
                case "resolved" -> chip.getStyleClass().add("resolved");
                case "escalated" -> chip.getStyleClass().add("escalated");
                default -> chip.getStyleClass().add("pending");
            }
            setGraphic(chip);
            setText(null);
        }
    }

    private static class ThreatLevelCell extends TableCell<IncidentRow, String> {
        @Override
        protected void updateItem(String level, boolean empty) {
            super.updateItem(level, empty);
            if (empty || level == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            Label chip = new Label(level);
            chip.getStyleClass().add("status-chip");
            switch (level.toLowerCase(Locale.ENGLISH)) {
                case "malicious" -> chip.getStyleClass().add("escalated");
                case "suspected" -> chip.getStyleClass().add("pending");
                case "cleared" -> chip.getStyleClass().add("validated");
                default -> chip.getStyleClass().add("resolved");
            }
            setGraphic(chip);
            setText(null);
        }
    }
}

