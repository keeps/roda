/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules;

import pt.gov.dgarq.roda.common.convert.db.model.data.Row;
import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.DatabaseStructure;

/**
 * @author Luis Faria
 * 
 */
public interface DatabaseHandler {

	/**
	 * Initialize the database, this will be the first method called
	 * 
	 * @throws ModuleException
	 */
	public void initDatabase() throws ModuleException;

	/**
	 * Handle the database structure, this will be the second method called
	 * 
	 * @param structure
	 *            the database structure
	 * @throws ModuleException
	 * @throws UnknownTypeException
	 */
	public void handleStructure(DatabaseStructure structure)
			throws ModuleException, UnknownTypeException;

	/**
	 * Prepare to handle the data of a new table. This method will be called
	 * after the handleStructure, and before each table data will request to be
	 * handled.
	 * 
	 * @param tableId
	 *            the table id
	 * @throws ModuleException
	 */
	public void handleDataOpenTable(String tableId) throws ModuleException;

	/**
	 * Finish handling the data of a table. This method will be called after all
	 * table rows where requested to be handled.
	 * 
	 * @param tableId
	 *            the table id
	 * @throws ModuleException
	 */
	public void handleDataCloseTable(String tableId) throws ModuleException;

	/**
	 * Handle a table row. This method will be called after the table was open
	 * and before it was closed, by row index order.
	 * 
	 * @param row
	 *            the table row
	 * @throws InvalidDataException
	 * @throws ModuleException
	 */
	public void handleDataRow(Row row) throws InvalidDataException,
			ModuleException;

	/**
	 * Finish the database. This method will be called when all data was
	 * requested to be handled. This is the last method.
	 * 
	 * @throws ModuleException
	 */
	public void finishDatabase() throws ModuleException;

}
