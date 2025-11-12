package dao;

import model.ThreatLevelLog;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ThreatLevelLogDAOImpl implements ThreatLevelLogDAO {

    @Override
    public boolean logChange(int perpetratorID, String oldLevel, String newLevel, int adminID) throws SQLException {
        String sql = """
            INSERT INTO ThreatLevelLog 
            (PerpetratorID, OldThreatLevel, NewThreatLevel, ChangeDate, AdminID) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, perpetratorID);
            stmt.setString(2, oldLevel);
            stmt.setString(3, newLevel);
            stmt.setString(4, DateUtils.toDatabaseFormat(LocalDateTime.now()));
            stmt.setInt(5, adminID);

            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    // Optional: set logID if needed
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ThreatLevelLog> findByPerpetratorID(int perpetratorID) throws SQLException {
        return findByColumn("PerpetratorID", perpetratorID);
    }

    @Override
    public List<ThreatLevelLog> findAll() throws SQLException {
        List<ThreatLevelLog> list = new ArrayList<>();
        String sql = "SELECT * FROM ThreatLevelLog ORDER BY ChangeDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToLog(rs));
            }
        }
        return list;
    }

    private List<ThreatLevelLog> findByColumn(String column, int value) throws SQLException {
        List<ThreatLevelLog> list = new ArrayList<>();
        String sql = "SELECT * FROM ThreatLevelLog WHERE " + column + " = ? ORDER BY ChangeDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        }
        return list;
    }

    private ThreatLevelLog mapResultSetToLog(ResultSet rs) throws SQLException {
        ThreatLevelLog log = new ThreatLevelLog();
        log.setLogID(rs.getInt("LogID"));
        log.setPerpetratorID(rs.getInt("PerpetratorID"));
        log.setOldThreatLevel(rs.getString("OldThreatLevel"));
        log.setNewThreatLevel(rs.getString("NewThreatLevel"));
        log.setChangeDate(DateUtils.fromDatabaseFormat(rs.getString("ChangeDate")));
        log.setAdminID(rs.getInt("AdminID"));
        return log;
    }
}