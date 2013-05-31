package pt.gov.dgarq.roda.common.convert.db.model.structure;

import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;

/**
 * 
 * @author Luis Faria
 */
public class ColumnStructure {

	private String id;

	private String name;

	private Type type;

	private Boolean nillable;

	private String description;

	/**
	 * ColumnStructure empty constructor
	 * 
	 */
	public ColumnStructure() {
	}

	/**
	 * ColumnStructure constructor
	 * 
	 * @param id
	 *            the column unique id
	 * @param name
	 *            the column name
	 * @param nillable
	 *            if column values can be null
	 * @param description
	 *            column description, optionally null
	 * @param type
	 *            the column type
	 */
	public ColumnStructure(String id, String name, Type type, Boolean nillable,
			String description) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.nillable = nillable;
		this.description = description;

	}

	/**
	 * @return column description, null if none
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            column description, null if none
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the column unique id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the column unique id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the column name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the column name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the column type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the column type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return true if values of this column can be null, false otherwise
	 */
	public Boolean isNillable() {
		return nillable;
	}

	/**
	 * @param nillable
	 *            true if values of this column can be null, false otherwise
	 */
	public void setNillable(Boolean nillable) {
		this.nillable = nillable;
	}

}
