/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.structure.type;

/**
 * A sequence of values of the same type.
 * 
 * @author Luis Faria
 */
public class ComposedTypeArray extends Type {
	private Type elementType;

	/**
	 * Empty Array type constructor.
	 * 
	 */
	public ComposedTypeArray() {
	}

	/**
	 * Array type constructor.
	 * 
	 * @param elementType
	 *            The type of the elements within this array (required).
	 */
	public ComposedTypeArray(Type elementType) {
		this.elementType = elementType;
	}

	/**
	 * @return The type of the elements within this array
	 */
	public Type getElementType() {
		return elementType;
	}

	/**
	 * @param elementType
	 *            The type of the elements within this array
	 */
	public void setElementType(Type elementType) {
		this.elementType = elementType;
	}

}
