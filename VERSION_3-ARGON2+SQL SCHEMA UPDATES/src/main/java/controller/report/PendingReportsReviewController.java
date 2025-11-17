package controller.report;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Administrator;
import model.Evidence;
import model.IncidentReport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pending Reports Review Controller
 * Shows pending incident reports and evidence for admin review
 */
public class PendingReportsReviewController {

    @FXML private TabPane reviewTabs;
    @FXML private Tab pendingReportsTab;
    @FXML private Tab pendingEvidenceTab;
    
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
    private final java.util.Map<IncidentReport, Boolean> selectedReports = new java.util.HashMap<>();
    
    // Pending Evidence Table
    @FXML private TableView<Evidence> evidenceTable;
    @FXML private TableColumn<Evidence, Integer> evidenceIdCol;
    @FXML private TableColumn<Evidence, Integer> evidenceIncidentCol;
    @FXML private TableColumn<Evidence, String> evidenceTypeCol;
    @FXML private TableColumn<Evidence, String> evidenceDateCol;
    @FXML private TableColumn<Evidence, String> evidenceStatusCol;
    @FXML private Button verifyEvidenceButton;
    @FXML private Button rejectEvidenceButton;
    @FXML private Label reportsCountLabel;
    @FXML private Label evidenceCountLabel;

    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private Administrator currentAdmin;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        System.out.println("PendingReportsReviewController: Initializing...");
        setupReportsTable();
        setupEvidenceTable();
        
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
        
        // Load data initially when first tab is selected
        if (reviewTabs != null && reviewTabs.getTabs().size() > 0) {
            Tab selectedTab = reviewTabs.getSelectionModel().getSelectedItem();
            if (selectedTab == pendingReportsTab) {
                System.out.println("PendingReportsReviewController: Initial load - Pending Reports tab");
                refreshPendingReports();
            } else if (selectedTab == pendingEvidenceTab) {
                System.out.println("PendingReportsReviewController: Initial load - Pending Evidence tab");
                refreshPendingEvidence();
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
            return new javafx.beans.property.SimpleBooleanProperty(selected);
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

    private void setupEvidenceTable() {
        evidenceIdCol.setCellValueFactory(new PropertyValueFactory<>("evidenceID"));
        evidenceIncidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        evidenceTypeCol.setCellValueFactory(new PropertyValueFactory<>("evidenceType"));
        evidenceDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getSubmissionDate();
            String dateStr = date != null ? date.format(dateFormatter) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        evidenceStatusCol.setCellValueFactory(new PropertyValueFactory<>("verifiedStatus"));
        
        evidenceIdCol.setPrefWidth(80);
        evidenceIncidentCol.setPrefWidth(100);
        evidenceTypeCol.setPrefWidth(150);
        evidenceDateCol.setPrefWidth(150);
        evidenceStatusCol.setPrefWidth(120);
        
        // Enable/disable buttons based on selection
        evidenceTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean hasSelection = newVal != null;
            verifyEvidenceButton.setDisable(!hasSelection);
            rejectEvidenceButton.setDisable(!hasSelection);
        });
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
            ObservableList<Evidence> evidence = FXCollections.observableArrayList(pending);
            if (evidenceTable != null) {
                evidenceTable.setItems(evidence);
            }
            if (evidenceCountLabel != null) {
                evidenceCountLabel.setText("Pending Evidence: " + pending.size());
            }
            System.out.println("PendingReportsReviewController: Evidence loaded successfully");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load pending evidence: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load pending evidence: " + e.getMessage());
        }
    }

    @FXML
    private void handleValidateReport() {
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected reports
        List<IncidentReport> toValidate = selectedReports.entrySet().stream()
                .filter(java.util.Map.Entry::getValue)
                .map(java.util.Map.Entry::getKey)
                .toList();

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
        IncidentReport selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected == null || currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a report to reject.");
            return;
        }

        // Note: The database schema only supports 'Pending' or 'Validated'
        // For rejection, we'll keep status as 'Pending' but could add a rejection note
        // Or we could delete it. For now, we'll just show an alert.
        showAlert(Alert.AlertType.INFORMATION, "Rejection", 
                "Report rejection feature - status remains 'Pending'.\n" +
                "To fully reject, consider updating the database schema.");
    }

    @FXML
    private void handleVerifyEvidence() {
        Evidence selected = evidenceTable.getSelectionModel().getSelectedItem();
        if (selected == null || currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select evidence to verify.");
            return;
        }

        try {
            if (evidenceDAO.verify(selected.getEvidenceID(), "Verified", currentAdmin.getAdminID())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Evidence #" + selected.getEvidenceID() + " has been verified.");
                refreshPendingEvidence();
            } else {
                showError("Failed to verify evidence.");
            }
        } catch (Exception e) {
            showError("Error verifying evidence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRejectEvidence() {
        Evidence selected = evidenceTable.getSelectionModel().getSelectedItem();
        if (selected == null || currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select evidence to reject.");
            return;
        }

        try {
            if (evidenceDAO.verify(selected.getEvidenceID(), "Rejected", currentAdmin.getAdminID())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Evidence #" + selected.getEvidenceID() + " has been rejected.");
                refreshPendingEvidence();
            } else {
                showError("Failed to reject evidence.");
            }
        } catch (Exception e) {
            showError("Error rejecting evidence: " + e.getMessage());
            e.printStackTrace();
        }
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

