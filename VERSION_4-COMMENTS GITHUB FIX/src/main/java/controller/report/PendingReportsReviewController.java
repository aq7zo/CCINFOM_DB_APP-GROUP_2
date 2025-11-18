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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Administrator;
import model.Evidence;
import model.IncidentReport;
import model.RecycleBinEvidence;
import model.RecycleBinReport;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pending Reports Review Controller
 * 
 * This controller manages the review and approval workflow for pending incident reports
 * and evidence submissions. It provides administrators with the ability to:
 * - View and validate pending incident reports
 * - Verify or reject submitted evidence
 * - Manage archived items in the recycle bin
 * - Preview evidence files (especially images)
 * 
 * The interface is organized into three main tabs:
 * 1. Pending Reports - Shows unvalidated incident reports awaiting admin approval
 * 2. Pending Evidence - Displays unverified evidence submissions
 * 3. Recycle Bin - Contains rejected/archived reports and evidence that can be restored
 * 
 * @author [Your Name]
 * @version 1.0
 */
public class PendingReportsReviewController {

    // Tab components for organizing the review interface
    @FXML private TabPane reviewTabs;
    @FXML private Tab pendingReportsTab;
    @FXML private Tab pendingEvidenceTab;
    @FXML private Tab recycleBinTab;
    
    // Pending Reports Table components
    @FXML private TableView<IncidentReport> reportsTable;
    @FXML private TableColumn<IncidentReport, Boolean> reportSelectCol;
    @FXML private TableColumn<IncidentReport, Integer> reportIdCol;
    @FXML private TableColumn<IncidentReport, String> reportDateCol;
    @FXML private TableColumn<IncidentReport, String> reportDescriptionCol;
    @FXML private TableColumn<IncidentReport, String> reportStatusCol;
    @FXML private Button validateReportButton;
    @FXML private Button rejectReportButton;
    
    // Map to track which reports have been selected via checkboxes
    private final Map<IncidentReport, Boolean> selectedReports = new HashMap<>();
    
    // Pending Evidence Table components
    @FXML private TableView<Evidence> evidenceTable;
    @FXML private TableColumn<Evidence, Boolean> evidenceSelectCol;
    @FXML private TableColumn<Evidence, Integer> evidenceIdCol;
    @FXML private TableColumn<Evidence, Integer> evidenceIncidentCol;
    @FXML private TableColumn<Evidence, String> evidenceTypeCol;
    @FXML private TableColumn<Evidence, String> evidenceDateCol;
    @FXML private TableColumn<Evidence, String> evidenceStatusCol;
    @FXML private Button verifyEvidenceButton;
    @FXML private Button rejectEvidenceButton;
    
    // Labels showing counts of items in each section
    @FXML private Label reportsCountLabel;
    @FXML private Label evidenceCountLabel;
    @FXML private Label recycleReportsCountLabel;
    @FXML private Label recycleEvidenceCountLabel;
    
    // Evidence preview components
    @FXML private ImageView evidencePreviewImage;
    @FXML private Label previewPlaceholderLabel;
    @FXML private Label previewMetadataLabel;
    @FXML private Label previewFilePathLabel;
    @FXML private Hyperlink openFileLocationLink;

    // Recycle Bin - Reports Table components
    @FXML private TableView<RecycleBinReport> recycleReportsTable;
    @FXML private TableColumn<RecycleBinReport, Boolean> recycleReportSelectCol;
    @FXML private TableColumn<RecycleBinReport, Integer> recycleReportIdCol;
    @FXML private TableColumn<RecycleBinReport, String> recycleReportReasonCol;
    @FXML private TableColumn<RecycleBinReport, String> recycleReportArchivedCol;
    @FXML private Button restoreReportButton;

    // Recycle Bin - Evidence Table components
    @FXML private TableView<RecycleBinEvidence> recycleEvidenceTable;
    @FXML private TableColumn<RecycleBinEvidence, Boolean> recycleEvidenceSelectCol;
    @FXML private TableColumn<RecycleBinEvidence, Integer> recycleEvidenceIdCol;
    @FXML private TableColumn<RecycleBinEvidence, Integer> recycleEvidenceIncidentCol;
    @FXML private TableColumn<RecycleBinEvidence, String> recycleEvidenceTypeCol;
    @FXML private TableColumn<RecycleBinEvidence, String> recycleEvidenceArchivedCol;
    @FXML private Button restoreEvidenceButton;

    // DAO instances for database operations
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private final RecycleBinDAO recycleBinDAO = new RecycleBinDAOImpl();
    
    // Currently logged-in administrator
    private Administrator currentAdmin;
    
    // Date formatter for consistent date display throughout the UI
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Maps to track checkbox selections for evidence and recycle bin items
    private final Map<Evidence, Boolean> selectedEvidence = new HashMap<>();
    private final Map<RecycleBinReport, Boolean> recycleReportSelections = new HashMap<>();
    private final Map<RecycleBinEvidence, Boolean> recycleEvidenceSelections = new HashMap<>();
    
    // Standard rejection reasons for archived items
    private static final String REPORT_REJECTION_REASON = "Rejected from Pending Reports Review";
    private static final String EVIDENCE_REJECTION_REASON = "Rejected from Pending Evidence Review";
    
    // Supported image file extensions for preview functionality
    private static final String[] IMAGE_EXTENSIONS = {"png", "jpg", "jpeg", "gif", "bmp", "webp"};
    
    // Currently displayed evidence in the preview pane
    private Evidence previewedEvidence;

    /**
     * Initializes the controller after FXML elements are injected.
     * 
     * This method is automatically called by JavaFX after the FXML file is loaded.
     * It performs the following initialization tasks:
     * - Sets up all table columns and cell factories
     * - Configures tab selection listeners for lazy loading
     * - Initializes the evidence preview pane
     * - Loads data for the initially selected tab
     */
    @FXML
    private void initialize() {
        System.out.println("PendingReportsReviewController: Initializing...");
        
        // Configure table structures and cell rendering
        setupReportsTable();
        setupEvidenceTable();
        setupRecycleBinTables();
        
        // Clear preview pane initially
        showEvidencePreview(null);
        
        // Setup button handlers (already set in FXML, but can be set here too)
        
        // Attach listeners to refresh data when tabs are selected
        // This implements lazy loading - data is only loaded when the tab becomes visible
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
        
        // Load data initially for whichever tab is selected by default
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

    /**
     * Configures the Pending Reports table columns and cell factories.
     * 
     * Sets up:
     * - Checkbox column for multi-selection
     * - Report ID, date, description, and status columns
     * - Column widths for optimal display
     * - Button enable/disable logic based on selections
     */
    private void setupReportsTable() {
        // Configure checkbox column for report selection
        reportSelectCol.setCellValueFactory(cellData -> {
            IncidentReport report = cellData.getValue();
            Boolean selected = selectedReports.getOrDefault(report, false);
            return new SimpleBooleanProperty(selected);
        });
        
        // Create custom cell factory for interactive checkboxes
        reportSelectCol.setCellFactory(column -> new javafx.scene.control.TableCell<IncidentReport, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            private IncidentReport currentReport;
            
            {
                // Set up checkbox action handler
                checkBox.setOnAction(e -> {
                    if (currentReport != null) {
                        // Update selection state in the map
                        selectedReports.put(currentReport, checkBox.isSelected());
                        // Enable/disable action buttons based on whether anything is selected
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
        
        // Configure data columns using property value factories
        reportIdCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        
        // Format date column with custom formatter
        reportDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateReported();
            String dateStr = date != null ? date.format(dateFormatter) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        
        reportDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        reportStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set preferred column widths for optimal layout
        reportSelectCol.setPrefWidth(60);
        reportIdCol.setPrefWidth(80);
        reportDateCol.setPrefWidth(150);
        reportDescriptionCol.setPrefWidth(350);
        reportStatusCol.setPrefWidth(100);
        
        // Initialize button states (disabled until items are selected)
        updateButtonStates();
    }
    
    /**
     * Updates the enabled/disabled state of report action buttons.
     * 
     * Buttons are enabled only when at least one report is selected via checkbox.
     * This provides visual feedback and prevents empty operations.
     */
    private void updateButtonStates() {
        long selectedCount = selectedReports.values().stream().filter(Boolean::booleanValue).count();
        boolean hasSelection = selectedCount > 0;
        validateReportButton.setDisable(!hasSelection);
        rejectReportButton.setDisable(!hasSelection);
    }

    /**
     * Updates the enabled/disabled state of evidence action buttons.
     * 
     * Buttons are enabled only when at least one evidence item is selected.
     */
    private void updateEvidenceButtonStates() {
        long selectedCount = selectedEvidence.values().stream().filter(Boolean::booleanValue).count();
        boolean hasSelection = selectedCount > 0;
        if (verifyEvidenceButton != null) {
            verifyEvidenceButton.setDisable(!hasSelection);
        }
        if (rejectEvidenceButton != null) {
            rejectEvidenceButton.setDisable(!hasSelection);
        }
    }

    /**
     * Updates the enabled/disabled state of the restore button for archived reports.
     * 
     * The restore button is enabled only when at least one archived report is selected.
     */
    private void updateRecycleReportButtonState() {
        boolean hasSelection = recycleReportSelections.values().stream().anyMatch(Boolean::booleanValue);
        if (restoreReportButton != null) {
            restoreReportButton.setDisable(!hasSelection);
        }
    }

    /**
     * Updates the enabled/disabled state of the restore button for archived evidence.
     * 
     * The restore button is enabled only when at least one archived evidence item is selected.
     */
    private void updateRecycleEvidenceButtonState() {
        boolean hasSelection = recycleEvidenceSelections.values().stream().anyMatch(Boolean::booleanValue);
        if (restoreEvidenceButton != null) {
            restoreEvidenceButton.setDisable(!hasSelection);
        }
    }

    /**
     * Configures the Pending Evidence table columns and cell factories.
     * 
     * Sets up:
     * - Checkbox column for multi-selection
     * - Evidence ID, incident ID, type, date, and status columns
     * - Preview functionality when rows are selected
     * - Column widths and cell rendering
     */
    private void setupEvidenceTable() {
        // Configure checkbox column for evidence selection
        evidenceSelectCol.setCellValueFactory(cellData -> {
            Evidence evidence = cellData.getValue();
            Boolean selected = selectedEvidence.getOrDefault(evidence, false);
            return new SimpleBooleanProperty(selected);
        });

        // Create custom cell factory for interactive checkboxes
        evidenceSelectCol.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private Evidence currentEvidence;

            {
                // Set up checkbox action handler
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

        // Configure data columns
        evidenceIdCol.setCellValueFactory(new PropertyValueFactory<>("evidenceID"));
        evidenceIncidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
        evidenceTypeCol.setCellValueFactory(new PropertyValueFactory<>("evidenceType"));
        
        // Format date column
        evidenceDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getSubmissionDate();
            String dateStr = date != null ? date.format(dateFormatter) : "";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        
        evidenceStatusCol.setCellValueFactory(new PropertyValueFactory<>("verifiedStatus"));
        
        // Set column widths
        evidenceSelectCol.setPrefWidth(60);
        evidenceIdCol.setPrefWidth(80);
        evidenceIncidentCol.setPrefWidth(100);
        evidenceTypeCol.setPrefWidth(150);
        evidenceDateCol.setPrefWidth(150);
        evidenceStatusCol.setPrefWidth(120);

        // Add listener to show preview when evidence item is selected in table
        if (evidenceTable != null) {
            evidenceTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> showEvidencePreview(newSelection));
        }

        // Initialize button states
        updateEvidenceButtonStates();
    }

    /**
     * Configures the Recycle Bin tables for both reports and evidence.
     * 
     * Sets up columns for archived items including:
     * - Selection checkboxes
     * - Item IDs and metadata
     * - Archive reasons and timestamps
     * - Restore functionality
     */
    private void setupRecycleBinTables() {
        // Setup Recycle Bin Reports table
        if (recycleReportSelectCol != null) {
            // Configure checkbox column for archived report selection
            recycleReportSelectCol.setCellValueFactory(cellData -> {
                RecycleBinReport report = cellData.getValue();
                Boolean selected = recycleReportSelections.getOrDefault(report, false);
                return new SimpleBooleanProperty(selected);
            });

            // Create custom cell factory for checkboxes
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

            // Configure data columns for archived reports
            recycleReportIdCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
            
            // Display archive reason with fallback to "N/A"
            recycleReportReasonCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getArchiveReason() != null
                            ? cellData.getValue().getArchiveReason() : "N/A"));
            
            // Format archived date
            recycleReportArchivedCol.setCellValueFactory(cellData -> {
                LocalDateTime archivedAt = cellData.getValue().getArchivedAt();
                String dateStr = archivedAt != null ? archivedAt.format(dateFormatter) : "";
                return new SimpleStringProperty(dateStr);
            });
        }

        // Setup Recycle Bin Evidence table
        if (recycleEvidenceSelectCol != null) {
            // Configure checkbox column for archived evidence selection
            recycleEvidenceSelectCol.setCellValueFactory(cellData -> {
                RecycleBinEvidence evidence = cellData.getValue();
                Boolean selected = recycleEvidenceSelections.getOrDefault(evidence, false);
                return new SimpleBooleanProperty(selected);
            });

            // Create custom cell factory for checkboxes
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

            // Configure data columns for archived evidence
            recycleEvidenceIdCol.setCellValueFactory(new PropertyValueFactory<>("evidenceID"));
            recycleEvidenceIncidentCol.setCellValueFactory(new PropertyValueFactory<>("incidentID"));
            recycleEvidenceTypeCol.setCellValueFactory(new PropertyValueFactory<>("evidenceType"));
            
            // Format archived date
            recycleEvidenceArchivedCol.setCellValueFactory(cellData -> {
                LocalDateTime archivedAt = cellData.getValue().getArchivedAt();
                String dateStr = archivedAt != null ? archivedAt.format(dateFormatter) : "";
                return new SimpleStringProperty(dateStr);
            });
        }

        // Initialize button states for recycle bin actions
        updateRecycleReportButtonState();
        updateRecycleEvidenceButtonState();
    }

    /**
     * Sets the currently logged-in administrator.
     * 
     * This method should be called after login to associate the current admin
     * with review operations. It also triggers a refresh of the currently visible tab.
     * 
     * @param admin The administrator who is logged in
     */
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

    /**
     * Refreshes the Pending Reports table with the latest data from the database.
     * 
     * This method:
     * - Fetches all pending (unvalidated) incident reports
     * - Updates the table display
     * - Clears selections for reports that no longer exist
     * - Updates the report count label
     * - Handles any database errors gracefully
     */
    public void refreshPendingReports() {
        try {
            System.out.println("PendingReportsReviewController: Loading pending reports...");
            
            // Fetch all reports with "Pending" status from database
            List<IncidentReport> pending = incidentDAO.findPending();
            System.out.println("PendingReportsReviewController: Found " + pending.size() + " pending reports");
            
            // Clean up selections - remove any reports that were deleted or validated
            selectedReports.keySet().removeIf(report -> !pending.contains(report));
            
            // Update table with fresh data
            ObservableList<IncidentReport> reports = FXCollections.observableArrayList(pending);
            if (reportsTable != null) {
                reportsTable.setItems(reports);
            }
            
            // Update count label in UI
            if (reportsCountLabel != null) {
                reportsCountLabel.setText("Pending Reports: " + pending.size());
            }
            
            // Update button states based on current selections
            updateButtonStates();
            System.out.println("PendingReportsReviewController: Reports loaded successfully");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load pending reports: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load pending reports: " + e.getMessage());
        }
    }

    /**
     * Refreshes the Pending Evidence table with the latest data from the database.
     * 
     * This method:
     * - Fetches all pending (unverified) evidence items
     * - Updates the table display
     * - Clears all selections (for consistency)
     * - Auto-selects the first item for preview
     * - Updates the evidence count label
     * - Handles any database errors gracefully
     */
    public void refreshPendingEvidence() {
        try {
            System.out.println("PendingReportsReviewController: Loading pending evidence...");
            
            // Fetch all evidence with "Pending" verification status
            List<Evidence> pending = evidenceDAO.findPending();
            System.out.println("PendingReportsReviewController: Found " + pending.size() + " pending evidence");

            // Clear all checkbox selections
            selectedEvidence.clear();

            // Update table with fresh data
            ObservableList<Evidence> evidence = FXCollections.observableArrayList(pending);
            if (evidenceTable != null) {
                evidenceTable.setItems(evidence);
                
                // If there are items, select and preview the first one
                if (!evidence.isEmpty()) {
                    evidenceTable.getSelectionModel().selectFirst();
                } else {
                    // Clear preview if no items available
                    showEvidencePreview(null);
                }
            } else {
                showEvidencePreview(null);
            }
            
            // Update count label
            if (evidenceCountLabel != null) {
                evidenceCountLabel.setText("Pending Evidence: " + pending.size());
            }
            
            // Update button states
            updateEvidenceButtonStates();
            System.out.println("PendingReportsReviewController: Evidence loaded successfully");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load pending evidence: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load pending evidence: " + e.getMessage());
        }
    }

    /**
     * Refreshes all tables in the Recycle Bin tab.
     * 
     * This convenience method calls both refresh methods for archived reports
     * and archived evidence to ensure the entire recycle bin is up to date.
     */
    public void refreshRecycleBin() {
        refreshRecycleReports();
        refreshRecycleEvidence();
    }

    /**
     * Refreshes the archived reports table in the Recycle Bin.
     * 
     * Loads all archived (rejected) reports and updates the display.
     * Cleans up selections for items that no longer exist.
     */
    private void refreshRecycleReports() {
        try {
            // Fetch all archived reports from recycle bin
            List<RecycleBinReport> archived = recycleBinDAO.findAllReports();
            
            // Remove selections for reports that no longer exist in recycle bin
            recycleReportSelections.keySet().removeIf(report -> !archived.contains(report));

            // Update table display
            ObservableList<RecycleBinReport> data = FXCollections.observableArrayList(archived);
            if (recycleReportsTable != null) {
                recycleReportsTable.setItems(data);
            }
            
            // Update count label
            if (recycleReportsCountLabel != null) {
                recycleReportsCountLabel.setText("Archived Reports: " + archived.size());
            }
            
            // Update button states
            updateRecycleReportButtonState();
            System.out.println("PendingReportsReviewController: Recycle bin reports loaded.");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load recycle bin reports: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load recycled reports: " + e.getMessage());
        }
    }

    /**
     * Refreshes the archived evidence table in the Recycle Bin.
     * 
     * Loads all archived (rejected) evidence items and updates the display.
     * Cleans up selections for items that no longer exist.
     */
    private void refreshRecycleEvidence() {
        try {
            // Fetch all archived evidence from recycle bin
            List<RecycleBinEvidence> archived = recycleBinDAO.findAllEvidence();
            
            // Remove selections for evidence that no longer exists in recycle bin
            recycleEvidenceSelections.keySet().removeIf(item -> !archived.contains(item));

            // Update table display
            ObservableList<RecycleBinEvidence> data = FXCollections.observableArrayList(archived);
            if (recycleEvidenceTable != null) {
                recycleEvidenceTable.setItems(data);
            }
            
            // Update count label
            if (recycleEvidenceCountLabel != null) {
                recycleEvidenceCountLabel.setText("Archived Evidence: " + archived.size());
            }
            
            // Update button states
            updateRecycleEvidenceButtonState();
            System.out.println("PendingReportsReviewController: Recycle bin evidence loaded.");
        } catch (Exception e) {
            System.err.println("PendingReportsReviewController: Failed to load recycle bin evidence: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load recycled evidence: " + e.getMessage());
        }
    }

    /**
     * Handles the Validate Report button action.
     * 
     * This method:
     * - Validates that an admin is logged in
     * - Processes all selected reports
     * - Updates their status to "Validated" in the database
     * - Records the validating admin's ID
     * - Displays success/failure counts
     * - Refreshes the pending reports table
     * 
     * FXML event handler for validateReportButton
     */
    @FXML
    private void handleValidateReport() {
        // Ensure an admin is logged in before proceeding
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected reports from the checkbox map
        List<IncidentReport> toValidate = getSelectedIncidentReports();

        if (toValidate.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one report to validate.");
            return;
        }

        try {
            int successCount = 0;
            int failCount = 0;
            
            // Process each selected report
            for (IncidentReport report : toValidate) {
                try {
                    // Update report status to "Validated" and record the admin who validated it
                    if (incidentDAO.updateStatus(report.getIncidentID(), "Validated", currentAdmin.getAdminID())) {
                        successCount++;
                        selectedReports.remove(report); // Remove from selection after successful validation
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Error validating report #" + report.getIncidentID() + ": " + e.getMessage());
                    failCount++;
                }
            }

            // Build result message showing success and failure counts
            String message = String.format("Validated %d report(s).", successCount);
            if (failCount > 0) {
                message += String.format(" %d report(s) failed to validate.", failCount);
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Validation Complete", message);
            
            // Refresh the table to remove validated reports from pending list
            refreshPendingReports();
        } catch (Exception e) {
            showError("Error validating reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the Reject Report button action.
     * 
     * This method:
     * - Validates that an admin is logged in
     * - Archives selected reports to the recycle bin
     * - Records the rejecting admin and reason
     * - Deletes reports from the main incident table
     * - Displays success/failure counts
     * - Refreshes pending reports, pending evidence (due to cascade), and recycle bin
     * 
     * Note: Rejecting a report may cascade delete associated evidence.
     * 
     * FXML event handler for rejectReportButton
     */
    @FXML
    private void handleRejectReport() {
        // Ensure an admin is logged in
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected reports
        List<IncidentReport> selected = getSelectedIncidentReports();
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one report to reject.");
            return;
        }

<<<<<<< HEAD
        int successCount = 0;
        int failCount = 0;

        // Process each selected report
        for (IncidentReport report : selected) {
            try {
                // First, archive the report to the recycle bin with rejection reason
                boolean archived = recycleBinDAO.archiveIncidentReport(report, currentAdmin.getAdminID(), REPORT_REJECTION_REASON);
                if (!archived) {
                    failCount++;
                    continue;
                }

                // Then delete from the main incident reports table
                if (incidentDAO.delete(report.getIncidentID())) {
                    selectedReports.remove(report);
                    successCount++;
                } else {
                    failCount++;
=======
        for (IncidentReport report : selected) {
            try {
                // Try to archive to recycle bin for audit purposes (non-critical)
                try {
                    recycleBinDAO.archiveIncidentReport(report, currentAdmin.getAdminID(), REPORT_REJECTION_REASON);
                } catch (Exception archiveEx) {
                    System.err.println("Error archiving report #" + report.getIncidentID() + ": " + archiveEx.getMessage());
                    archiveEx.printStackTrace();
                }

                // Always update status to "Rejected" so victim can see it (critical operation)
                try {
                    if (incidentDAO.updateStatus(report.getIncidentID(), "Rejected", currentAdmin.getAdminID())) {
                        selectedReports.remove(report);
                    }
                } catch (Exception statusEx) {
                    System.err.println("Error updating status for report #" + report.getIncidentID() + ": " + statusEx.getMessage());
                    statusEx.printStackTrace();
                    // Check if it's a schema issue
                    if (statusEx.getMessage() != null && statusEx.getMessage().contains("Data truncated")) {
                        System.err.println("NOTE: Database schema may need to be updated. Run: ALTER TABLE IncidentReports MODIFY COLUMN Status ENUM('Pending', 'Validated', 'Rejected') DEFAULT 'Pending';");
                    }
>>>>>>> 73b1e9d449c7447cd670f6e37ab138c842aca912
                }
            } catch (Exception ex) {
                System.err.println("Unexpected error rejecting report #" + report.getIncidentID() + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }

<<<<<<< HEAD
        // Build result message
        String message = String.format("Rejected %d report(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d report(s) failed to archive.", failCount);
        }
=======
        String message = "Reports rejected successfully.";
>>>>>>> 73b1e9d449c7447cd670f6e37ab138c842aca912

        showAlert(Alert.AlertType.INFORMATION, "Rejection Complete", message);
        
        // Refresh all affected views
        refreshPendingReports();
        refreshPendingEvidence(); // cascade delete may remove related evidence
        refreshRecycleReports();
    }

    /**
     * Handles the Verify Evidence button action.
     * 
     * This method:
     * - Validates that an admin is logged in
     * - Processes all selected evidence items
     * - Updates their verification status to "Verified"
     * - Records the verifying admin's ID
     * - Displays success/failure counts
     * - Refreshes the pending evidence table
     * 
     * FXML event handler for verifyEvidenceButton
     */
    @FXML
    private void handleVerifyEvidence() {
        // Ensure an admin is logged in
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected evidence items
        List<Evidence> toVerify = getSelectedEvidenceItems();
        if (toVerify.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one piece of evidence to verify.");
            return;
        }

        try {
            int successCount = 0;
            int failCount = 0;

            // Process each selected evidence item
            for (Evidence evidence : toVerify) {
                try {
                    // Update verification status to "Verified" and record the admin
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

            // Build result message
            String message = String.format("Verified %d evidence item(s).", successCount);
            if (failCount > 0) {
                message += String.format(" %d item(s) failed to verify.", failCount);
            }

            showAlert(Alert.AlertType.INFORMATION, "Verification Complete", message);
            
            // Refresh the evidence table
            refreshPendingEvidence();
        } catch (Exception e) {
            showError("Error verifying evidence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the Reject Evidence button action.
     * 
     * This method:
     * - Validates that an admin is logged in
     * - Archives selected evidence items to the recycle bin
     * - Records the rejecting admin and reason
     * - Deletes evidence from the main evidence table
     * - Displays success/failure counts
     * - Refreshes pending evidence and recycle bin evidence tables
     * 
     * FXML event handler for rejectEvidenceButton
     */
    @FXML
    private void handleRejectEvidence() {
        // Ensure an admin is logged in
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected evidence items
        List<Evidence> toReject = getSelectedEvidenceItems();
        if (toReject.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one piece of evidence to reject.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        // Process each selected evidence item
        for (Evidence evidence : toReject) {
            try {
                // First, archive the evidence to the recycle bin with rejection reason
                boolean archived = recycleBinDAO.archiveEvidence(evidence, currentAdmin.getAdminID(), EVIDENCE_REJECTION_REASON);
                if (!archived) {
                    failCount++;
                    continue;
                }

                // Then delete from the main evidence table
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

        // Build result message
        String message = String.format("Rejected %d evidence item(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d item(s) failed to archive.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Rejection Complete", message);
        
        // Refresh affected views
        refreshPendingEvidence();
        refreshRecycleEvidence();
    }

    /**
     * Handles the Restore Reports button action in the Recycle Bin.
     * 
     * This method:
     * - Validates that an admin is logged in
     * - Restores selected archived reports back to pending status
     * - Removes them from the recycle bin
     * - Displays success/failure counts
     * - Refreshes both recycle bin and pending reports tables
     * 
     * FXML event handler for restoreReportButton
     */
    @FXML
    private void handleRestoreReports() {
        // Ensure an admin is logged in
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected archived reports
        List<RecycleBinReport> toRestore = getSelectedRecycleReports();
        if (toRestore.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one archived report to restore.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        // Process each selected archived report
        for (RecycleBinReport report : toRestore) {
            try {
                // Restore the report back to the incident reports table
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

        // Build result message
        String message = String.format("Restored %d report(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d report(s) failed to restore.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Restore Complete", message);
        
        // Refresh both recycle bin and pending reports
        refreshRecycleReports();
        refreshPendingReports();
    }

    /**
     * Handles the Restore Evidence button action in the Recycle Bin.
     * 
     * This method:
     * - Validates that an admin is logged in
     * - Restores selected archived evidence items back to pending status
     * - Removes them from the recycle bin
     * - Displays success/failure counts
     * - Refreshes both recycle bin and pending evidence tables
     * 
     * FXML event handler for restoreEvidenceButton
     */
    @FXML
    private void handleRestoreEvidence() {
        // Ensure an admin is logged in
        if (currentAdmin == null) {
            showAlert(Alert.AlertType.WARNING, "No Admin", "Admin not set. Please log out and log back in.");
            return;
        }

        // Get all selected archived evidence items
        List<RecycleBinEvidence> toRestore = getSelectedRecycleEvidence();
        if (toRestore.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one archived evidence item to restore.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        // Process each selected archived evidence item
        for (RecycleBinEvidence evidence : toRestore) {
            try {
                // Restore the evidence back to the evidence table
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

        // Build result message
        String message = String.format("Restored %d evidence item(s).", successCount);
        if (failCount > 0) {
            message += String.format(" %d item(s) failed to restore.", failCount);
        }

        showAlert(Alert.AlertType.INFORMATION, "Restore Complete", message);
        
        // Refresh both recycle bin and pending evidence
        refreshRecycleEvidence();
        refreshPendingEvidence();
    }

    /**
     * Handles the Open File Location hyperlink action.
     * 
     * This method:
     * - Validates that an evidence item is currently previewed
     * - Checks that the evidence has a file path
     * - Verifies the file exists on the file system
     * - Opens the file with the system's default application
     * - Displays appropriate error messages if any step fails
     * 
     * FXML event handler for openFileLocationLink
     */
    @FXML
    private void handleOpenFileLocation() {
        // Ensure an evidence item is selected for preview
        if (previewedEvidence == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Select an evidence item to open its file location.");
            return;
        }

        // Get the file path from the evidence record
        String filePath = previewedEvidence.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Missing File", "This evidence record does not have a saved file path.");
            return;
        }

        // Create a File object and check if it exists
        File file = new File(filePath);
        if (!file.exists()) {
            showAlert(Alert.AlertType.ERROR, "File Not Found",
                    "The referenced file could not be found:\n" + file.getAbsolutePath());
            return;
        }

        // Check if desktop operations are supported on this platform
        if (!Desktop.isDesktopSupported()) {
            showAlert(Alert.AlertType.WARNING, "Not Supported",
                    "Opening files is not supported on this device. Please access the file manually:\n" + file.getAbsolutePath());
            return;
        }

        try {
            // Open the file with the system's default application
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Unable to Open File",
                    "An error occurred while opening the file:\n" + e.getMessage());
        }
    }

    /**
     * Extracts a list of selected incident reports from the selection map.
     * 
     * @return List of IncidentReport objects that have been checked/selected
     */
    private List<IncidentReport> getSelectedIncidentReports() {
        return selectedReports.entrySet().stream()
                .filter(Map.Entry::getValue) // Only include entries where value is true
                .map(Map.Entry::getKey)      // Extract the IncidentReport key
                .toList();
    }

    /**
     * Extracts a list of selected evidence items from the selection map.
     * 
     * @return List of Evidence objects that have been checked/selected
     */
    private List<Evidence> getSelectedEvidenceItems() {
        return selectedEvidence.entrySet().stream()
                .filter(Map.Entry::getValue) // Only include entries where value is true
                .map(Map.Entry::getKey)      // Extract the Evidence key
                .toList();
    }

    /**
     * Extracts a list of selected archived reports from the recycle bin selection map.
     * 
     * @return List of RecycleBinReport objects that have been checked/selected
     */
    private List<RecycleBinReport> getSelectedRecycleReports() {
        return recycleReportSelections.entrySet().stream()
                .filter(Map.Entry::getValue) // Only include entries where value is true
                .map(Map.Entry::getKey)      // Extract the RecycleBinReport key
                .toList();
    }

    /**
     * Extracts a list of selected archived evidence from the recycle bin selection map.
     * 
     * @return List of RecycleBinEvidence objects that have been checked/selected
     */
    private List<RecycleBinEvidence> getSelectedRecycleEvidence() {
        return recycleEvidenceSelections.entrySet().stream()
                .filter(Map.Entry::getValue) // Only include entries where value is true
                .map(Map.Entry::getKey)      // Extract the RecycleBinEvidence key
                .toList();
    }

    /**
     * Displays or hides the evidence preview based on the selected evidence item.
     * 
     * This method handles:
     * - Displaying metadata (ID, type, status, submission date)
     * - Showing image previews for supported file types
     * - Displaying file path information
     * - Showing appropriate placeholder messages when preview is unavailable
     * - Enabling/disabling the "Open File Location" link
     * 
     * @param evidence The Evidence object to preview, or null to clear the preview
     */
    private void showEvidencePreview(Evidence evidence) {
        // Store the currently previewed evidence for other operations
        previewedEvidence = evidence;

        // Ensure UI components are available
        if (previewMetadataLabel == null || previewPlaceholderLabel == null) {
            return;
        }

        // Handle null case - clear the preview
        if (evidence == null) {
            previewMetadataLabel.setText("Select an evidence item to preview.");
            if (previewFilePathLabel != null) {
                previewFilePathLabel.setText("");
            }
            setPreviewImageVisible(false);
            togglePlaceholder(true, "Select an evidence item to preview.");
            toggleOpenFileLink(false);
            return;
        }

        // Format metadata for display
        String submitted = evidence.getSubmissionDate() != null
                ? evidence.getSubmissionDate().format(dateFormatter)
                : "N/A";
        String status = safeValue(evidence.getVerifiedStatus());
        String type = safeValue(evidence.getEvidenceType());

        // Display evidence metadata in the preview area
        previewMetadataLabel.setText(String.format(
                "Evidence #%d | Incident #%d%nType: %s%nStatus: %s%nSubmitted: %s",
                evidence.getEvidenceID(),
                evidence.getIncidentID(),
                type,
                status,
                submitted));

        // Check if evidence has an associated file
        String filePath = evidence.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            if (previewFilePathLabel != null) {
                previewFilePathLabel.setText("File: (no file path provided)");
            }
            setPreviewImageVisible(false);
            togglePlaceholder(true, "No file path is associated with this evidence.");
            toggleOpenFileLink(false);
            return;
        }

        // Verify the file exists on the file system
        File file = new File(filePath);
        boolean exists = file.exists();
        if (previewFilePathLabel != null) {
            String labelText = exists ? "File: " + file.getAbsolutePath()
                    : "File: " + file.getAbsolutePath() + " (not found)";
            previewFilePathLabel.setText(labelText);
        }

        // Handle case where file doesn't exist
        if (!exists) {
            setPreviewImageVisible(false);
            togglePlaceholder(true, "The referenced file could not be found.");
            toggleOpenFileLink(false);
            return;
        }

        // Check if the file is an image that can be previewed
        boolean isImage = isImageFile(filePath);
        if (isImage) {
            try {
                // Attempt to load and display the image
                Image image = new Image(file.toURI().toString());
                
                // Verify image loaded successfully and has valid dimensions
                if (!image.isError() && image.getWidth() > 1 && image.getHeight() > 1) {
                    if (evidencePreviewImage != null) {
                        evidencePreviewImage.setImage(image);
                    }
                    setPreviewImageVisible(true);
                    togglePlaceholder(false, null);
                    // Show open file link if image is larger than preview area
                    toggleOpenFileLink(shouldOfferFullView(image));
                    return;
                }
            } catch (Exception ex) {
                System.err.println("PendingReportsReviewController: Failed to display image preview for evidence "
                        + evidence.getEvidenceID() + ": " + ex.getMessage());
            }
            // If image loading failed, show placeholder with option to open file
            setPreviewImageVisible(false);
            togglePlaceholder(true, "Unable to render the image preview. Use the link below to open the original file.");
            toggleOpenFileLink(true);
            return;
        }

        // File is not an image - show placeholder and offer to open the file
        setPreviewImageVisible(false);
        togglePlaceholder(true, "Preview available for image uploads only. Use the link below to open the file.");
        toggleOpenFileLink(true);
    }

    /**
     * Shows or hides the evidence preview image component.
     * 
     * @param visible true to show the image, false to hide it and clear the image
     */
    private void setPreviewImageVisible(boolean visible) {
        if (evidencePreviewImage == null) {
            return;
        }
        evidencePreviewImage.setVisible(visible);
        evidencePreviewImage.setManaged(visible); // Remove from layout when hidden
        if (!visible) {
            evidencePreviewImage.setImage(null); // Clear image to free memory
        }
    }

    /**
     * Shows or hides the placeholder label in the preview area.
     * 
     * The placeholder is displayed when no image preview is available.
     * 
     * @param show true to show the placeholder, false to hide it
     * @param message Optional message to display in the placeholder, or null to keep current message
     */
    private void togglePlaceholder(boolean show, String message) {
        if (previewPlaceholderLabel == null) {
            return;
        }
        previewPlaceholderLabel.setVisible(show);
        previewPlaceholderLabel.setManaged(show); // Remove from layout when hidden
        
        // Update message if provided
        if (message != null && !message.isBlank()) {
            previewPlaceholderLabel.setText(message);
        } else if (!show && previewPlaceholderLabel.getText().isBlank()) {
            // Reset to default message when hiding if text is blank
            previewPlaceholderLabel.setText("Select an evidence item to preview.");
        }
    }

    /**
     * Shows or hides the "Open File Location" hyperlink.
     * 
     * @param show true to show the hyperlink, false to hide it
     */
    private void toggleOpenFileLink(boolean show) {
        if (openFileLocationLink == null) {
            return;
        }
        openFileLocationLink.setVisible(show);
        openFileLocationLink.setManaged(show); // Remove from layout when hidden
    }

    /**
     * Checks if a file is an image based on its file extension.
     * 
     * Supported image extensions: png, jpg, jpeg, gif, bmp, webp
     * 
     * @param filePath The path to the file to check
     * @return true if the file has an image extension, false otherwise
     */
    private boolean isImageFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        
        // Find the last dot to extract the extension
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filePath.length() - 1) {
            return false; // No extension or dot is the last character
        }
        
        // Extract and normalize the extension
        String extension = filePath.substring(dotIndex + 1).toLowerCase();
        
        // Check if extension matches any supported image format
        for (String allowed : IMAGE_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the "Open File Location" link should be offered for an image.
     * 
     * The link is offered when the actual image dimensions exceed the preview area,
     * allowing users to view the full-resolution image.
     * 
     * @param image The Image to check
     * @return true if the image is larger than the preview area, false otherwise
     */
    private boolean shouldOfferFullView(Image image) {
        if (image == null || evidencePreviewImage == null) {
            return false;
        }
        
        // Get the preview area's dimensions
        double fitWidth = evidencePreviewImage.getFitWidth();
        double fitHeight = evidencePreviewImage.getFitHeight();
        
        if (fitWidth <= 0 || fitHeight <= 0) {
            return false; // Invalid dimensions
        }
        
        // Check if image is larger than preview area in either dimension
        return image.getWidth() > fitWidth || image.getHeight() > fitHeight;
    }

    /**
     * Returns a safe display value for potentially null or blank strings.
     * 
     * @param value The string to check
     * @return The original value if not null/blank, otherwise "N/A"
     */
    private String safeValue(String value) {
        return (value == null || value.isBlank()) ? "N/A" : value;
    }

    /**
     * Displays a JavaFX alert dialog with the specified parameters.
     * 
     * @param type The type of alert (INFORMATION, WARNING, ERROR, etc.)
     * @param title The title of the alert dialog
     * @param message The message content to display
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Remove header for cleaner appearance
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an error alert dialog.
     * 
     * This is a convenience method for showing error messages.
     * 
     * @param message The error message to display
     */
    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}