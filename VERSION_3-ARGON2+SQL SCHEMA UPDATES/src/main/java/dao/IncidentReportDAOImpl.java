package dao;

import model.IncidentReport;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IncidentReportDAOImpl implements IncidentReportDAO {

    @Override
    public boolean create(IncidentReport report) throws SQLException {
        String sql = "INSERT INTO IncidentReports (VictimID, PerpetratorID, AttackTypeID, AdminID, " +
                "DateReported, Description, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, report.getVictimID());
            stmt.setInt(2, report.getPerpetratorID());
            stmt.setInt(3, report.getAttackTypeID());
            setInteger(stmt, 4, report.getAdminID());
            stmt.setString(5, DateUtils.toDatabaseFormat(LocalDateTime.now()));
            stmt.setString(6, report.getDescription());
            stmt.setString(7, report.getStatus());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        report.setIncidentID(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public IncidentReport findById(int incidentID) throws SQLException {
        String sql = "SELECT * FROM IncidentReports WHERE IncidentID = ?";
        return executeQuery(sql, stmt -> stmt.setInt(1, incidentID));
    }

    @Override
    public List<IncidentReport> findByVictimID(int victimID) throws SQLException {
        return findByColumn("VictimID", victimID);
    }

    @Override
    public List<IncidentReport> findByPerpetratorID(int perpetratorID) throws SQLException {
        return findByColumn("PerpetratorID", perpetratorID);
    }

    @Override
    public List<IncidentReport> findPending() throws SQLException {
        String sql = "SELECT * FROM IncidentReports WHERE Status = 'Pending' ORDER BY DateReported";
        List<IncidentReport> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToIncidentReport(rs));
            }
        }
        return list;
    }

    @Override
    public boolean updateStatus(int incidentID, String status, Integer adminID) throws SQLException {
        String sql = "UPDATE IncidentReports SET Status = ?, AdminID = ? WHERE IncidentID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            setInteger(stmt, 2, adminID);
            stmt.setInt(3, incidentID);

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public int countVictimsLast7Days(int perpetratorID) throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT VictimID) 
            FROM IncidentReports 
            WHERE PerpetratorID = ? 
              AND DateReported >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, perpetratorID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public int countUniqueVictimsLast7Days(int perpetratorID) throws SQLException {
        String sql = """
        SELECT COUNT(DISTINCT VictimID) 
        FROM IncidentReports 
        WHERE PerpetratorID = ? 
          AND DateReported >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, perpetratorID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public int countIncidentsLastMonth(int victimID) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM IncidentReports 
            WHERE VictimID = ? 
              AND YEAR(DateReported) = YEAR(CURDATE()) 
              AND MONTH(DateReported) = MONTH(CURDATE())
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<IncidentReport> findByColumn(String column, int value) throws SQLException {
        List<IncidentReport> list = new ArrayList<>();
        String sql = "SELECT * FROM IncidentReports WHERE " + column + " = ? ORDER BY DateReported DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToIncidentReport(rs));
                }
            }
        }
        return list;
    }

    private IncidentReport executeQuery(String sql, PreparedStatementSetter setter) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setter.setValues(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIncidentReport(rs);
                }
            }
        }
        return null;
    }

    private IncidentReport mapResultSetToIncidentReport(ResultSet rs) throws SQLException {
        IncidentReport ir = new IncidentReport();
        ir.setIncidentID(rs.getInt("IncidentID"));
        ir.setVictimID(rs.getInt("VictimID"));
        ir.setPerpetratorID(rs.getInt("PerpetratorID"));
        ir.setAttackTypeID(rs.getInt("AttackTypeID"));
        ir.setAdminID(getInteger(rs, "AdminID"));
        ir.setDateReported(DateUtils.fromDatabaseFormat(rs.getString("DateReported")));
        ir.setDescription(rs.getString("Description"));
        ir.setStatus(rs.getString("Status"));
        return ir;
    }

    private void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
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