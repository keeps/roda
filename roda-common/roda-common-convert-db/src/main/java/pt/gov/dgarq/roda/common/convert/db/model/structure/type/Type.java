/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.structure.type;

/**
 * @author Luis Faria
 * 
 * Abstract definition of column type. All column type implementations must
 * extend this class.
 */
public abstract class Type {

	private String originalTypeName;

	private String description;

	/**
	 * Type abstract empty constructor
	 * 
	 */
	public Type() {
		description = null;
		originalTypeName = null;
	}

	/**
	 * Type abstract constructor
	 * 
	 * @param originalTypeName
	 *            the name of the original type, null if not applicable
	 * @param description
	 *            the type description, null if none
	 */
	public Type(String originalTypeName, String description) {
		this.originalTypeName = originalTypeName;
		this.description = description;
	}

	/**
	 * @return the name of the original type, null if not applicable
	 */
	public String getOriginalTypeName() {
		return originalTypeName;
	}

	/**
	 * @param originalTypeName
	 *            the name of the original type, null if not applicable
	 */
	public void setOriginalTypeName(String originalTypeName) {
		this.originalTypeName = originalTypeName;
	}

	/**
	 * @return the type description, null if none
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the type description, null for none
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
