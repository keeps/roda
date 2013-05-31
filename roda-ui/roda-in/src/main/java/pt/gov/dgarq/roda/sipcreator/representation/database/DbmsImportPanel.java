/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation.database;

import javax.swing.JPanel;

import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;

/**
 * @author Luis Faria
 * 
 */
public interface DbmsImportPanel {

	/**
	 * Get the JPanel where to configure the import
	 * 
	 * @return the panel
	 */
	public JPanel getPanel();

	/**
	 * Check if all inserted info is valid
	 * 
	 * @return true if valid, false otherwise
	 */
	public boolean isInfoValid();

	/**
	 * Get the database import module
	 * 
	 * @return the database import module or null if info not valid
	 * @throws ModuleException
	 */
	public DatabaseImportModule getDatabaseImportModule()
			throws ModuleException;

	/**
	 * Get the string to use in the select combo box
	 * 
	 * @return A description string
	 */
	public String toString();

}
