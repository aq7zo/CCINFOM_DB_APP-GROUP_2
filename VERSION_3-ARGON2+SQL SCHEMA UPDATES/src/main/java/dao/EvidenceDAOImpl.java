package dao;

import model.Evidence;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvidenceDAOImpl implements EvidenceDAO {

    @Override
    public boolean upload(Evidence evidence) throws SQLException {
        String sql = """
            INSERT INTO EvidenceUpload 
            (IncidentID, EvidenceType, FilePath, SubmissionDate, VerifiedStatus, AdminID) 
            VALUES (?, ?, ?, ?, 'Pending', NULL)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, evidence.getIncidentID());
            stmt.setString(2, evidence.getEvidenceType());
            stmt.setString(3, evidence.getFilePath());
            stmt.setString(4, DateUtils.toDatabaseFormat(LocalDateTime.now()));

            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        evidence.setEvidenceID(keys.getInt(1));
                        evidence.setSubmissionDate(LocalDateTime.now());
                        evidence.setVerifiedStatus("Pending");
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Evidence findById(int evidenceID) throws SQLException {
        String sql = "SELECT * FROM EvidenceUpload WHERE EvidenceID = ?";
        return executeQuery(sql, stmt -> stmt.setInt(1, evidenceID));
    }

    @Override
    public List<Evidence> findByIncidentID(int incidentID) throws SQLException {
        return findByColumn("IncidentID", incidentID);
    }

    @Override
    public List<Evidence> findPending() throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = "SELECT * FROM EvidenceUpload WHERE VerifiedStatus = 'Pending' ORDER BY SubmissionDate";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEvidence(rs));
            }
        }
        return list;
    }

    @Override
    public boolean verify(int evidenceID, String status, int adminID) throws SQLException {
        String sql = "UPDATE EvidenceUpload SET VerifiedStatus = ?, AdminID = ? WHERE EvidenceID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status); // Verified or Rejected
            stmt.setInt(2, adminID);
            stmt.setInt(3, evidenceID);

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int evidenceID) throws SQLException {
        String sql = "DELETE FROM EvidenceUpload WHERE EvidenceID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, evidenceID);
            return stmt.executeUpdate() > 0;
        }
    }

    private List<Evidence> findByColumn(String column, int value) throws SQLException {
        List<Evidence> list = new ArrayList<>();
        String sql = "SELECT * FROM EvidenceUpload WHERE " + column + " = ? ORDER BY SubmissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEvidence(rs));
                }
            }
        }
        return list;
    }

    private Evidence executeQuery(String sql, PreparedStatementSetter setter) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setter.setValues(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvidence(rs);
                }
            }
        }
        return null;
    }

    private Evidence mapResultSetToEvidence(ResultSet rs) throws SQLException {
        Evidence e = new Evidence();
        e.setEvidenceID(rs.getInt("EvidenceID"));
        e.setIncidentID(rs.getInt("IncidentID"));
        e.setEvidenceType(rs.getString("EvidenceType"));
        e.setFilePath(rs.getString("FilePath"));

        String dateStr = rs.getString("SubmissionDate");
        if (dateStr != null) {
            e.setSubmissionDate(DateUtils.fromDatabaseFormat(dateStr));
        }

        e.setVerifiedStatus(rs.getString("VerifiedStatus"));
        e.setAdminID(getInteger(rs, "AdminID"));
        return e;
    }

    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        int val = rs.getInt(column);
        return rs.wasNull() ? null : val;
    }

    @FunctionalInterface
    private interface PreparedStatementSetter {
        void setValues(PreparedStatement stmt) throws SQLException;
    }
}