package pt.gov.dgarq.roda.common.convert.db.model.structure;

/**
 * 
 * @author MarcoPalos
 */
public class ForeignKey {

	private String id;

	private String name;

	private String refTable;

	private String refColumn;

	/**
	 * Creates a new instance of ForeignKey
	 * 
	 * @param id
	 *            the unique id for the foreign key
	 * @param name
	 *            the name of the foreign key in this table
	 * @param refTable
	 *            the table which the foreign key refers to
	 * @param refColumn
	 *            the name of the column which the foreign key refers to
	 */
	public ForeignKey(String id, String name, String refTable, String refColumn) {
		this.id = id;
		this.name = name;
		this.refTable = new String(refTable);
		this.refColumn = new String(refColumn);
	}

	/**
	 * @return the unique id of the foreign key
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the unique id for the foreign key
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name of the foreign key in this table
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name of the foreign key in this table
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name of the column which the foreign key refers to
	 */
	public String getRefColumn() {
		return refColumn;
	}

	/**
	 * @return the table which the foreign key refers to
	 */
	public String getRefTable() {
		return refTable;
	}

	/**
	 * @param refColumn
	 *            the name of the column which the foreign key refers to
	 */
	public void setRefColumn(String refColumn) {
		this.refColumn = refColumn;
	}

	/**
	 * @param refTable
	 *            the table which the foreign key refers to
	 */
	public void setRefTable(String refTable) {
		this.refTable = refTable;
	}

}
