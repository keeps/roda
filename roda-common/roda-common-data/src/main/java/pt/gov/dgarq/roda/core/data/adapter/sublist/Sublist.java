package pt.gov.dgarq.roda.core.data.adapter.sublist;

import java.io.Serializable;

/**
 * @author Rui Castro
 */
public class Sublist implements Serializable {
  private static final long serialVersionUID = 5274598970369555552L;

  private int firstElementIndex = 0;
  private int maximumElementCount = 10;

  /**
   * Constructs an empty {@link Sublist}.
   */
  public Sublist() {
  }

  /**
   * Constructs a {@link Sublist} cloning an existing {@link Sublist}.
   * 
   * @param sublist
   *          the {@link Sublist} to clone.
   */
  public Sublist(Sublist sublist) {
    this(sublist.getFirstElementIndex(), sublist.getMaximumElementCount());
  }

  /**
   * Constructs a {@link Sublist} with the given parameters.
   * 
   * @param firstElementIndex
   * @param maximumElementCount
   */
  public Sublist(int firstElementIndex, int maximumElementCount) {
    setFirstElementIndex(firstElementIndex);
    setMaximumElementCount(maximumElementCount);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Sublist(firstElementIndex=" + getFirstElementIndex() + ", maximumElementCount=" + getMaximumElementCount()
      + ")";
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = true;

    if (obj != null && obj instanceof Sublist) {
      Sublist other = (Sublist) obj;
      equal = equal && (getFirstElementIndex() == other.getFirstElementIndex());
      equal = equal && (getMaximumElementCount() == other.getMaximumElementCount());
    } else {
      equal = false;
    }

    return equal;
  }

  /**
   * @return the firstElementIndex
   */
  public int getFirstElementIndex() {
    return firstElementIndex;
  }

  /**
   * @param firstElementIndex
   *          the firstElementIndex to set
   */
  public void setFirstElementIndex(int firstElementIndex) {
    this.firstElementIndex = firstElementIndex;
  }

  /**
   * @return the elementCount
   */
  public int getMaximumElementCount() {
    return maximumElementCount;
  }

  /**
   * @param elementCount
   *          the elementCount to set
   */
  public void setMaximumElementCount(int elementCount) {
    this.maximumElementCount = elementCount;
  }

}
