package dao;

import dao.AdministratorDAO;
import model.Administrator;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdministratorDAOImpl implements AdministratorDAO {

    @Override
    public Administrator findByEmail(String email) throws SQLException {
        // Trim email and use case-insensitive comparison for better compatibility
        String query = "SELECT * FROM Administrators WHERE LOWER(TRIM(ContactEmail)) = LOWER(TRIM(?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAdministrator(rs);
            }
        }
        return null;
    }

    @Override
    public Administrator findById(int adminID) throws SQLException {
        String query = "SELECT * FROM Administrators WHERE AdminID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, adminID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAdministrator(rs);
            }
        }
        return null;
    }

    @Override
    public List<Administrator> findAll() throws SQLException {
        List<Administrator> admins = new ArrayList<>();
        String query = "SELECT * FROM Administrators";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                admins.add(mapResultSetToAdministrator(rs));
            }
        }
        return admins;
    }

    @Override
    public boolean create(Administrator admin) throws SQLException {
        String query = "INSERT INTO Administrators (Name, Role, ContactEmail, PasswordHash) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, admin.getName());
            stmt.setString(2, admin.getRole());
            stmt.setString(3, admin.getContactEmail());
            stmt.setString(4, admin.getPasswordHash());

            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    admin.setAdminID(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Administrator admin) throws SQLException {
        String query = "UPDATE Administrators SET Name = ?, Role = ?, ContactEmail = ?, PasswordHash = ? WHERE AdminID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, admin.getName());
            stmt.setString(2, admin.getRole());
            stmt.setString(3, admin.getContactEmail());
            stmt.setString(4, admin.getPasswordHash());
            stmt.setInt(5, admin.getAdminID());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int adminID) throws SQLException {
        String query = "DELETE FROM Administrators WHERE AdminID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, adminID);
            return stmt.executeUpdate() > 0;
        }
    }

    private Administrator mapResultSetToAdministrator(ResultSet rs) throws SQLException {
        Administrator admin = new Administrator();
        admin.setAdminID(rs.getInt("AdminID"));
        admin.setName(rs.getString("Name"));
        admin.setRole(rs.getString("Role"));
        admin.setContactEmail(rs.getString("ContactEmail"));
        admin.setPasswordHash(rs.getString("PasswordHash"));

        Timestamp timestamp = rs.getTimestamp("DateAssigned");
        if (timestamp != null) {
            admin.setDateAssigned(timestamp.toLocalDateTime());
        }
        return admin;
    }
}

