/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * @author Rui Castro
 */
public class ArrangementTableRow implements Serializable {
  private static final long serialVersionUID = -521475304664928781L;

  private String[] entries;

  /**
   * Constructs a new empty {@link ArrangementTableRow}.
   */
  public ArrangementTableRow() {
  }

  /**
   * Constructs a new {@link ArrangementTableRow} cloning an existing
   * {@link ArrangementTableRow}.
   * 
   * @param tRow
   *          the {@link ArrangementTableRow} to clone.
   */
  public ArrangementTableRow(ArrangementTableRow tRow) {
    this(tRow.getEntries());
  }

  /**
   * @param entries
   */
  public ArrangementTableRow(String[] entries) {
    this.entries = entries;
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof ArrangementTableRow) {
      ArrangementTableRow other = (ArrangementTableRow) obj;
      return Arrays.asList(this.entries).equals(Arrays.asList(other.entries));
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Row " + Arrays.asList(entries);
  }

  /**
   * @return the entries
   */
  public String[] getEntries() {
    return entries;
  }

  /**
   * @param entries
   *          the entries to set
   */
  public void setEntries(String[] entries) {
    this.entries = entries;
  }

}
