package controller.report;

import dao.EvidenceDAO;
import dao.EvidenceDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import dao.RecycleBinDAO;
import dao.RecycleBinDAOImpl;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Administrator;
import model.Evidence;
import model.IncidentReport;
import model.RecycleBinEvidence;
import model.RecycleBinReport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pending Reports Review Controller
 * Shows pending incident reports and evidence for admin review
 */
public class PendingReportsReviewController {

    @FXML private TabPane reviewTabs;
    @FXML private Tab pendingReportsTab;
    @FXML private Tab pendingEvidenceTab;
    @FXML private Tab recycleBinTab;
    
    // Pending Reports Table
    @FXML private TableView<IncidentReport> reportsTable;
    @FXML private TableColumn<IncidentReport, Boolean> reportSelectCol;
    @FXML private TableColumn<IncidentReport, Integer> reportIdCol;
    @FXML private TableColumn<IncidentReport, String> reportDateCol;
    @FXML private TableColumn<IncidentReport, String> reportDescriptionCol;
    @FXML private TableColumn<IncidentReport, String> reportStatusCol;
    @FXML private Button validateReportButton;
    @FXML private Button rejectReportButton;
    
    // Track selected reports
    private final Map<IncidentReport, Boolean> selectedReports = new HashMap<>();
    
    // Pending Evidence Table
    @FXML private TableView<Evidence> evidenceTable;
    @FXML private TableColumn<Evidence, Boolean> evidenceSelectCol;
    @FXML private TableColumn<Evidence, Integer> evidenceIdCol;
    @FXML private TableColumn<Evidence, Integer> evidenceIncidentCol;
    @FXML private TableColumn<Evidence, String> evidenceTypeCol;
    @FXML private TableColumn<Evidence, String> evidenceDateCol;
    @FXML private TableColumn<Evidence, String> evidenceStatusCol;
    @FXML private Button verifyEvidenceButton;
    @FXML private Button rejectEvidenceButton;
    @FXML private Label reportsCountLabel;
    @FXML private Label evidenceCountLabel;
    @FXML private Label recycleReportsCountLabel;
    @FXML private Label recycleEvidenceCountLabel;

    // Recycle Bin - Reports
    @FXML private TableView<RecycleBinReport> recycleReportsTable;
    @FXML private TableColumn<RecycleBinReport, Boolean> recycleReportSelectCol;
    @FXML private TableColumn<RecycleBinReport, Integer> recycleReportIdCol;
    @FXML private TableColumn<RecycleBinReport, String> recycleReportReasonCol;
    @FXML private TableColumn<RecycleBinReport, String> recycleReportArchivedCol;
    @FXML private Button restoreReportButton;

    // Recycle Bin - Evidence
    @FXML private TableView<RecycleBinEvidence> recycleEvidenceTable;
    @FXML private TableColumn<RecycleBinEvidence, Boolean> recycleEvidenceSelectCol;
    @FXML private TableColumn<RecycleBinEvidence, Integer> recycleEvidenceIdCol;
    @FXML private TableColumn<RecycleBinEvidence, Integer> recycleEvidenceIncidentCol;
    @FXML private TableColumn<RecycleBinEvidence, String> recycleEvidenceTypeCol;
    @FXML private TableColumn<RecycleBinEvidence, String> recycleEvidenceArchivedCol;
    @FXML private Button restoreEvidenceButton;

    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private final RecycleBinDAO recycleBinDAO = new RecycleBinDAOImpl();
    private Administrator currentAdmin;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Map<Evidence, Boolean> selectedEvidence = new HashMap<>();
    private final Map<RecycleBinReport, Boolean> recycleReportSelections = new HashMap<>();
    private final Map<RecycleBinEvidence, Boolean> recycleEvidenceSelections = new HashMap<>();
    private static final String REPORT_REJECTION_REASON = "Rejected from Pending Reports Review";
    private static final String EVIDENCE_REJECTION_REASON = "Rejected from Pending Evidence Review";

    @FXML
    private void initialize() {
        System.out.println("PendingReportsReviewController: Initializing...");
        setupReportsTable();
        setupEvidenceTable();
        setupRecycleBinTables();
        
        // Setup button handlers (already set in FXML, but can be set here too)
        
        // Refresh when tab is selected - load data regardless of admin (pending items are visible to all admins)
        if (pendingReportsTab != null) {
            pendingReportsTab.setOnSelectionChanged(e -> {
                if (pendingReportsTab.isSelected()) {
                    System.out.println("PendingReportsReviewController: Pending Reports tab selected, refreshing...");
                    refreshPendingReports();
                }
            });
        }
        
        if (pendingEvidenceTab != null) {
            pendingEvidenceTab.setOnSelectionChanged(e -> {
                if (pendingEvidenceTab.isSelected()) {
                    System.out.println("PendingReportsReviewController: Pending Evidence tab selected, refreshing...");
                    refreshPendingEvidence();
                }
            });
        }
        
        if (recycleBinTab != null) {
            recycleBinTab.setOnSelectionChanged(e -> {
                if (recycleBinTab.isSelected()) {
                    System.out.println("PendingReportsReviewController: Recycle Bin tab selected, refreshing...");
                    refreshRecycleBin();
                }
            });
        }
        
        // Load data initially when first tab is selected
        if (reviewTabs != null && reviewTabs.getTabs().size() > 0) {
            Tab selectedTab = reviewTabs.getSelectionModel().getSelectedItem();
            if (selectedTab == pendingReportsTab) {
                System.out.println("PendingReportsReviewController: Initial load - Pending Reports tab");
                refreshPendingReports();
            } else if (selectedTab == pendingEvidenceTab) {
                System.out.println("PendingReportsReviewController: Initial load - Pending Evidence tab");
                refreshPendingEvidence();
            } else if (selectedTab == recycleBinTab) {
                System.out.println("PendingReportsReviewController: Initial load - Recycle Bin tab");
                refreshRecycleBin();
            } else if (selectedTab == null && pendingReportsTab != null) {
                // If no tab selected, select and load the first one (Pending Reports)
                reviewTabs.getSelectionModel().select(pendingReportsTab);
            }
        }
        
        System.out.println("PendingReportsReviewController: Initialization complete");
    }

    private void setupReportsTable() {
        // Checkbox column for selection
        reportSelectCol.setCellValueFactory(cellData -> {
            IncidentReport report = cellData.getValue();
            Boolean selected = selectedReports.getOrDefault(report, false);
            return new SimpleBooleanProperty(selected);
        });
        
        reportSelectCol.setCellFactory(column -> new javafx.scene.control.TableCell<IncidentReport, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            private IncidentReport currentReport;
            
            {
                checkBox.setOnAction(e -> {
                    if (currentReport != null) {
                        selectedReports.put(currentReport, checkBox.isSelected());
                        updateButtonStates();
                    }
                });
            }
            
            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    currentReport = null;
                } else {
                    currentReport = getTableRow().getItem();
                    boolean isSelected = selectedReports.getOrDefault(currentReport, false);
                    checkBox.setSelected(isSelected);
                    setGraphic(checkBox);
                }
            }
        });
        
        reportIdCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        reportDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateReported();
            String dateStr = date != null ? date.format(dateFormatter) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        reportDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        reportStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        reportSelectCol.setPrefWidth(60);
        reportIdCol.setPrefWidth(80);
        reportDateCol.setPrefWidth(150);
        reportDescriptionCol.setPrefWidth(350);
        reportStatusCol.setPrefWidth(100);
        
        // Enable/disable buttons based on checkbox selection
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        long selectedCount = selectedReports.values().stream().filter(Boolean::booleanValue).count();
        boolean hasSelection = selectedCount > 0;
        validateReportButton.setDisable(!hasSelection);
        rejectReportButton.setDisable(!hasSelection);
    }

    private void updateEvidenceButtonStates() {
        long selectedCount = selectedEvidence.values().stream().filter(Boolean::booleanValue).count();
        boolean hasSelection = selectedCount > 0;
        verifyEvidenceButton.setDisable(!hasSelection);
        rejectEvidenceButton.setDisable(!hasSelection);
    }

    private void updateRecycleReportButtonState() {
        boolean hasSelection = recycleReportSelections.values().stream().anyMatch(Boolean::booleanValue);
        if (restoreReportButton != null) {
            restoreReportButton.setDisable(!hasSelection);
        }
    }

    private void updateRecycleEvidenceButtonState() {
        boolean hasSelection = recycleEvidenceSelections.values().stream().anyMatch(Boolean::booleanValue);
        if (restoreEvidenceButton != null) {
            restoreEvidenceButton.setDisable(!hasSelection);
        }
    }

    private void setupEvidenceTable() {
        evidenceSelectCol.setCellValueFactory(cellData -> {
            Evidence evidence = cellData.getValue();
            Boolean selected = selectedEvidence.getOrDefault(evidence, false);
            return new SimpleBooleanProperty(selected);
        });

        evidenceSelectCol.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private Evidence currentEvidence;

            {
                checkBox.setOnAction(e -> {
                    if (currentEvidence != null) {
                        selectedEvidence.put(currentEvidence, checkBox.isSelected());
                        updateEvidenceButtonStates();
                    }
                });
            }

            @Override
            protected void updateItem(Boolean selected, boolean empty) {
                super.updateItem(selected, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    currentEvidence = null;
                } else {
                    currentEvidence = getTableRow().getItem();
                    boolean isSelected = selectedEvidence.getOrDefault(currentEvidence, false);
                    checkBox.setSelected(isSelected);
                    setGraphic(checkBox);
                }
            }
        });

        evidenceIdCol.setCellValueFactory(new PropertyValueFactory<>("evidenceID"));
        evidenceIncidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        evidenceTypeCol.setCellValueFactory(new PropertyValueFactory<>("evidenceType"));
        evidenceDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getSubmissionDate();
            String dateStr = date != null ? date.format(dateFormatter) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        evidenceStatusCol.setCellValueFactory(new PropertyValueFactory<>("verifiedStatus"));
        
        evidenceSelectCol.setPrefWidth(60);
        evidenceIdCol.setPrefWidth(80);
        evidenceIncidentCol.setPrefWidth(100);
        evidenceTypeCol.setPrefWidth(150);
        evidenceDateCol.setPrefWidth(150);
        evidenceStatusCol.setPrefWidth(120);

        updateEvidenceButtonStates();
    }

    private void setupRecycleBinTables() {
        if (recycleReportSelectCol != null) {
            recycleReportSelectCol.setCellValueFactory(cellData -> {
                RecycleBinReport report = cellData.getValue();
                Boolean selected = recycleReportSelections.getOrDefault(report, false);
                return new SimpleBooleanProperty(selected);
            });

            recycleReportSelectCol.setCellFactory(column -> new TableCell<>() {
                private final CheckBox checkBox = new CheckBox();
                private RecycleBinReport currentReport;

                {
                    checkBox.setOnAction(e -> {
                        if (currentReport != null) {
                            recycleReportSelections.put(currentReport, checkBox.isSelected());
                            updateRecycleReportButtonState();
                        }
                    });
                }

                @Override
                protected void updateItem(Boolean selected, boolean empty) {
                    super.updateItem(selected, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        currentReport = null;
                    } else {
                        currentReport = getTableRow().getItem();
                        boolean isSelected = recycleReportSelections.getOrDefault(currentReport, false);
                        checkBox.setSelected(isSelected);
                        setGraphic(checkBox);
                    }
                }
            });

            recycleReportIdCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
            recycleReportReasonCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getArchiveReason() != null
                            ? cellData.getValue().getArchiveReason() : "N/A"));
            recycleReportArchivedCol.setCellValueFactory(cellData -> {
                LocalDateTime archivedAt = cellData.getValue().getArchivedAt();
                String dateStr = archivedAt != null ? archivedAt.format(dateFormatter) : "";
                return new SimpleStringProperty(dateStr);
            });
        }

        if (recycleEvidenceSelectCol != null) {
            recycleEvidenceSelectCol.setCellValueFactory(cellData -> {
                RecycleBinEvidence evidence = cellData.getValue();
                Boolean selected = recycleEvidenceSelections.getOrDefault(evidence, false);
                return new SimpleBooleanProperty(selected);
            });

            recycleEvidenceSelectCol.setCellFactory(column -> new TableCell<>() {
                private final CheckBox checkBox = new CheckBox();
                private RecycleBinEvidence currentEvidence;

                {
                    checkBox.setOnAction(e -> {
                        if (currentEvidence != null) {
                            recycleEvidenceSelections.put(currentEvidence, checkBox.isSelected());
                            updateRecycleEvidenceButtonState();
                        }
                    });
                }

                @Override
                protected void updateItem(Boolean selected, boolean empty) {
                    super.updateItem(selected, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        currentEvidence = null;
                    } else {
                        currentEvidence = getTableRow().getItem();
                        boolean isSelected = recycleEvidenceSelections.getOrDefault(currentEvidence, false);
                        checkBox.setSelected(isSelected);
                        setGraphic(checkBox);
                    }
                }
            });

            recycleEvidenceIdCol.setCellValueFactory(new PropertyValueFactory<>("evidenceID"));
            recycleEvidenceIncidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
            recycleEvidenceTypeCol.setCellValueFactory(new PropertyValueFactory<>("evidenceType"));
            recycleEvidenceArchivedCol.setCellValueFactory(cellData -> {
                LocalDateTime archivedAt = cellData.getValue().getArchivedAt();
                String dateStr = archivedAt != null ? archivedAt.format(dateFormatter) : "";
                return new SimpleStringProperty(dateStr);
            });
        }

        updateRecycleReportButtonState();
        updateRecycleEvidenceButtonState();
    }

    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
        // Load data when admin is set - refresh the currently visible tab
        if (reviewTabs != null) {
            Tab selectedTab = reviewTabs.getSelectionModel().getSelectedItem();
            if (selectedTab == pendingReportsTab) {
                refreshPendingReports();
            } else if (selectedTab == pendingEvidenceTab) {
                refreshPendingEvidence();
            } else if (selectedTab == recycleBinTab) {
                refreshRecycleBin();
            }
        }
    }

    public void refreshPendingReports() {
        try {
            System.out.println("PendingReportsReviewController: Loading pending reports...");
            List<IncidentReport> pending = incidentDAO.findPending();
            System.out.println("PendingReportsReviewController: Found " + pending.size() + " pending reports");
            
            // Clear selections for reports that no longer exist
            selectedReports.keySet().removeIf(report -> !pending.contains(report));
            
            ObservableList<IncidentReport> reports = FXCollections.observableArrayList(pending);
            if (reportsTable != null) {
                reportsTable.setItems(reports);
            }
            if (reportsCountLabel != null) {
                reportsCountLabel.setText("Pending Reports: " + pending.size());
            }
            updateButtonStates();
            System.out.println("PendingReportsReviewController: Reports loaded successfully");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load pending reports: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load pending reports: " + e.getMessage());
        }
    }

    public void refreshPendingEvidence() {
        try {
            System.out.println("PendingReportsReviewController: Loading pending evidence...");
            List<Evidence> pending = evidenceDAO.findPending();
            System.out.println("PendingReportsReviewController: Found " + pending.size() + " pending evidence");

            selectedEvidence.clear();

            ObservableList<Evidence> evidence = FXCollections.observableArrayList(pending);
            if (evidenceTable != null) {
                evidenceTable.setItems(evidence);
            }
            if (evidenceCountLabel != null) {
                evidenceCountLabel.setText("Pending Evidence: " + pending.size());
            }
            updateEvidenceButtonStates();
            System.out.println("PendingReportsReviewController: Evidence loaded successfully");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load pending evidence: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load pending evidence: " + e.getMessage());
        }
    }

    public void refreshRecycleBin() {
        refreshRecycleReports();
        refreshRecycleEvidence();
    }

    private void refreshRecycleReports() {
        try {
            List<RecycleBinReport> archived = recycleBinDAO.findAllReports();
            recycleReportSelections.keySet().removeIf(report -> !archived.contains(report));

            ObservableList<RecycleBinReport> data = FXCollections.observableArrayList(archived);
            if (recycleReportsTable != null) {
                recycleReportsTable.setItems(data);
            }
            if (recycleReportsCountLabel != null) {
                recycleReportsCountLabel.setText("Archived Reports: " + archived.size());
            }
            updateRecycleReportButtonState();
            System.out.println("PendingReportsReviewController: Recycle bin reports loaded.");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load recycle bin reports: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load recycled reports: " + e.getMessage());
        }
    }

    private void refreshRecycleEvidence() {
        try {
            List<RecycleBinEvidence> archived = recycleBinDAO.findAllEvidence();
            recycleEvidenceSelections.keySet().removeIf(item -> !archived.contains(item));

            ObservableList<RecycleBinEvidence> data = FXCollections.observableArrayList(archived);
            if (recycleEvidenceTable != null) {
                recycleEvidenceTable.setItems(data);
            }
            if (recycleEvidenceCountLabel != null) {
                recycleEvidenceCountLabel.setText("Archived Evidence: " + archived.size());
            }
            updateRecycleEvidenceButtonState();
            System.out.println("PendingReportsReviewController: Recycle bin evidence loaded.");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load recycle bin evidence: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load recycled evidence: " + e.getMessage());
        }
    }

    @FXML
    private void handleValidateReport() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected reports
        List<IncidentReport> toValidate = getSelectedIncidentReports();

        if (toValidate.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one report to validate.");
            return;
        }

        try {
            int successCount = 0;
            int failCount = 0;
            
            for (IncidentReport report : toValidate) {
                try {
                    if (incidentDAO.updateStatus(report.getIncidentID(), "Validated", currentAdmin.getAdminID())) {
                        successCount++;
                        selectedReports.remove(report); // Remove from selection after validation
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Error validating report #" + report.getIncidentID() + ": " + e.getMessage());
                    failCount++;
                }
            }

            String message = String.format("Validated %d report(s).", successCount);
            if (failCount > 0) {
                message += String.format(" %d report(s) failed to validate.", failCount);
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Validation Complete", message);
            refreshPendingReports();
        } catch (Exception e) {
            showError("Error validating reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRejectReport() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<IncidentReport> selected = getSelectedIncidentReports();
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one report to reject.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (IncidentReport report : selected) {
            try {
                boolean archived = recycleBinDAO.archiveIncidentReport(report, currentAdmin.getAdminID(), REPORT_REJECTION_REASON);
                if (!archived) {
                    failCount++;
                    continue;
                }

                if (incidentDAO.delete(report.getIncidentID())) {
                    selectedReports.remove(report);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception ex) {
                System.err.println("Error rejecting report #" + report.getIncidentID() + ": " + ex.getMessage());
                failCount++;
            }
        }

        String message = String.format("Rejected %d report(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d report(s) failed to archive.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Rejection Complete", message);
        refreshPendingReports();
        refreshPendingEvidence(); // cascade delete may remove related evidence
        refreshRecycleReports();
    }

    @FXML
    private void handleVerifyEvidence() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<Evidence> toVerify = getSelectedEvidenceItems();
        if (toVerify.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one piece of evidence to verify.");
            return;
        }

        try {
            int successCount = 0;
            int failCount = 0;

            for (Evidence evidence : toVerify) {
                try {
                    if (evidenceDAO.verify(evidence.getEvidenceID(), "Verified", currentAdmin.getAdminID())) {
                        successCount++;
                        selectedEvidence.remove(evidence);
                    } else {
                        failCount++;
                    }
                } catch (Exception ex) {
                    System.err.println("Error verifying evidence #" + evidence.getEvidenceID() + ": " + ex.getMessage());
                    failCount++;
                }
            }

            String message = String.format("Verified %d evidence item(s).", successCount);
            if (failCount > 0) {
                message += String.format(" %d item(s) failed to verify.", failCount);
            }

            showAlert(Alert.AlertType.INFORMATION, "Verification Complete", message);
            refreshPendingEvidence();
        } catch (Exception e) {
            showError("Error verifying evidence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRejectEvidence() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<Evidence> toReject = getSelectedEvidenceItems();
        if (toReject.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one piece of evidence to reject.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Evidence evidence : toReject) {
            try {
                boolean archived = recycleBinDAO.archiveEvidence(evidence, currentAdmin.getAdminID(), EVIDENCE_REJECTION_REASON);
                if (!archived) {
                    failCount++;
                    continue;
                }

                if (evidenceDAO.delete(evidence.getEvidenceID())) {
                    selectedEvidence.remove(evidence);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception ex) {
                System.err.println("Error rejecting evidence #" + evidence.getEvidenceID() + ": " + ex.getMessage());
                failCount++;
            }
        }

        String message = String.format("Rejected %d evidence item(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d item(s) failed to archive.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Rejection Complete", message);
        refreshPendingEvidence();
        refreshRecycleEvidence();
    }

    @FXML
    private void handleRestoreReports() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<RecycleBinReport> toRestore = getSelectedRecycleReports();
        if (toRestore.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one archived report to restore.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (RecycleBinReport report : toRestore) {
            try {
                if (recycleBinDAO.restoreIncidentReport(report)) {
                    recycleReportSelections.remove(report);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception ex) {
                System.err.println("Error restoring report #" + report.getIncidentID() + ": " + ex.getMessage());
                failCount++;
            }
        }

        String message = String.format("Restored %d report(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d report(s) failed to restore.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Restore Complete", message);
        refreshRecycleReports();
        refreshPendingReports();
    }

    @FXML
    private void handleRestoreEvidence() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        List<RecycleBinEvidence> toRestore = getSelectedRecycleEvidence();
        if (toRestore.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one archived evidence item to restore.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (RecycleBinEvidence evidence : toRestore) {
            try {
                if (recycleBinDAO.restoreEvidence(evidence)) {
                    recycleEvidenceSelections.remove(evidence);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception ex) {
                System.err.println("Error restoring evidence #" + evidence.getEvidenceID() + ": " + ex.getMessage());
                failCount++;
            }
        }

        String message = String.format("Restored %d evidence item(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d item(s) failed to restore.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Restore Complete", message);
        refreshRecycleEvidence();
        refreshPendingEvidence();
    }

    private List<IncidentReport> getSelectedIncidentReports() {
        return selectedReports.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<Evidence> getSelectedEvidenceItems() {
        return selectedEvidence.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<RecycleBinReport> getSelectedRecycleReports() {
        return recycleReportSelections.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<RecycleBinEvidence> getSelectedRecycleEvidence() {
        return recycleEvidenceSelections.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}

