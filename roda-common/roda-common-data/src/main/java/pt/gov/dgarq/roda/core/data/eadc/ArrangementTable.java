package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Rui Castro
 */
public class ArrangementTable implements EadCValue, Serializable {
  private static final long serialVersionUID = -1230167618301818277L;

  private ArrangementTableGroup[] arrangementTableGroups = null;

  /**
   * Constructs a new empty {@link ArrangementTable}.
   */
  public ArrangementTable() {
  }

  /**
   * Constructs a new {@link ArrangementTable} clonning an existing
   * {@link ArrangementTable}.
   * 
   * @param arrangementTable
   */
  public ArrangementTable(ArrangementTable arrangementTable) {
    this(arrangementTable.getArrangementTableGroups());
  }

  /**
   * Constructs a new {@link ArrangementTable} with the given
   * <code>arrangementTableGroups</code>.
   * 
   * @param arrangementTableGroups
   */
  public ArrangementTable(ArrangementTableGroup[] arrangementTableGroups) {
    setArrangementTableGroups(arrangementTableGroups);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof ArrangementTable) {
      ArrangementTable other = (ArrangementTable) obj;
      return this.arrangementTableGroups == other.arrangementTableGroups
        || Arrays.asList(this.arrangementTableGroups).equals(Arrays.asList(other.arrangementTableGroups));
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {

    ArrangementTableGroup[] arrangementTableGroups2 = getArrangementTableGroups();
    if (arrangementTableGroups2 == null) {
      arrangementTableGroups2 = new ArrangementTableGroup[0];
    }

    return "ArrangementTable( " + Arrays.asList(arrangementTableGroups2) + ")";
  }

  /**
   * @return the arrangementTableGroups
   */
  public ArrangementTableGroup[] getArrangementTableGroups() {
    return arrangementTableGroups;
  }

  /**
   * @param arrangementTableGroups
   *          the arrangementTableGroups to set
   */
  public void setArrangementTableGroups(ArrangementTableGroup[] arrangementTableGroups) {
    this.arrangementTableGroups = arrangementTableGroups;
  }

}
