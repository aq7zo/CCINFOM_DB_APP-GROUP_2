package dao;

import model.Administrator;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for the Administrator entity.
 *
 * Defines the complete set of CRUD operations required for managing administrator accounts
 * in the PhishNet system. All methods interact directly with the Administrators table
 * and throw SQLException to allow callers (typically service classes) to handle
 * database errors appropriately.
 *
 * This interface is implemented by AdministratorDAOImpl and used by service-layer
 * classes such as AdministratorService and AdminAuthenticationService.
 *
 * Important: Passwords are stored only as secure Argon2id hashes — never in plaintext.
 */
public interface AdministratorDAO {

    /**
     * Retrieves an administrator account by their contact email address.
     *
     * Used primarily during login/authentication.
     *
     * @param email the administrator's email (case-insensitive in practice, but stored as-is)
     * @return fully populated Administrator object if found, null if no match
     * @throws SQLException if a database access error occurs
     */
    Administrator findByEmail(String email) throws SQLException;

    /**
     * Retrieves an administrator account by their unique numeric ID.
     *
     * Used when loading admin details for session management, audit logs, or updates.
     *
     * @param adminID the unique administrator ID (primary key)
     * @return Administrator object if found, null otherwise
     * @throws SQLException if a database access error occurs
     */
    Administrator findById(int adminID) throws SQLException;

    /**
     * Returns all administrator accounts in the system.
     *
     * Used in admin management panels and for generating audit reports.
     *
     * @return List of all administrators (never null, may be empty)
     * @throws SQLException if a database access error occurs
     */
    List<Administrator> findAll() throws SQLException;

    /**
     * Creates a new administrator account in the database.
     *
     * Typically used during initial system setup or when adding new staff.
     * The password must already be hashed using SecurityUtils.hashPassword() before calling.
     *
     * @param admin Administrator object with all required fields (especially password hash)
     * @return true if the INSERT was successful and one row was affected
     * @throws SQLException if a database access error or constraint violation occurs
     */
    boolean create(Administrator admin) throws SQLException;

    /**
     * Updates an existing administrator's information.
     *
     * Supports updating name, role, email, and password (if rehashed).
     * AdminID must be valid and present.
     *
     * @param admin Administrator object containing updated values
     * @return true if the UPDATE affected exactly one row
     * @throws SQLException if a database access error occurs
     */
    boolean update(Administrator admin) throws SQLException;

    /**
     * Permanently deletes an administrator account by ID.
     *
     * Use with extreme caution — this removes the user completely.
     * Consider soft-delete or deactivation in production systems.
     *
     * @param adminID the ID of the administrator to delete
     * @return true if one row was successfully deleted
     * @throws SQLException if a database access error occurs
     */
    boolean delete(int adminID) throws SQLException;
}