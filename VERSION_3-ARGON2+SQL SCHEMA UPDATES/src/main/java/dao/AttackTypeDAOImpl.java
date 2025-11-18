package dao;

import model.AttackType;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttackTypeDAOImpl implements AttackTypeDAO {

    @Override
    public AttackType findById(int attackTypeID) throws SQLException {
        String sql = "SELECT * FROM AttackTypes WHERE AttackTypeID = ?";
        return executeQuery(sql, stmt -> stmt.setInt(1, attackTypeID));
    }

    @Override
    public AttackType findByName(String attackName) throws SQLException {
        String sql = "SELECT * FROM AttackTypes WHERE AttackName = ?";
        return executeQuery(sql, stmt -> stmt.setString(1, attackName));
    }

    @Override
    public List<AttackType> findAll() throws SQLException {
        List<AttackType> list = new ArrayList<>();
        String sql = "SELECT * FROM AttackTypes ORDER BY SeverityLevel DESC, AttackName";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAttackType(rs));
            }
        }
        return list;
    }

    @Override
    public boolean create(AttackType attackType) throws SQLException {
        String sql = "INSERT INTO AttackTypes (AttackName, Description, SeverityLevel) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, attackType.getAttackName());
            stmt.setString(2, attackType.getDescription());
            stmt.setString(3, attackType.getSeverityLevel());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        attackType.setAttackTypeID(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(AttackType attackType) throws SQLException {
        String sql = "UPDATE AttackTypes SET AttackName = ?, Description = ?, SeverityLevel = ? WHERE AttackTypeID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, attackType.getAttackName());
            stmt.setString(2, attackType.getDescription());
            stmt.setString(3, attackType.getSeverityLevel());
            stmt.setInt(4, attackType.getAttackTypeID());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int attackTypeID) throws SQLException {
        String sql = "DELETE FROM AttackTypes WHERE AttackTypeID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attackTypeID);
            return stmt.executeUpdate() > 0;
        }
    }

    private AttackType executeQuery(String sql, PreparedStatementSetter setter) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setter.setValues(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttackType(rs);
                }
            }
        }
        return null;
    }

    private AttackType mapResultSetToAttackType(ResultSet rs) throws SQLException {
        AttackType at = new AttackType();
        at.setAttackTypeID(rs.getInt("AttackTypeID"));
        at.setAttackName(rs.getString("AttackName"));
        at.setDescription(rs.getString("Description"));
        at.setSeverityLevel(rs.getString("SeverityLevel"));
        return at;
    }

    @FunctionalInterface
    private interface PreparedStatementSetter {
        void setValues(PreparedStatement stmt) throws SQLException;
    }
}