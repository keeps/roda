/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules;

import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;

/**
 * @author Luis Faria
 * 
 */
public interface DatabaseImportModule {

	/**
	 * Import the database model.
	 * 
	 * @param databaseHandler
	 *            The database model handler to be called when importing the
	 *            database.
	 * 
	 * @throws UnknownTypeException
	 *             a type used in the original database structure is unknown and
	 *             cannot be mapped
	 * @throws InvalidDataException
	 *             the database data is not valid
	 * @throws ModuleException
	 *             generic module exception
	 * 
	 */
	public void getDatabase(DatabaseHandler databaseHandler)
			throws ModuleException, UnknownTypeException, InvalidDataException;
}
