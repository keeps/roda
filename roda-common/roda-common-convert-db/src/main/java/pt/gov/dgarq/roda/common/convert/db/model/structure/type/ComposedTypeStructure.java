/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.structure.type;

import java.util.List;
import java.util.Vector;

/**
 * A type composed by structuring other type. Any complex type can be
 * constructed with this type (except recursive types).
 * 
 * @author Luis Faria
 */
public class ComposedTypeStructure extends Type {
	private List<Type> elements;
	
	/**
	 * Empty structured type constructor
	 */
	public ComposedTypeStructure() {
		this.elements = new Vector<Type>();
	}

	/**
	 * Structured type constructor
	 * 
	 * @param elements
	 *            the sequence of types that compose this type (required).
	 */
	public ComposedTypeStructure(List<Type> elements) {
		this.elements = elements;
	}

	/**
	 * @return the sequence of types that compose this type
	 */
	public List<Type> getElements() {
		return elements;
	}

	/**
	 * @param elements
	 *            the sequence of types that compose this type
	 */
	public void setElements(List<Type> elements) {
		this.elements = elements;
	}

}
