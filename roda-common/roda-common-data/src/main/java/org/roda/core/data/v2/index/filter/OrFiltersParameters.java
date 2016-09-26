/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.util.List;

public class OrFiltersParameters extends FilterParameter {
  private static final long serialVersionUID = -7444113772637341849L;

  private List<FilterParameter> values = null;

  /**
   * Constructs an empty {@link OrFiltersParameters}.
   */
  public OrFiltersParameters() {
  }

  /**
   * Constructs a {@link OrFiltersParameters} cloning an existing
   * {@link OrFiltersParameters}.
   * 
   * @param orFiltersParameters
   *          the {@link OrFiltersParameters} to clone.
   */
  public OrFiltersParameters(OrFiltersParameters orFiltersParameters) {
    this(orFiltersParameters.getName(), orFiltersParameters.getValues());
  }

  /**
   * Constructs a {@link OrFiltersParameters} from a list of values.
   * 
   * @param name
   *          the name of the attribute.
   * @param values
   *          the list of values for this filter.
   */
  public OrFiltersParameters(String name, List<FilterParameter> values) {
    setName(name);
    setValues(values);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "OrFiltersParameters(name=" + getName() + ", values=" + getValues() + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((values == null) ? 0 : values.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof OrFiltersParameters)) {
      return false;
    }
    OrFiltersParameters other = (OrFiltersParameters) obj;
    if (values == null) {
      if (other.values != null) {
        return false;
      }
    } else if (!values.equals(other.values)) {
      return false;
    }
    return true;
  }

  /**
   * @return the values
   */
  public List<FilterParameter> getValues() {
    return values;
  }

  /**
   * @param values
   *          the values to set
   */
  public void setValues(List<FilterParameter> values) {
    this.values = values;
  }

}
