/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.sublist;

import java.io.Serializable;

import org.roda.core.data.common.RodaConstants;

/**
 * @author Rui Castro
 */
public class Sublist implements Serializable {
  private static final long serialVersionUID = 5274598970369555552L;
  public static final Sublist NONE = new Sublist(0, 0);
  /**
   * 20170512 hsilva: this should not be used at all and, because it can be very
   * harmful for Solr, I'll set the maximumElementCount to a default value
   * (which is a very low number like 100)
   */
  @Deprecated
  public static final Sublist ALL = new Sublist(0, RodaConstants.DEFAULT_PAGINATION_VALUE);

  private int firstElementIndex = 0;
  private int maximumElementCount = 10;

  /**
   * Constructs an empty {@link Sublist}.
   */
  public Sublist() {
    // do nothing
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
  @Override
  public String toString() {
    return "Sublist(firstElementIndex=" + getFirstElementIndex() + ", maximumElementCount=" + getMaximumElementCount()
      + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + firstElementIndex;
    result = prime * result + maximumElementCount;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Sublist)) {
      return false;
    }
    Sublist other = (Sublist) obj;
    if (firstElementIndex != other.firstElementIndex) {
      return false;
    }
    if (maximumElementCount != other.maximumElementCount) {
      return false;
    }
    return true;
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
  public Sublist setFirstElementIndex(int firstElementIndex) {
    this.firstElementIndex = firstElementIndex;
    return this;
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
