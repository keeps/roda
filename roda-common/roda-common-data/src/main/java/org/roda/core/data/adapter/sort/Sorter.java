/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.adapter.sort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rui Castro
 */
public class Sorter implements Serializable {
  private static final long serialVersionUID = 4255866410869992178L;

  private List<SortParameter> parameters = new ArrayList<SortParameter>();

  /**
   * Constructs an empty {@link Sorter}.
   */
  public Sorter() {
  }

  /**
   * Constructs a {@link Sorter} cloning an existing {@link Sorter}.
   * 
   * @param sorter
   *          the {@link Sorter} to clone.
   */
  public Sorter(Sorter sorter) {
    this(sorter.getParameters());
  }

  public Sorter(SortParameter parameter) {
    add(parameter);
  }

  /**
   * Constructs a {@link Sorter} with the given parameters.
   * 
   * @param parameters
   *          the sort parameters.
   */
  public Sorter(SortParameter[] parameters) {
    setParameters(parameters);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = true;

    if (obj != null && obj instanceof Sorter) {
      Sorter other = (Sorter) obj;
      equal = equal && (getParameters() == other.getParameters() || getParameters().equals(other.getParameters()));
    } else {
      equal = false;
    }

    return equal;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    if (getParameters() != null) {
      return "Sorter (" + Arrays.asList(getParameters()) + ")";
    } else {
      return "Sorter ()";
    }
  }

  /**
   * @return the parameters
   */
  public SortParameter[] getParameters() {
    return (SortParameter[]) parameters.toArray(new SortParameter[parameters.size()]);
  }

  /**
   * @param parameters
   *          the parameters to set
   */
  public void setParameters(SortParameter[] parameters) {
    this.parameters.clear();
    add(parameters);
  }

  /**
   * Adds the given parameters.
   * 
   * @param parameters
   */
  public void add(SortParameter[] parameters) {
    if (parameters != null) {
      this.parameters.addAll(Arrays.asList(parameters));
    }
  }

  /**
   * Adds the given parameter.
   * 
   * @param parameter
   */
  public void add(SortParameter parameter) {
    if (parameters != null) {
      this.parameters.add(parameter);
    }
  }

}
