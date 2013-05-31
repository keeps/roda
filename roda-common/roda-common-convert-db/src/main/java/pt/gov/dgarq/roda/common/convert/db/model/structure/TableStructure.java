/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Faria
 * 
 */
public class TableStructure {
	private String id;

	private String name;

	private String description;

	private List<ColumnStructure> columns;

	private List<ForeignKey> foreignKeys;

	private PrimaryKey primaryKey;

	/**
	 * Empty table constructor. All fields are null except columns and foreign
	 * keys, which are empty lists
	 * 
	 */
	public TableStructure() {
		id = null;
		name = null;
		description = null;
		columns = new ArrayList<ColumnStructure>();
		foreignKeys = new ArrayList<ForeignKey>();
		primaryKey = null;
	}

	/**
	 * TableStructure constructor
	 * 
	 * @param id
	 *            the table id
	 * @param name
	 *            the table name
	 * @param description
	 *            the table description, optionally null
	 * @param columns
	 *            the table columns
	 * @param foreignKeys
	 *            foreign keys definition
	 * @param primaryKey
	 *            primary key definition
	 */
	public TableStructure(String id, String name, String description,
			List<ColumnStructure> columns, List<ForeignKey> foreignKeys,
			PrimaryKey primaryKey) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.columns = columns;
		this.foreignKeys = foreignKeys;
		this.primaryKey = primaryKey;
	}

	/**
	 * @return the table columns
	 */
	public List<ColumnStructure> getColumns() {
		return columns;
	}

	/**
	 * @param columns
	 *            the table columns
	 */
	public void setColumns(List<ColumnStructure> columns) {
		this.columns = columns;
	}

	/**
	 * @return the table description, null when not defined
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the table description, null when not defined
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the table unique id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the table unique id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the table name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the table name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return existing foreign keys
	 */
	public List<ForeignKey> getForeignKeys() {
		return foreignKeys;
	}

	/**
	 * @param foreignKeys
	 *            existing foreign keys
	 */
	public void setForeignKeys(List<ForeignKey> foreignKeys) {
		this.foreignKeys = foreignKeys;
	}

	/**
	 * @return primary key, null if doesn't exist
	 */
	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey
	 *            primary key, null if doesn't exist
	 */
	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

}
