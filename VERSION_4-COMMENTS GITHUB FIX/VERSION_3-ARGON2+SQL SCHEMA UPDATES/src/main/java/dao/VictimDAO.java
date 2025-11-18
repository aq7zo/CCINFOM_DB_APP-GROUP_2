package dao;

import model.Victim;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for the Victim entity.
 * Defines CRUD (Create, Read, Update, Delete) operations and other database interactions for victims.
 */
public interface VictimDAO {

    /**
     * Finds a victim by their email.
     *
     * @param email Victim's email address.
     * @return Victim object if found; otherwise, null.
     * @throws SQLException if a database access error occurs.
     */
    Victim findByEmail(String email) throws SQLException;

    /**
     * Finds a victim by their unique ID.
     *
     * @param victimID Victim's unique ID.
     * @return Victim object if found; otherwise, null.
     * @throws SQLException if a database access error occurs.
     */
    Victim findById(int victimID) throws SQLException;

    /**
     * Retrieves all victims from the database.
     *
     * @return List of all Victim objects.
     * @throws SQLException if a database access error occurs.
     */
    List<Victim> findAll() throws SQLException;

    /**
     * Creates a new victim record in the database.
     *
     * @param victim Victim object containing data to insert.
     * @return true if creation was successful; false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean create(Victim victim) throws SQLException;

    /**
     * Updates an existing victim record in the database.
     *
     * @param victim Victim object containing updated data.
     * @return true if update was successful; false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean update(Victim victim) throws SQLException;

    /**
     * Deletes a victim record from the database by ID.
     *
     * @param victimID ID of the victim to delete.
     * @return true if deletion was successful; false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean delete(int victimID) throws SQLException;

    /**
     * Updates only the account status field of a victim.
     *
     * @param victimID  ID of the victim to update.
     * @param newStatus New account status value.
     * @return true if the status update was successful; false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    boolean updateAccountStatus(int victimID, String newStatus) throws SQLException;
}
