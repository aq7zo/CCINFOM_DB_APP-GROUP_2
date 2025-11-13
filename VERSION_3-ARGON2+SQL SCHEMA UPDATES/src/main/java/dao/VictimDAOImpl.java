package dao;

import model.Victim;
import util.DatabaseConnection;
import util.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of VictimDAO interface
 * Handles database operations for Victim entity
 */
public class VictimDAOImpl implements VictimDAO {

    @Override
    public Victim findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Victims WHERE ContactEmail = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVictim(rs);
                }
            }
        }

        return null;
    }

    @Override
    public Victim findById(int victimID) throws SQLException {
        String sql = "SELECT * FROM Victims WHERE VictimID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVictim(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Victim> findAll() throws SQLException {
        List<Victim> victims = new ArrayList<>();
        String sql = "SELECT * FROM Victims ORDER BY VictimID";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                victims.add(mapResultSetToVictim(rs));
            }
        }

        return victims;
    }

    @Override
    public boolean create(Victim victim) throws SQLException {
        String sql = "INSERT INTO Victims (Name, ContactEmail, PasswordHash, AccountStatus) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Validate required fields
            if (victim.getName() == null || victim.getName().trim().isEmpty()) {
                throw new SQLException("Name cannot be null or empty");
            }
            if (victim.getContactEmail() == null || victim.getContactEmail().trim().isEmpty()) {
                throw new SQLException("ContactEmail cannot be null or empty");
            }
            if (victim.getPasswordHash() == null || victim.getPasswordHash().trim().isEmpty()) {
                throw new SQLException("PasswordHash cannot be null or empty");
            }
            if (victim.getAccountStatus() == null || victim.getAccountStatus().trim().isEmpty()) {
                victim.setAccountStatus("Active"); // Set default if not provided
            }

            stmt.setString(1, victim.getName().trim());
            stmt.setString(2, victim.getContactEmail().trim().toLowerCase());
            stmt.setString(3, victim.getPasswordHash());
            stmt.setString(4, victim.getAccountStatus());

            System.out.println("Executing INSERT: Name=" + victim.getName() + ", Email=" + victim.getContactEmail());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                // Get the generated ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedID = generatedKeys.getInt(1);
                        victim.setVictimID(generatedID);
                        System.out.println("Generated VictimID: " + generatedID);
                    } else {
                        System.err.println("Warning: No generated keys returned");
                    }
                }
                return true;
            } else {
                System.err.println("Warning: INSERT executed but no rows affected");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in VictimDAOImpl.create(): " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw e; // Re-throw to let caller handle
        }
    }

    @Override
    public boolean update(Victim victim) throws SQLException {
        String sql = "UPDATE Victims SET Name = ?, ContactEmail = ?, PasswordHash = ?, AccountStatus = ? WHERE VictimID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, victim.getName());
            stmt.setString(2, victim.getContactEmail());
            stmt.setString(3, victim.getPasswordHash());
            stmt.setString(4, victim.getAccountStatus());
            stmt.setInt(5, victim.getVictimID());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public boolean delete(int victimID) throws SQLException {
        String sql = "DELETE FROM Victims WHERE VictimID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimID);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    @Override
    public boolean updateAccountStatus(int victimID, String newStatus) throws SQLException {
        String sql = "UPDATE Victims SET AccountStatus = ? WHERE VictimID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, victimID);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Helper method to map ResultSet to Victim object
     */
    private Victim mapResultSetToVictim(ResultSet rs) throws SQLException {
        Victim victim = new Victim();
        victim.setVictimID(rs.getInt("VictimID"));
        victim.setName(rs.getString("Name"));
        victim.setContactEmail(rs.getString("ContactEmail"));
        victim.setPasswordHash(rs.getString("PasswordHash"));
        victim.setAccountStatus(rs.getString("AccountStatus"));

        String dateStr = rs.getString("DateCreated");
        if (dateStr != null) {
            victim.setDateCreated(DateUtils.fromDatabaseFormat(dateStr));
        }

        return victim;
    }
}

