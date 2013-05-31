package pt.gov.dgarq.roda.common.convert.db.model.structure;

import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Luis Faria
 */
public class PrimaryKey {

	private List<String> columnNames;

	/**
	 * Empty primary key constructor
	 * 
	 * 
	 */
	public PrimaryKey() {
		this.columnNames = new Vector<String>();
	}

	/**
	 * Create a primary key. A primary can be composed by a sequence of columns.
	 * 
	 * @param columnNames
	 *            the name of the columns that compose this primary key
	 * 
	 */
	public PrimaryKey(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	/**
	 * @return the name of the columns that compose this primary key
	 */
	public List<String> getColumnNames() {
		return columnNames;
	}

	/**
	 * @param columnNames
	 *            the name of the columns that compose this primary key
	 */
	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

}
