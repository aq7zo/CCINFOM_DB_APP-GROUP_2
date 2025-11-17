package dao;

import model.Perpetrator;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PerpetratorDAOImpl implements PerpetratorDAO {

    @Override
    public Perpetrator findByIdentifier(String identifier) throws SQLException {
        String sql = "SELECT * FROM Perpetrators WHERE Identifier = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerpetrator(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Perpetrator findById(int perpetratorID) throws SQLException {
        String sql = "SELECT * FROM Perpetrators WHERE PerpetratorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, perpetratorID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerpetrator(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Perpetrator> findAll() throws SQLException {
        List<Perpetrator> list = new ArrayList<>();
        String sql = "SELECT * FROM Perpetrators ORDER BY LastIncidentDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToPerpetrator(rs));
            }
        }
        return list;
    }

    @Override
    public boolean create(Perpetrator perpetrator) throws SQLException {
        String sql = "INSERT INTO Perpetrators (Identifier, IdentifierType, AssociatedName, ThreatLevel, LastIncidentDate) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, perpetrator.getIdentifier());
            stmt.setString(2, perpetrator.getIdentifierType());
            stmt.setString(3, perpetrator.getAssociatedName());
            stmt.setString(4, perpetrator.getThreatLevel());
            stmt.setString(5, DateUtils.toDatabaseFormat(perpetrator.getLastIncidentDate()));

            if (stmt.executeUpdate() > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        perpetrator.setPerpetratorID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Perpetrator perpetrator) throws SQLException {
        String sql = "UPDATE Perpetrators SET IdentifierType = ?, AssociatedName = ?, " +
                "ThreatLevel = ?, LastIncidentDate = ? WHERE PerpetratorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, perpetrator.getIdentifierType());
            stmt.setString(2, perpetrator.getAssociatedName());
            stmt.setString(3, perpetrator.getThreatLevel());
            stmt.setString(4, DateUtils.toDatabaseFormat(perpetrator.getLastIncidentDate()));
            stmt.setInt(5, perpetrator.getPerpetratorID());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int perpetratorID) throws SQLException {
        String sql = "DELETE FROM Perpetrators WHERE PerpetratorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, perpetratorID);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Perpetrator createOrUpdate(Perpetrator perpetrator) throws SQLException {
        Perpetrator existing = findByIdentifier(perpetrator.getIdentifier());
        if (existing != null) {
            // Update existing
            existing.setIdentifierType(perpetrator.getIdentifierType());
            existing.setAssociatedName(perpetrator.getAssociatedName());
            existing.setThreatLevel(perpetrator.getThreatLevel());
            existing.setLastIncidentDate(LocalDateTime.now());
            update(existing);
            return existing;
        } else {
            // Create new
            perpetrator.setLastIncidentDate(LocalDateTime.now());
            create(perpetrator);
            return perpetrator;
        }
    }

    private Perpetrator mapResultSetToPerpetrator(ResultSet rs) throws SQLException {
        Perpetrator p = new Perpetrator();
        p.setPerpetratorID(rs.getInt("PerpetratorID"));
        p.setIdentifier(rs.getString("Identifier"));
        p.setIdentifierType(rs.getString("IdentifierType"));
        p.setAssociatedName(rs.getString("AssociatedName"));
        p.setThreatLevel(rs.getString("ThreatLevel"));

        String dateStr = rs.getString("LastIncidentDate");
        if (dateStr != null) {
            p.setLastIncidentDate(DateUtils.fromDatabaseFormat(dateStr));
        }
        return p;
    }
}