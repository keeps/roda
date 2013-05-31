package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;

/**
 * This class represents a EAD Component note.
 * 
 * @author Rui Castro
 * 
 * @deprecated {@link Note} is no longer a type in a EadC document.
 */
public class Note implements Serializable {
	private static final long serialVersionUID = 6675330096663546895L;

	private String name;
	private String value;

	/**
	 * Constructs an empty {@link Note}.
	 */
	public Note() {
	}

	/**
	 * Constructs a new {@link Note} clonning an existing one.
	 * 
	 * @param note
	 *            the {@link Note} to clone.
	 */
	public Note(Note note) {
		this(note.getName(), note.getValue());
	}

	/**
	 * Constructs a new {@link Note} with the given parameters.
	 * 
	 * @param name
	 * @param value
	 */
	public Note(String name, String value) {
		setName(name);
		setValue(value);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Note (" + getName() + ", " + getValue() + ")";
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
