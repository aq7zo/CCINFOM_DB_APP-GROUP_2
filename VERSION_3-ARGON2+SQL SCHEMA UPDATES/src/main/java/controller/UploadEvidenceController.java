package controller;

import dao.EvidenceDAO;
import dao.EvidenceDAOImpl;
import dao.IncidentReportDAO;
import dao.IncidentReportDAOImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Evidence;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

/**
 * Transaction 2: Evidence Upload
 */
public class UploadEvidenceController {

    @FXML private ComboBox<String> evidenceTypeCombo;
    @FXML private TextField filePathField;
    @FXML private Button browseButton;
    @FXML private Button uploadButton;
    @FXML private Label statusLabel;

    private int incidentID;
    private final EvidenceDAO evidenceDAO = new EvidenceDAOImpl();
    private final IncidentReportDAO incidentDAO = new IncidentReportDAOImpl();
    private File selectedFile;

    @FXML
    private void initialize() {
        evidenceTypeCombo.setItems(FXCollections.observableArrayList(
                "Screenshot", "Email", "File", "Chat Log"
        ));
        evidenceTypeCombo.setValue("Screenshot");

        filePathField.textProperty().addListener((obs, old, newVal) -> validate());
        evidenceTypeCombo.valueProperty().addListener((obs, old, newVal) -> validate());
    }

    public void setIncidentID(int incidentID) {
        this.incidentID = incidentID;
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

        try {
            // Copy to uploads/ folder
            Path uploadDir = Path.of("uploads/evidence");
            Files.createDirectories(uploadDir);
            String fileName = incidentID + "_" + System.currentTimeMillis() + "_" + selectedFile.getName();
            Path dest = uploadDir.resolve(fileName);
            Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // Save to DB
            Evidence ev = new Evidence();
            ev.setIncidentID(incidentID);
            ev.setEvidenceType(evidenceTypeCombo.getValue());
            ev.setFilePath(dest.toString());
            ev.setSubmissionDate(LocalDateTime.now());
            ev.setVerifiedStatus("Pending");

            if (evidenceDAO.upload(ev)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Evidence uploaded!\nFile: " + fileName + "\nAwaiting admin review.");
                clearForm();
            }
        } catch (Exception e) {
            showError("Upload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validate() {
        uploadButton.setDisable(selectedFile == null);
    }

    private void clearForm() {
        filePathField.clear();
        selectedFile = null;
        statusLabel.setText("");
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