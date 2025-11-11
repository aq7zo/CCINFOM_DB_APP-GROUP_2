package app.controllers;

import app.config.SessionManager;
import app.models.AttackType;
import app.models.Evidence;
import app.models.IncidentReport;
import app.models.Perpetrator;
import app.models.Victim;
import app.services.AttackTypeService;
import app.services.AuthService;
import app.services.EvidenceService;
import app.services.IncidentService;
import app.services.PerpetratorService;
import app.services.VictimService;
import app.utils.DateUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Modernized administrator dashboard controller with threat intelligence views.
 */
public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label lastSyncLabel;
    @FXML private Label totalReportsValue;
    @FXML private Label totalReportsTrend;
    @FXML private Label activePerpetratorsValue;
    @FXML private Label activePerpetratorsTrend;
    @FXML private Label flaggedVictimsValue;
    @FXML private Label flaggedVictimsTrend;
    @FXML private Label verifiedEvidenceValue;
    @FXML private Label verifiedEvidenceTrend;
    @FXML private Label incidentQueueSummary;

    @FXML private Button syncNowButton;
    @FXML private Button logoutButton;
    @FXML private Button validateSelectedButton;
    @FXML private Button rejectSelectedButton;
    @FXML private Button escalateThreatButton;
    @FXML private Button flagVictimButton;
    @FXML private Button reinstateVictimButton;
    @FXML private Button verifyEvidenceButton;
    @FXML private Button rejectEvidenceButton;
    @FXML private Button generatePdfButton;
    @FXML private Button exportCsvButton;
    @FXML private Button saveIntelNotesButton;

    @FXML private ComboBox<String> incidentStatusFilter;
    @FXML private ComboBox<String> threatLevelFilter;
    @FXML private ComboBox<String> victimStatusFilter;
    @FXML private ComboBox<String> reportMonthSelector;
    @FXML private ComboBox<String> reportYearSelector;

    @FXML private TableView<IncidentRow> incidentTable;
    @FXML private TableColumn<IncidentRow, String> incIdColumn;
    @FXML private TableColumn<IncidentRow, String> incVictimColumn;
    @FXML private TableColumn<IncidentRow, String> incPerpColumn;
    @FXML private TableColumn<IncidentRow, String> incAttackColumn;
    @FXML private TableColumn<IncidentRow, String> incStatusColumn;
    @FXML private TableColumn<IncidentRow, String> incDateColumn;
    @FXML private TableColumn<IncidentRow, String> incEvidenceColumn;

    @FXML private TableView<PerpetratorRow> perpetratorTable;
    @FXML private TableColumn<PerpetratorRow, String> perpIdColumn;
    @FXML private TableColumn<PerpetratorRow, String> perpIdentifierColumn;
    @FXML private TableColumn<PerpetratorRow, String> perpTypeColumn;
    @FXML private TableColumn<PerpetratorRow, String> perpThreatColumn;
    @FXML private TableColumn<PerpetratorRow, String> perpLastIncidentColumn;
    @FXML private TableColumn<PerpetratorRow, String> perpLinkedVictimsColumn;

    @FXML private TableView<VictimRow> victimTable;
    @FXML private TableColumn<VictimRow, String> victimIdColumn;
    @FXML private TableColumn<VictimRow, String> victimNameColumn;
    @FXML private TableColumn<VictimRow, String> victimEmailColumn;
    @FXML private TableColumn<VictimRow, String> victimStatusColumn;
    @FXML private TableColumn<VictimRow, String> victimReportsColumn;
    @FXML private TableColumn<VictimRow, String> victimCreatedColumn;

    @FXML private TableView<EvidenceRow> evidenceTable;
    @FXML private TableColumn<EvidenceRow, String> evidenceIdColumn;
    @FXML private TableColumn<EvidenceRow, String> evidenceIncidentColumn;
    @FXML private TableColumn<EvidenceRow, String> evidenceTypeColumn;
    @FXML private TableColumn<EvidenceRow, String> evidencePathColumn;
    @FXML private TableColumn<EvidenceRow, String> evidenceStatusColumn;
    @FXML private TableColumn<EvidenceRow, String> evidenceSubmittedColumn;

    @FXML private BarChart<String, Number> monthlyTrendChart;
    @FXML private TextArea intelNotesArea;
    @FXML private ListView<String> intelInsightsList;

    private final AuthService authService = new AuthService();
    private final IncidentService incidentService = new IncidentService();
    private final PerpetratorService perpetratorService = new PerpetratorService();
    private final VictimService victimService = new VictimService();
    private final EvidenceService evidenceService = new EvidenceService();
    private final AttackTypeService attackTypeService = new AttackTypeService();

    private final ObservableList<IncidentRow> incidentRows = FXCollections.observableArrayList();
    private final ObservableList<PerpetratorRow> perpetratorRows = FXCollections.observableArrayList();
    private final ObservableList<VictimRow> victimRows = FXCollections.observableArrayList();
    private final ObservableList<EvidenceRow> evidenceRows = FXCollections.observableArrayList();

    private final Map<Integer, AttackType> attackTypeCache = new HashMap<>();
    private LocalDateTime lastSyncTimestamp;

    @FXML
    private void initialize() {
        configureHeader();
        configureFilters();
        configureTables();
        hydrateCaches();
        refreshDashboard();
    }

    @FXML
    private void handleSync() {
        refreshDashboard();
        showAlert(Alert.AlertType.INFORMATION, "Data refreshed", "All metrics and queues have been synchronized.");
    }

    @FXML
    private void handleValidateSelectedIncident() {
        IncidentRow selected = incidentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No incident selected", "Choose an incident to validate.");
            return;
        }
        SessionManager session = SessionManager.getInstance();
        if (session.getAdmin() == null) {
            showAlert(Alert.AlertType.ERROR, "Session error", "Administrator session missing.");
            return;
        }
        boolean success = incidentService.validateIncident(selected.getIncidentId(), session.getAdmin().getAdminID());
        if (success) {
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Incident validated", "Incident #" + selected.getIncidentId() + " has been marked as validated.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Validation failed", "Unable to validate the selected incident. Please retry.");
        }
    }

    @FXML
    private void handleRejectSelectedIncident() {
        IncidentRow selected = incidentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No incident selected", "Choose an incident to reject.");
            return;
        }
        SessionManager session = SessionManager.getInstance();
        Integer adminId = session.getAdmin() != null ? session.getAdmin().getAdminID() : null;
        boolean success = incidentService.updateIncidentStatus(selected.getIncidentId(), "Rejected", adminId);
        if (success) {
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Incident rejected", "Incident #" + selected.getIncidentId() + " has been marked as rejected.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Action failed", "Unable to update the incident status.");
        }
    }

    @FXML
    private void handleEscalateThreat() {
        PerpetratorRow selected = perpetratorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No perpetrator selected", "Select a perpetrator to escalate.");
            return;
        }
        SessionManager session = SessionManager.getInstance();
        Integer adminId = session.getAdmin() != null ? session.getAdmin().getAdminID() : null;
        boolean success = perpetratorService.updateThreatLevel(selected.getPerpetratorId(), "Malicious", adminId);
        if (success) {
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Threat escalated", "Threat level escalated to Malicious.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Action failed", "Unable to escalate threat level.");
        }
    }

    @FXML
    private void handleFlagVictim() {
        VictimRow selected = victimTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No victim selected", "Select a victim to flag.");
            return;
        }
        SessionManager session = SessionManager.getInstance();
        Integer adminId = session.getAdmin() != null ? session.getAdmin().getAdminID() : null;
        boolean success = victimService.updateVictimStatus(selected.getVictimId(), "Flagged", adminId);
        if (success) {
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Victim flagged", "Victim account has been flagged for review.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Action failed", "Unable to flag the victim account.");
        }
    }

    @FXML
    private void handleReinstateVictim() {
        VictimRow selected = victimTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No victim selected", "Select a victim to reinstate.");
            return;
        }
        SessionManager session = SessionManager.getInstance();
        Integer adminId = session.getAdmin() != null ? session.getAdmin().getAdminID() : null;
        boolean success = victimService.updateVictimStatus(selected.getVictimId(), "Active", adminId);
        if (success) {
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Victim reinstated", "Victim account has been returned to Active status.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Action failed", "Unable to reinstate the victim account.");
        }
    }

    @FXML
    private void handleVerifyEvidence() {
        EvidenceRow selected = evidenceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No evidence selected", "Select an evidence record to verify.");
            return;
        }
        SessionManager session = SessionManager.getInstance();
        Integer adminId = session.getAdmin() != null ? session.getAdmin().getAdminID() : null;
        boolean success = evidenceService.updateEvidenceStatus(selected.getEvidenceId(), "Verified", adminId);
        if (success) {
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Evidence verified", "Evidence #" + selected.getEvidenceId() + " marked as verified.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Action failed", "Unable to verify the evidence record.");
        }
    }

    @FXML
    private void handleRejectEvidence() {
        EvidenceRow selected = evidenceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No evidence selected", "Select an evidence record to reject.");
            return;
        }
        SessionManager session = SessionManager.getInstance();
        Integer adminId = session.getAdmin() != null ? session.getAdmin().getAdminID() : null;
        boolean success = evidenceService.updateEvidenceStatus(selected.getEvidenceId(), "Rejected", adminId);
        if (success) {
            refreshDashboard();
            showAlert(Alert.AlertType.INFORMATION, "Evidence rejected", "Evidence #" + selected.getEvidenceId() + " marked as rejected.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Action failed", "Unable to update the evidence record.");
        }
    }

    @FXML
    private void handleGeneratePdf() {
        showAlert(Alert.AlertType.INFORMATION, "PDF generation", "PDF export is queued. (Mock implementation)");
    }

    @FXML
    private void handleExportCsv() {
        showAlert(Alert.AlertType.INFORMATION, "CSV export", "CSV export has been generated. (Mock implementation)");
    }

    @FXML
    private void handleSaveIntelNotes() {
        String notes = intelNotesArea.getText();
        if (notes == null || notes.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "No notes", "Enter briefing notes before saving.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Notes saved", "Intelligence notes captured for the current interval.");
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void configureHeader() {
        SessionManager session = SessionManager.getInstance();
        if (session.getAdmin() != null) {
            welcomeLabel.setText("Welcome, " + session.getAdmin().getName() + " (" + session.getAdmin().getRole() + ")");
        } else {
            welcomeLabel.setText("Welcome, Administrator");
        }
        updateLastSyncLabel();
    }

    private void configureFilters() {
        incidentStatusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Validated", "Rejected", "Resolved"));
        incidentStatusFilter.getSelectionModel().selectFirst();
        incidentStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> applyIncidentFilter());

        threatLevelFilter.setItems(FXCollections.observableArrayList("All", "UnderReview", "Suspected", "Malicious", "Cleared"));
        threatLevelFilter.getSelectionModel().selectFirst();
        threatLevelFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> applyPerpetratorFilter());

        victimStatusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Flagged", "Suspended"));
        victimStatusFilter.getSelectionModel().selectFirst();
        victimStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> applyVictimFilter());

        LocalDate today = LocalDate.now();
        List<String> months = List.of(Month.values()).stream()
                .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .collect(Collectors.toList());
        reportMonthSelector.setItems(FXCollections.observableArrayList(months));
        reportMonthSelector.getSelectionModel().select(today.getMonth().getValue() - 1);

        int currentYear = today.getYear();
        ObservableList<String> years = FXCollections.observableArrayList();
        for (int i = 0; i < 5; i++) {
            years.add(String.valueOf(currentYear - i));
        }
        reportYearSelector.setItems(years);
        reportYearSelector.getSelectionModel().selectFirst();
    }

    private void configureTables() {
        incIdColumn.setCellValueFactory(cell -> cell.getValue().incidentIdProperty());
        incVictimColumn.setCellValueFactory(cell -> cell.getValue().victimProperty());
        incPerpColumn.setCellValueFactory(cell -> cell.getValue().perpetratorProperty());
        incAttackColumn.setCellValueFactory(cell -> cell.getValue().attackTypeProperty());
        incStatusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        incDateColumn.setCellValueFactory(cell -> cell.getValue().reportedOnProperty());
        incEvidenceColumn.setCellValueFactory(cell -> cell.getValue().evidenceSummaryProperty());
        incidentTable.setItems(incidentRows);

        perpIdColumn.setCellValueFactory(cell -> cell.getValue().perpetratorIdProperty());
        perpIdentifierColumn.setCellValueFactory(cell -> cell.getValue().identifierProperty());
        perpTypeColumn.setCellValueFactory(cell -> cell.getValue().identifierTypeProperty());
        perpThreatColumn.setCellValueFactory(cell -> cell.getValue().threatLevelProperty());
        perpLastIncidentColumn.setCellValueFactory(cell -> cell.getValue().lastIncidentProperty());
        perpLinkedVictimsColumn.setCellValueFactory(cell -> cell.getValue().linkedVictimsProperty());
        perpetratorTable.setItems(perpetratorRows);

        victimIdColumn.setCellValueFactory(cell -> cell.getValue().victimIdProperty());
        victimNameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        victimEmailColumn.setCellValueFactory(cell -> cell.getValue().emailProperty());
        victimStatusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        victimReportsColumn.setCellValueFactory(cell -> cell.getValue().reportsProperty());
        victimCreatedColumn.setCellValueFactory(cell -> cell.getValue().createdProperty());
        victimTable.setItems(victimRows);

        evidenceIdColumn.setCellValueFactory(cell -> cell.getValue().evidenceIdProperty());
        evidenceIncidentColumn.setCellValueFactory(cell -> cell.getValue().incidentIdProperty());
        evidenceTypeColumn.setCellValueFactory(cell -> cell.getValue().typeProperty());
        evidencePathColumn.setCellValueFactory(cell -> cell.getValue().filePathProperty());
        evidenceStatusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        evidenceSubmittedColumn.setCellValueFactory(cell -> cell.getValue().submittedProperty());
        evidenceTable.setItems(evidenceRows);
    }

    private void hydrateCaches() {
        attackTypeCache.clear();
        attackTypeService.getAllAttackTypes()
                .forEach(type -> attackTypeCache.put(type.getAttackTypeID(), type));
    }

    private void refreshDashboard() {
        lastSyncTimestamp = DateUtils.now();
        updateLastSyncLabel();
        hydrateCaches();
        loadIncidents();
        loadPerpetrators();
        loadVictims();
        loadEvidence();
        updateSummaryCards();
        updateCharts();
        updateInsights();
    }

    private void updateLastSyncLabel() {
        if (lastSyncTimestamp == null) {
            lastSyncLabel.setText("Last sync: Not yet refreshed");
        } else {
            lastSyncLabel.setText("Last sync: " + DateUtils.formatForDisplay(lastSyncTimestamp));
        }
    }

    private void loadIncidents() {
        List<IncidentReport> reports = incidentService.getAllIncidents();
        incidentRows.setAll(reports.stream()
                .map(this::toIncidentRow)
                .sorted(Comparator.comparing(IncidentRow::getReportedOnDate).reversed())
                .collect(Collectors.toList()));
        applyIncidentFilter();
        incidentQueueSummary.setText(incidentRows.isEmpty()
                ? "No incidents queued."
                : incidentRows.size() + " incident(s) in queue.");
    }

    private void loadPerpetrators() {
        List<Perpetrator> perpetrators = perpetratorService.getAllPerpetrators();
        perpetratorRows.setAll(perpetrators.stream()
                .map(this::toPerpetratorRow)
                .collect(Collectors.toList()));
        applyPerpetratorFilter();
    }

    private void loadVictims() {
        List<Victim> victims = victimService.getAllVictims();
        Map<Integer, Long> reportCounts = incidentRows.stream()
                .collect(Collectors.groupingBy(IncidentRow::getVictimId, Collectors.counting()));

        victimRows.setAll(victims.stream()
                .map(victim -> toVictimRow(victim, reportCounts.getOrDefault(victim.getVictimID(), 0L)))
                .collect(Collectors.toList()));
        applyVictimFilter();
    }

    private void loadEvidence() {
        List<Evidence> evidenceList = evidenceService.getAllEvidence();
        evidenceRows.setAll(evidenceList.stream()
                .map(this::toEvidenceRow)
                .collect(Collectors.toList()));
    }

    private void updateSummaryCards() {
        LocalDateTime thirtyDaysAgo = DateUtils.now().minusDays(30);
        long recentReports = incidentRows.stream()
                .filter(row -> row.getReportedOnDate().isAfter(thirtyDaysAgo))
                .count();
        totalReportsValue.setText(String.valueOf(recentReports));
        totalReportsTrend.setText(recentReports + " captured in last 30 days.");

        long activeThreats = perpetratorRows.stream()
                .filter(row -> !row.getThreatLevel().equalsIgnoreCase("Cleared"))
                .count();
        activePerpetratorsValue.setText(String.valueOf(activeThreats));
        activePerpetratorsTrend.setText(activeThreats > 0 ? "Active threats under monitoring." : "No active escalations.");

        long flaggedVictims = victimRows.stream()
                .filter(row -> row.getStatus().equalsIgnoreCase("Flagged"))
                .count();
        flaggedVictimsValue.setText(String.valueOf(flaggedVictims));
        flaggedVictimsTrend.setText(flaggedVictims > 0 ? "Flag review required." : "All victims active.");

        long verifiedEvidence = evidenceRows.stream()
                .filter(row -> row.getStatus().equalsIgnoreCase("Verified"))
                .count();
        verifiedEvidenceValue.setText(String.valueOf(verifiedEvidence));
        verifiedEvidenceTrend.setText(verifiedEvidence + " items verified to date.");
    }

    private void updateCharts() {
        monthlyTrendChart.getData().clear();
        Map<Month, Long> countsByMonth = incidentRows.stream()
                .collect(Collectors.groupingBy(
                        row -> row.getReportedOnDate().getMonth(),
                        Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        countsByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> series.getData().add(new XYChart.Data<>(
                        entry.getKey().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        entry.getValue())));
        monthlyTrendChart.getData().add(series);
    }

    private void updateInsights() {
        ObservableList<String> insights = FXCollections.observableArrayList();
        Optional<String> topAttackType = incidentRows.stream()
                .collect(Collectors.groupingBy(IncidentRow::getAttackType, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> "Top attack vector: " + entry.getKey() + " (" + entry.getValue() + ")");
        topAttackType.ifPresent(insights::add);

        perpetratorRows.stream()
                .filter(row -> row.getThreatLevel().equalsIgnoreCase("Malicious"))
                .findFirst()
                .ifPresent(row -> insights.add("Escalation priority: Perpetrator " + row.getIdentifier() + " is marked Malicious."));

        long pendingEvidence = evidenceRows.stream()
                .filter(row -> row.getStatus().equalsIgnoreCase("Pending"))
                .count();
        insights.add(pendingEvidence + " evidence item(s) awaiting verification.");

        intelInsightsList.setItems(insights);
    }

    private void applyIncidentFilter() {
        String selected = incidentStatusFilter.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("All")) {
            incidentTable.setItems(incidentRows);
        } else {
            ObservableList<IncidentRow> filtered = incidentRows.stream()
                    .filter(row -> row.getStatus().equalsIgnoreCase(selected))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            incidentTable.setItems(filtered);
        }
    }

    private void applyPerpetratorFilter() {
        String selected = threatLevelFilter.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("All")) {
            perpetratorTable.setItems(perpetratorRows);
        } else {
            ObservableList<PerpetratorRow> filtered = perpetratorRows.stream()
                    .filter(row -> row.getThreatLevel().equalsIgnoreCase(selected))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            perpetratorTable.setItems(filtered);
        }
    }

    private void applyVictimFilter() {
        String selected = victimStatusFilter.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("All")) {
            victimTable.setItems(victimRows);
        } else {
            ObservableList<VictimRow> filtered = victimRows.stream()
                    .filter(row -> row.getStatus().equalsIgnoreCase(selected))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            victimTable.setItems(filtered);
        }
    }

    private IncidentRow toIncidentRow(IncidentReport report) {
        Victim victim = victimService.getVictimById(report.getVictimID());
        Perpetrator perpetrator = perpetratorService.getPerpetratorById(report.getPerpetratorID());
        AttackType attackType = attackTypeCache.get(report.getAttackTypeID());
        List<Evidence> evidenceList = evidenceService.getEvidenceByIncident(report.getIncidentID());

        String victimName = victim != null ? victim.getName() : "Unknown Victim";
        String perpetratorDescriptor = perpetrator != null
                ? perpetrator.getIdentifier()
                : "Unknown Perpetrator";
        String attackName = attackType != null ? attackType.getAttackName() : "Unclassified";
        String evidenceSummary = evidenceList.isEmpty()
                ? "None"
                : evidenceList.size() + " item(s)";

        return new IncidentRow(
                report.getIncidentID(),
                report.getVictimID(),
                victimName,
                perpetratorDescriptor,
                attackName,
                report.getPerpetratorID(),
                report.getStatus(),
                DateUtils.formatForDisplay(report.getDateReported()),
                report.getDateReported(),
                evidenceSummary
        );
    }

    private PerpetratorRow toPerpetratorRow(Perpetrator perpetrator) {
        List<IncidentRow> linkedIncidents = incidentRows.stream()
                .filter(row -> Objects.equals(perpetrator.getPerpetratorID(), row.getPerpetratorId()))
                .collect(Collectors.toList());
        String linkedVictims = linkedIncidents.stream()
                .map(IncidentRow::getVictimName)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        if (linkedVictims.isBlank()) {
            linkedVictims = "None";
        }
        return new PerpetratorRow(
                perpetrator.getPerpetratorID(),
                perpetrator.getIdentifier(),
                perpetrator.getIdentifierType(),
                perpetrator.getThreatLevel(),
                perpetrator.getLastIncidentDate() != null ? DateUtils.formatForDisplay(perpetrator.getLastIncidentDate()) : "N/A",
                linkedVictims
        );
    }

    private VictimRow toVictimRow(Victim victim, long reportsFiled) {
        return new VictimRow(
                victim.getVictimID(),
                victim.getName(),
                victim.getContactEmail(),
                victim.getAccountStatus(),
                String.valueOf(reportsFiled),
                victim.getDateCreated() != null ? DateUtils.formatForDisplay(victim.getDateCreated()) : "N/A"
        );
    }

    private EvidenceRow toEvidenceRow(Evidence evidence) {
        return new EvidenceRow(
                evidence.getEvidenceID(),
                evidence.getIncidentID(),
                evidence.getEvidenceType(),
                evidence.getFilePath(),
                evidence.getVerifiedStatus(),
                evidence.getSubmissionDate() != null ? DateUtils.formatForDisplay(evidence.getSubmissionDate()) : "N/A"
        );
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* --------- View Model Classes --------- */

    public static class IncidentRow {
        private final javafx.beans.property.SimpleStringProperty incidentId;
        private final javafx.beans.property.SimpleStringProperty victimName;
        private final javafx.beans.property.SimpleStringProperty perpetrator;
        private final javafx.beans.property.SimpleStringProperty attackType;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty reportedOn;
        private final javafx.beans.property.SimpleStringProperty evidenceSummary;
        private final int incidentIdValue;
        private final int victimId;
        private final int perpetratorId;
        private final LocalDateTime reportedOnDate;

        public IncidentRow(int incidentId,
                           int victimId,
                           String victimName,
                           String perpetrator,
                           String attackType,
                           int perpetratorId,
                           String status,
                           String reportedOn,
                           LocalDateTime reportedOnDate,
                           String evidenceSummary) {
            this.incidentIdValue = incidentId;
            this.victimId = victimId;
            this.perpetratorId = perpetratorId;
            this.victimName = new javafx.beans.property.SimpleStringProperty(victimName);
            this.perpetrator = new javafx.beans.property.SimpleStringProperty(perpetrator);
            this.attackType = new javafx.beans.property.SimpleStringProperty(attackType);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.reportedOn = new javafx.beans.property.SimpleStringProperty(reportedOn);
            this.reportedOnDate = reportedOnDate != null ? reportedOnDate : LocalDateTime.MIN;
            this.evidenceSummary = new javafx.beans.property.SimpleStringProperty(evidenceSummary);
            this.incidentId = new javafx.beans.property.SimpleStringProperty("#" + incidentId);
        }

        public javafx.beans.property.StringProperty incidentIdProperty() {
            return incidentId;
        }

        public javafx.beans.property.StringProperty victimProperty() {
            return victimName;
        }

        public javafx.beans.property.StringProperty perpetratorProperty() {
            return perpetrator;
        }

        public javafx.beans.property.StringProperty attackTypeProperty() {
            return attackType;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        public javafx.beans.property.StringProperty reportedOnProperty() {
            return reportedOn;
        }

        public javafx.beans.property.StringProperty evidenceSummaryProperty() {
            return evidenceSummary;
        }

        public int getIncidentId() {
            return incidentIdValue;
        }

        public int getVictimId() {
            return victimId;
        }

        public String getVictimName() {
            return victimName.get();
        }

        public int getPerpetratorId() {
            return perpetratorId;
        }

        public String getAttackType() {
            return attackType.get();
        }

        public String getStatus() {
            return status.get();
        }

        public String getReportedOn() {
            return reportedOn.get();
        }

        public LocalDateTime getReportedOnDate() {
            return reportedOnDate;
        }
    }

    public static class PerpetratorRow {
        private final int perpetratorIdValue;
        private final javafx.beans.property.SimpleStringProperty perpetratorId;
        private final javafx.beans.property.SimpleStringProperty identifier;
        private final javafx.beans.property.SimpleStringProperty identifierType;
        private final javafx.beans.property.SimpleStringProperty threatLevel;
        private final javafx.beans.property.SimpleStringProperty lastIncident;
        private final javafx.beans.property.SimpleStringProperty linkedVictims;

        public PerpetratorRow(int perpetratorId,
                              String identifier,
                              String identifierType,
                              String threatLevel,
                              String lastIncident,
                              String linkedVictims) {
            this.perpetratorIdValue = perpetratorId;
            this.perpetratorId = new javafx.beans.property.SimpleStringProperty("#" + perpetratorId);
            this.identifier = new javafx.beans.property.SimpleStringProperty(identifier);
            this.identifierType = new javafx.beans.property.SimpleStringProperty(identifierType);
            this.threatLevel = new javafx.beans.property.SimpleStringProperty(threatLevel);
            this.lastIncident = new javafx.beans.property.SimpleStringProperty(lastIncident);
            this.linkedVictims = new javafx.beans.property.SimpleStringProperty(linkedVictims);
        }

        public javafx.beans.property.StringProperty perpetratorIdProperty() {
            return perpetratorId;
        }

        public javafx.beans.property.StringProperty identifierProperty() {
            return identifier;
        }

        public javafx.beans.property.StringProperty identifierTypeProperty() {
            return identifierType;
        }

        public javafx.beans.property.StringProperty threatLevelProperty() {
            return threatLevel;
        }

        public javafx.beans.property.StringProperty lastIncidentProperty() {
            return lastIncident;
        }

        public javafx.beans.property.StringProperty linkedVictimsProperty() {
            return linkedVictims;
        }

        public int getPerpetratorId() {
            return perpetratorIdValue;
        }

        public String getThreatLevel() {
            return threatLevel.get();
        }

        public String getIdentifier() {
            return identifier.get();
        }
    }

    public static class VictimRow {
        private final int victimIdValue;
        private final javafx.beans.property.SimpleStringProperty victimId;
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty email;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty reports;
        private final javafx.beans.property.SimpleStringProperty created;

        public VictimRow(int victimId,
                         String name,
                         String email,
                         String status,
                         String reports,
                         String created) {
            this.victimIdValue = victimId;
            this.victimId = new javafx.beans.property.SimpleStringProperty("#" + victimId);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.email = new javafx.beans.property.SimpleStringProperty(email);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.reports = new javafx.beans.property.SimpleStringProperty(reports);
            this.created = new javafx.beans.property.SimpleStringProperty(created);
        }

        public javafx.beans.property.StringProperty victimIdProperty() {
            return victimId;
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public javafx.beans.property.StringProperty emailProperty() {
            return email;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        public javafx.beans.property.StringProperty reportsProperty() {
            return reports;
        }

        public javafx.beans.property.StringProperty createdProperty() {
            return created;
        }

        public int getVictimId() {
            return victimIdValue;
        }

        public String getStatus() {
            return status.get();
        }
    }

    public static class EvidenceRow {
        private final int evidenceIdValue;
        private final javafx.beans.property.SimpleStringProperty evidenceId;
        private final javafx.beans.property.SimpleStringProperty incidentId;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty filePath;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty submitted;

        public EvidenceRow(int evidenceId,
                           int incidentId,
                           String type,
                           String filePath,
                           String status,
                           String submitted) {
            this.evidenceIdValue = evidenceId;
            this.evidenceId = new javafx.beans.property.SimpleStringProperty("#" + evidenceId);
            this.incidentId = new javafx.beans.property.SimpleStringProperty("#" + incidentId);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
            this.filePath = new javafx.beans.property.SimpleStringProperty(filePath);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.submitted = new javafx.beans.property.SimpleStringProperty(submitted);
        }

        public javafx.beans.property.StringProperty evidenceIdProperty() {
            return evidenceId;
        }

        public javafx.beans.property.StringProperty incidentIdProperty() {
            return incidentId;
        }

        public javafx.beans.property.StringProperty typeProperty() {
            return type;
        }

        public javafx.beans.property.StringProperty filePathProperty() {
            return filePath;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        public javafx.beans.property.StringProperty submittedProperty() {
            return submitted;
        }

        public int getEvidenceId() {
            return evidenceIdValue;
        }

        public String getStatus() {
            return status.get();
        }
    }
}

