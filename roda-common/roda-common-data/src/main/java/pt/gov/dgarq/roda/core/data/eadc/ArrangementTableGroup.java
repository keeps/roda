package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;

/**
 * 
 * @author Rui Castro
 */
public class ArrangementTableGroup implements Serializable {
	private static final long serialVersionUID = -2627829548140025727L;

	private int columns = -1;

	private ArrangementTableHead head = null;

	private ArrangementTableBody body = null;

	/**
	 * Constructs a new empty {@link ArrangementTableGroup}.
	 */
	public ArrangementTableGroup() {
	}

	/**
	 * Constructs a new {@link ArrangementTableGroup} clonning an existing
	 * {@link ArrangementTableGroup}.
	 * @param tGroup 
	 */
	public ArrangementTableGroup(ArrangementTableGroup tGroup) {
		this(tGroup.getColumns(), tGroup.getHead(), tGroup.getBody());
	}

	/**
	 * @param columns
	 * @param head
	 * @param body
	 */
	public ArrangementTableGroup(int columns, ArrangementTableHead head,
			ArrangementTableBody body) {
		setColumns(columns);
		setHead(head);
		setBody(body);
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ArrangementTableGroup) {
			ArrangementTableGroup other = (ArrangementTableGroup) obj;
			return (this.getColumns() == other.getColumns())
					&& (this.getHead() == other.getHead() || this.getHead()
							.equals(other.getHead()))
					&& (this.getBody() == other.getBody() || this.getBody()
							.equals(other.getBody()));
		} else {
			return false;
		}
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Group columns=" + getColumns() + "\n" + getHead() + "\n"
				+ getBody();
	}

	/**
	 * @return the body
	 */
	public ArrangementTableBody getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(ArrangementTableBody body) {
		if (body != null) {
			this.body = body;
		} else {
			throw new IllegalArgumentException("body cannot be null");
		}
	}

	/**
	 * @return the columns
	 */
	public int getColumns() {
		return columns;
	}

	/**
	 * @param columns
	 *            the columns to set
	 */
	public void setColumns(int columns) {
		this.columns = columns;
	}

	/**
	 * @return the head
	 */
	public ArrangementTableHead getHead() {
		return head;
	}

	/**
	 * @param head
	 *            the head to set
	 */
	public void setHead(ArrangementTableHead head) {
		this.head = head;
	};

}
