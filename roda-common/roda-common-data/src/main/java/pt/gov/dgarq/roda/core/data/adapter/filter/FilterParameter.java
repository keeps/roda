package pt.gov.dgarq.roda.core.data.adapter.filter;

import java.io.Serializable;

/**
 * This is a parameter of a {@link Filter}.
 * 
 * @author Rui Castro
 */
public abstract class FilterParameter implements Serializable {
  private static final long serialVersionUID = 3744111668897879761L;

  private String name;

  /**
   * Constructs an empty {@link FilterParameter}.
   */
  public FilterParameter() {
  }

  /**
   * 
   * 
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = true;

    if (obj != null && obj instanceof FilterParameter) {
      FilterParameter other = (FilterParameter) obj;
      equal = equal && (getName() == other.getName() || getName().equals(other.getName()));
    } else {
      equal = false;
    }

    return equal;
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

}
