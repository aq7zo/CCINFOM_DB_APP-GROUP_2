package controller;

import dao.EvidenceDAO;
import dao.EvidenceDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Evidence;
import model.IncidentReport;
import model.Victim;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Evidence Upload page (Transaction 2).
 * Handles the UI and logic for uploading evidence files associated with an incident report.
 */
public class UploadEvidenceController {

    // FXML UI components
    @FXML private ComboBox<String> evidenceTypeCombo; // Dropdown for evidence type selection
    @FXML private ComboBox<IncidentReport> incidentCombo; // Dropdown for selecting an incident
    @FXML private TextField filePathField; // Text field displaying selected file path
    @FXML private Button browseButton; // Button to browse for a file
    @FXML private Button uploadButton; // Button to upload selected file
    @FXML private Label statusLabel; // Label to show upload status or messages

    // DAO objects for database operations
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();

    // State variables
    private File selectedFile; // Stores the currently selected file
    private Victim currentVictim; // The victim currently logged in or being processed
    private final DateTimeFormatter incidentFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // Format for displaying incident dates

    /**
     * Initializes the controller.
     * Sets up dropdowns, cell factories, and change listeners for validation.
     */
    @FXML
    private void initialize() {
        // Populate evidence type options
        evidenceTypeCombo.setItems(FXCollections.observableArrayList(
                "Screenshot", "Email", "File", "Chat Log"
        ));
        evidenceTypeCombo.setValue("Screenshot"); // Default value

        // Customize how incidents are displayed in the dropdown list
        incidentCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(IncidentReport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(buildIncidentLabel(item));
                }
            }
        });

        // Customize the button display of the ComboBox
        incidentCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(IncidentReport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select incident");
                } else {
                    setText(buildIncidentLabel(item));
                }
            }
        });

        // Add listeners to validate form whenever inputs change
        filePathField.textProperty().addListener((obs, old, newVal) -> validate());
        evidenceTypeCombo.valueProperty().addListener((obs, old, newVal) -> validate());
        incidentCombo.valueProperty().addListener((obs, old, newVal) -> validate());
    }

    /**
     * Sets the current victim and refreshes the incident list for that victim.
     * @param victim The victim currently being processed
     */
    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        refreshIncidentList();
    }

    /**
     * Handles browsing for a file using a FileChooser.
     * Updates the filePathField and selectedFile.
     */
    @FXML
    private void handleBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Evidence File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        selectedFile = chooser.showOpenDialog(null); // Open file chooser dialog
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath()); // Display path in text field
        }
    }

    /**
     * Handles uploading the selected evidence file.
     * Validates inputs, copies file to uploads directory, and saves record to the database.
     */
    @FXML
    private void handleUpload() {
        // Check if a file is selected
        if (selectedFile == null) {
            showError("Please select a file");
            return;
        }

        // Check if an incident is selected
        IncidentReport selectedIncident = incidentCombo.getValue();
        if (selectedIncident == null) {
            showError("Please choose an incident");
            return;
        }

        try {
            // Create the uploads/evidence directory if it doesn't exist
            Path uploadDir = Path.of("uploads/evidence");
            Files.createDirectories(uploadDir);

            // Generate a unique filename using incident ID and timestamp
            String fileName = selectedIncident.getIncidentID() + "_" + System.currentTimeMillis() + "_" + selectedFile.getName();
            Path dest = uploadDir.resolve(fileName);

            // Copy the selected file to the uploads directory
            Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // Create Evidence object to save to database
            Evidence ev = new Evidence();
            ev.setIncidentID(selectedIncident.getIncidentID());
            ev.setEvidenceType(evidenceTypeCombo.getValue());
            ev.setFilePath(dest.toString());
            ev.setSubmissionDate(LocalDateTime.now());
            ev.setVerifiedStatus("Pending");

            // Upload to database
            if (evidenceDAO.upload(ev)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Evidence uploaded!\nFile: " + fileName + "\nAwaiting admin review.");
                clearForm(); // Clear form after successful upload
                statusLabel.setText("Pending review for Incident #" + selectedIncident.getIncidentID());
            }
        } catch (Exception e) {
            showError("Upload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enables or disables the upload button based on form validity.
     */
    private void validate() {
        uploadButton.setDisable(selectedFile == null || incidentCombo.getValue() == null);
    }

    /**
     * Clears the form inputs and resets the state.
     */
    private void clearForm() {
        filePathField.clear();
        selectedFile = null;
        statusLabel.setText("");
    }

    /**
     * Refreshes the list of incidents for the current victim.
     * Loads incidents from the database and updates the ComboBox.
     */
    public void refreshIncidentList() {
        if (currentVictim == null) {
            incidentCombo.getItems().clear();
            validate();
            return;
        }
        try {
            List<IncidentReport> incidents = incidentDAO.findByVictimID(currentVictim.getVictimID());
            incidentCombo.setItems(FXCollections.observableArrayList(incidents));
            if (!incidents.isEmpty() && incidentCombo.getSelectionModel().isEmpty()) {
                incidentCombo.getSelectionModel().selectFirst();
            }
            validate();
        } catch (Exception e) {
            showError("Failed to load incidents: " + e.getMessage());
        }
    }

    /**
     * Selects a specific incident in the ComboBox programmatically.
     * @param incident The incident to select
     */
    public void selectIncident(IncidentReport incident) {
        if (incident == null) return;
        for (IncidentReport item : incidentCombo.getItems()) {
            if (item.getIncidentID() == incident.getIncidentID()) {
                incidentCombo.getSelectionModel().select(item);
                validate();
                return;
            }
        }
    }

    /**
     * Builds a display label for an incident in the ComboBox.
     * @param incident The incident to build the label for
     * @return Formatted string like "Incident #ID • Date"
     */
    private String buildIncidentLabel(IncidentReport incident) {
        String date = incident.getDateReported() != null
                ? incident.getDateReported().format(incidentFormatter)
                : "Unknown date";
        return "Incident #" + incident.getIncidentID() + " • " + date;
    }

    /**
     * Shows an alert dialog.
     * @param type Alert type (INFO, ERROR, etc.)
     * @param title Title of the alert
     * @param msg Message content
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /**
     * Shows an error alert dialog.
     * @param msg Error message
     */
    private void showError(String msg) { 
        showAlert(Alert.AlertType.ERROR, "Error", msg); 
    }

    /**
     * Handles cancel button click.
     * Closes the current window.
     */
    @FXML private void handleCancel() {
        ((Stage) uploadButton.getScene().getWindow()).close();
    }
}
