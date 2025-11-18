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
 * Transaction 2: Evidence Upload
 */
public class UploadEvidenceController {

    @FXML private ComboBox<String> evidenceTypeCombo;
    @FXML private ComboBox<IncidentReport> incidentCombo;
    @FXML private TextField filePathField;
    @FXML private Button browseButton;
    @FXML private Button uploadButton;
    @FXML private Label statusLabel;

    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private File selectedFile;
    private Victim currentVictim;
    private final DateTimeFormatter incidentFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        evidenceTypeCombo.setItems(FXCollections.observableArrayList(
                "Screenshot", "Email", "File", "Chat Log"
        ));
        evidenceTypeCombo.setValue("Screenshot");

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

        filePathField.textProperty().addListener((obs, old, newVal) -> validate());
        evidenceTypeCombo.valueProperty().addListener((obs, old, newVal) -> validate());
        incidentCombo.valueProperty().addListener((obs, old, newVal) -> validate());
    }

    public void setCurrentVictim(Victim victim) {
        this.currentVictim = victim;
        refreshIncidentList();
    }

    @FXML
    private void handleBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Evidence File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        selectedFile = chooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleUpload() {
        if (selectedFile == null) {
            showError("Please select a file");
            return;
        }

        IncidentReport selectedIncident = incidentCombo.getValue();
        if (selectedIncident == null) {
            showError("Please choose an incident");
            return;
        }

        try {
            // Copy to uploads/ folder
            Path uploadDir = Path.of("uploads/evidence");
            Files.createDirectories(uploadDir);
            String fileName = selectedIncident.getIncidentID() + "_" + System.currentTimeMillis() + "_" + selectedFile.getName();
            Path dest = uploadDir.resolve(fileName);
            Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // Save to DB
            Evidence ev = new Evidence();
            ev.setIncidentID(selectedIncident.getIncidentID());
            ev.setEvidenceType(evidenceTypeCombo.getValue());
            ev.setFilePath(dest.toString());
            ev.setSubmissionDate(LocalDateTime.now());
            ev.setVerifiedStatus("Pending");

            if (evidenceDAO.upload(ev)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Evidence uploaded!\nFile: " + fileName + "\nAwaiting admin review.");
                clearForm();
                statusLabel.setText("Pending review for Incident #" + selectedIncident.getIncidentID());
            }
        } catch (Exception e) {
            showError("Upload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validate() {
        uploadButton.setDisable(selectedFile == null || incidentCombo.getValue() == null);
    }

    private void clearForm() {
        filePathField.clear();
        selectedFile = null;
        statusLabel.setText("");
    }

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

    private String buildIncidentLabel(IncidentReport incident) {
        String date = incident.getDateReported() != null
                ? incident.getDateReported().format(incidentFormatter)
                : "Unknown date";
        return "Incident #" + incident.getIncidentID() + " • " + date;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) { showAlert(Alert.AlertType.ERROR, "Error", msg); }

    @FXML private void handleCancel() {
        ((Stage) uploadButton.getScene().getWindow()).close();
    }
}