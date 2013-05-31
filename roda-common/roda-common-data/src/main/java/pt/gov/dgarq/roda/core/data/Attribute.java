package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;

/**
 * This is a simple attribute with name and value.
 * 
 * @author Rui Castro
 */
public class Attribute implements Serializable {
	private static final long serialVersionUID = -1200920433672324166L;

	private String name = null;
	private String value = null;

	/**
	 * Constructs a new {@link Attribute}.
	 */
	public Attribute() {
	}

	/**
	 * Constructs a new {@link Attribute} cloning an existing {@link Attribute}.
	 * 
	 * @param attribute
	 *            the {@link Attribute} to clone.
	 */
	public Attribute(Attribute attribute) {
		this(attribute.getName(), attribute.getValue());
	}

	/**
	 * Constructs a new {@link Attribute} with the given parameters.
	 * 
	 * @param name
	 *            the name of the Attribute.
	 * @param value
	 *            the value of the {@link Attribute}.
	 */
	public Attribute(String name, String value) {
		setName(name);
		setValue(value);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Attribute(name=" + getName() + ", value=" + getValue() + ")";
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Attribute) {
			Attribute other = (Attribute) obj;
			return getName() == other.getName()
					|| getName().equals(other.getName());
		} else {
			return false;
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
