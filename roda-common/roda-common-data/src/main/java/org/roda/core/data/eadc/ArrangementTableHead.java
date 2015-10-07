package org.roda.core.data.eadc;

import java.util.Arrays;

/**
 * @author Rui Castro
 */
public class ArrangementTableHead extends ArrangementTableBody {
  private static final long serialVersionUID = 5201744676138722608L;

  /**
   * Constructs a new empty {@link ArrangementTableHead}.
   */
  public ArrangementTableHead() {
  }

  /**
   * Constructs a new {@link ArrangementTableHead} clonning an existing
   * {@link ArrangementTableHead}.
   * 
   * @param tHead
   */
  public ArrangementTableHead(ArrangementTableHead tHead) {
    this(tHead.getRows());
  }

  /**
   * @param rows
   */
  public ArrangementTableHead(ArrangementTableRow[] rows) {
    setRows(rows);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Head (rows=" + Arrays.toString(getRows()) + ")";
  }
}
