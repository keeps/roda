package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Rui Castro
 */
public class ArrangementTableBody implements Serializable {
  private static final long serialVersionUID = 2987788730658602667L;

  private ArrangementTableRow[] rows = null;

  /**
	 */
  public ArrangementTableBody() {
  }

  /**
   * Constructs a new {@link ArrangementTableHead} clonning an existing
   * {@link ArrangementTableHead}.
   * 
   * @param tBody
   */
  public ArrangementTableBody(ArrangementTableBody tBody) {
    this(tBody.getRows());
  }

  /**
   * @param rows
   */
  public ArrangementTableBody(ArrangementTableRow[] rows) {
    this.rows = rows;
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof ArrangementTableBody) {
      ArrangementTableBody other = (ArrangementTableBody) obj;
      // return equals(this.rows, other.rows);
      return Arrays.asList(this.rows).equals(Arrays.asList(other.rows));
    } else {
      return false;
    }
  }

  /**
   * Compare two rows
   * 
   * @param rows1
   * @param rows2
   * @return true if rows have the same length and all items are equal and in
   *         the same order
   */
  private boolean equals(Object[] rows1, Object[] rows2) {
    boolean equal = true;
    if (rows1.length == rows2.length) {
      for (int i = 0; i < rows1.length && equal; i++) {
        equal &= rows1[i].equals(rows2[i]);
      }
    } else {
      equal = false;
    }
    return equal;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Body " + Arrays.asList(rows);
  }

  /**
   * @return the rows
   */
  public ArrangementTableRow[] getRows() {
    return rows;
  }

  /**
   * @param rows
   *          the rows to set
   */
  public void setRows(ArrangementTableRow[] rows) {
    this.rows = rows;
  }

}
