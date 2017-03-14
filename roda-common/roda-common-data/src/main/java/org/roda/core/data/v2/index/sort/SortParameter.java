/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.sort;

import java.io.Serializable;

/**
 * @author Rui Castro
 */
public class SortParameter implements Serializable {
  private static final long serialVersionUID = 5682003551885541798L;

  private String name;
  private boolean descending = false;

  /**
   * Constructs an empty {@link SortParameter}.
   */
  public SortParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link SortParameter} cloning an existing
   * {@link SortParameter}.
   * 
   * @param sortParameter
   *          the {@link SortParameter} to clone.
   */
  public SortParameter(SortParameter sortParameter) {
    this(sortParameter.getName(), sortParameter.isDescending());
  }

  /**
   * Constructs a {@link SortParameter} with the given parameters.
   * 
   * @param name
   *          the name of the attribute to sort
   * @param descending
   *          descending or ascending order.
   */
  public SortParameter(String name, boolean descending) {
    setName(name);
    setDescending(descending);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "SortParameter(name=" + getName() + ", descending=" + isDescending() + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (descending ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    if (!(obj instanceof SortParameter)) {
      return false;
    }
    SortParameter other = (SortParameter) obj;
    if (descending != other.descending) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the descending
   */
  public boolean isDescending() {
    return descending;
  }

  /**
   * @param descending
   *          the descending to set
   */
  public void setDescending(boolean descending) {
    this.descending = descending;
  }

}
