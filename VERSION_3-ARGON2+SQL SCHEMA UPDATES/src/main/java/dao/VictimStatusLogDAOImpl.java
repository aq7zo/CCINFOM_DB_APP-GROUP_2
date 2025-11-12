package dao;

import model.VictimStatusLog;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VictimStatusLogDAOImpl implements VictimStatusLogDAO {

    @Override
    public boolean logChange(int victimID, String oldStatus, String newStatus, int adminID) throws SQLException {
        String sql = """
            INSERT INTO VictimStatusLog 
            (VictimID, OldStatus, NewStatus, ChangeDate, AdminID) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimID);
            stmt.setString(2, oldStatus);
            stmt.setString(3, newStatus);
            stmt.setString(4, DateUtils.toDatabaseFormat(LocalDateTime.now()));
            stmt.setInt(5, adminID);

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<VictimStatusLog> findByVictimID(int victimID) throws SQLException {
        return findByColumn("VictimID", victimID);
    }

    @  Override
    public List<VictimStatusLog> findAll() throws SQLException {
        List<VictimStatusLog> list = new ArrayList<>();
        String sql = "SELECT * FROM VictimStatusLog ORDER BY ChangeDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToLog(rs));
            }
        }
        return list;
    }

    private List<VictimStatusLog> findByColumn(String column, int value) throws SQLException {
        List<VictimStatusLog> list = new ArrayList<>();
        String sql = "SELECT * FROM VictimStatusLog WHERE " + column + " = ? ORDER BY ChangeDate DESC";

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

    private VictimStatusLog mapResultSetToLog(ResultSet rs) throws SQLException {
        VictimStatusLog log = new VictimStatusLog();
        log.setLogID(rs.getInt("LogID"));
        log.setVictimID(rs.getInt("VictimID"));
        log.setOldStatus(rs.getString("OldStatus"));
        log.setNewStatus(rs.getString("NewStatus"));
        log.setChangeDate(DateUtils.fromDatabaseFormat(rs.getString("ChangeDate")));
        log.setAdminID(rs.getInt("AdminID"));
        return log;
    }
}