package dao;

import model.AttackType;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for managing AttackType records.
 *
 * Provides access to the AttackTypes reference table, which contains
 * predefined categories of cyber threats (e.g., Phishing, Ransomware, DDoS).
 * These types are used when victims submit incident reports and help
 * administrators prioritize and analyze threats effectively.
 *
 * All methods throw SQLException to allow service-layer handling of
 * database errors (logging, rollback, user-friendly messages).
 */
public interface AttackTypeDAO {

    /**
     * Retrieves an attack type by its primary key.
     *
     * @param attackTypeID the unique ID of the attack type
     * @return AttackType object if found, null otherwise
     * @throws SQLException if a database access error occurs
     */
    AttackType findById(int attackTypeID) throws SQLException;

    /**
     * Finds an attack type by its name (case-insensitive).
     *
     * Useful during incident report submission to avoid duplicates
     * and ensure consistent categorization.
     *
     * @param attackName the name of the attack type (e.g., "Business Email Compromise")
     * @return matching AttackType or null if not found
     * @throws SQLException if a database access error occurs
     */
    AttackType findByName(String attackName) throws SQLException;

    /**
     * Returns all defined attack types in the system.
     *
     * Used to populate dropdowns in victim report forms and admin management screens.
     * Typically ordered by SeverityLevel or AttackName.
     *
     * @return List of all attack types (never null)
     * @throws SQLException if a database access error occurs
     */
    List<AttackType> findAll() throws SQLException;

    /**
     * Creates a new attack type entry.
     *
     * Used by administrators to extend the threat catalog as new attack vectors emerge.
     *
     * @param attackType the AttackType object to insert (must have valid name and severity)
     * @return true if insertion was successful and generated ID was retrieved
     * @throws SQLException if a database error or constraint violation occurs
     */
    boolean create(AttackType attackType) throws SQLException;

    /**
     * Updates an existing attack type (e.g., description or severity level).
     *
     * @param attackType the AttackType object with updated values (must include valid AttackTypeID)
     * @return true if exactly one row was updated
     * @throws SQLException if a database access error occurs
     */
    boolean update(AttackType attackType) throws SQLException;

    /**
     * Permanently deletes an attack type by ID.
     *
     * Use with caution — may break referential integrity if existing incidents
     * reference this type. Consider soft-delete in production.
     *
     * @param attackTypeID the ID of the attack type to remove
     * @return true if deletion was successful
     * @throws SQLException if a database error occurs
     */
    boolean delete(int attackTypeID) throws SQLException;
}