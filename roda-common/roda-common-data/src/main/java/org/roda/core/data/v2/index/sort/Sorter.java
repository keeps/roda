/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.sort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class Sorter implements Serializable {
  private static final long serialVersionUID = 4255866410869992178L;

  public static final Sorter NONE = new Sorter();

  private List<SortParameter> parameters = new ArrayList<>();

  /**
   * Constructs an empty {@link Sorter}.
   */
  public Sorter() {
    // do nothing
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
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
    if (!(obj instanceof Sorter)) {
      return false;
    }
    Sorter other = (Sorter) obj;
    if (parameters == null) {
      if (other.parameters != null) {
        return false;
      }
    } else if (!parameters.equals(other.parameters)) {
      return false;
    }
    return true;
  }

  /**
   * @see Object#toString()
   */
  @Override
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
    return parameters.toArray(new SortParameter[parameters.size()]);
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
