/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This is a filter of data. It's used by some service methods that deal with
 * sets or lists, to filter the elements in the set or list.
 *
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
@JsonIgnoreProperties({"returnLite"})
public class Filter implements Serializable {
  @Serial
  private static final long serialVersionUID = -5544859696646804386L;

  public static final Filter ALL = new Filter(new AllFilterParameter());
  public static final Filter NULL = null;

  private List<FilterParameter> parameters = new ArrayList<>();

  /**
   * Constructs an empty {@link Filter}.
   */
  public Filter() {
    super();
  }

  /**
   * Constructs a {@link Filter} cloning an existing {@link Filter}.
   *
   * @param filter
   *          the {@link Filter} to clone.
   */
  public Filter(Filter filter) {
    this(filter.getParameters());
  }

  /**
   * Constructs a {@link Filter} with a single parameter.
   *
   * @param parameter
   */
  public Filter(FilterParameter parameter) {
    add(parameter);
  }

  public Filter(FilterParameter... parameters) {
    List<FilterParameter> parameterList = new ArrayList<>();
    for (FilterParameter parameter : parameters) {
      parameterList.add(parameter);
    }
    setParameters(parameterList);
  }

  /**
   * Constructs a {@link Filter} with the given parameters.
   *
   * @param parameters
   */
  public Filter(List<FilterParameter> parameters) {
    setParameters(parameters);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "Filter [parameters=" + parameters + "]";
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
    if (!(obj instanceof Filter)) {
      return false;
    }
    Filter other = (Filter) obj;
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
   * Gets the list of {@link FilterParameter}s.
   *
   * @return an array of {@link FilterParameter} with this filter parameters.
   */
  public List<FilterParameter> getParameters() {
    return parameters;
  }

  /**
   * Sets this filter's {@link FilterParameter}s.
   *
   * @param parameters
   *          an array of {@link FilterParameter} to set.
   */
  public void setParameters(List<FilterParameter> parameters) {
    this.parameters.clear();
    this.parameters.addAll(parameters);
  }

  /**
   * Adds the given parameter.
   *
   * @param parameter
   * @return
   */
  public Filter add(FilterParameter parameter) {
    if (parameter != null) {
      this.parameters.add(parameter);
    }
    return this;
  }

  public void add(List<FilterParameter> parameters) {
    if (parameters != null) {
      this.parameters.addAll(parameters);
    }
  }

}
