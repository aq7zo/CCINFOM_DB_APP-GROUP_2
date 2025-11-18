package dao;

import model.Perpetrator;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO Interface for managing Perpetrator data.
 * Defines CRUD operations and lookup utilities used by the incident reporting system.
 */
public interface PerpetratorDAO {

    /**
     * Finds a perpetrator using a unique identifying string.
     * This may include phone number, email address, social media URL,
     * or any field the system considers unique.
     *
     * @param identifier A unique string used to identify a perpetrator.
     * @return The matching Perpetrator object, or null if not found.
     * @throws SQLException If a database access error occurs.
     */
    Perpetrator findByIdentifier(String identifier) throws SQLException;

    /**
     * Retrieves a perpetrator by their numeric database ID.
     *
     * @param perpetratorID The primary key of the perpetrator.
     * @return The matching Perpetrator object, or null if not found.
     * @throws SQLException If a database access error occurs.
     */
    Perpetrator findById(int perpetratorID) throws SQLException;

    /**
     * Retrieves all perpetrators stored in the system.
     *
     * @return A list of all perpetrators. May be empty, but never null.
     * @throws SQLException If a database access error occurs.
     */
    List<Perpetrator> findAll() throws SQLException;

    /**
     * Inserts a new perpetrator into the database.
     *
     * @param perpetrator The perpetrator object containing details to save.
     * @return true if creation succeeded, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    boolean create(Perpetrator perpetrator) throws SQLException;

    /**
     * Updates an existing perpetrator record.
     * Typically used to modify fields such as threat level,
     * incident count, or updated profile information.
     *
     * @param perpetrator The updated perpetrator object.
     * @return true if update succeeded, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    boolean update(Perpetrator perpetrator) throws SQLException;

    /**
     * Deletes a perpetrator record from the system.
     * Should be used with caution, as this may impact
     * incident history or reporting logic.
     *
     * @param perpetratorID The perpetrator ID to delete.
     * @return true if deletion succeeded, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    boolean delete(int perpetratorID) throws SQLException;

    /**
     * Performs an upsert-like operation.
     * If the perpetrator already exists (e.g., found by identifier),
     * the existing record is updated. Otherwise, a new record is created.
     *
     * @param perpetrator The perpetrator data to create or update.
     * @return The newly created or updated Perpetrator object.
     * @throws SQLException If a database access error occurs.
     */
    Perpetrator createOrUpdate(Perpetrator perpetrator) throws SQLException;
}
