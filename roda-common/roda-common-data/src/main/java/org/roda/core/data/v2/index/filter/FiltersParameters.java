/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.util.List;

public abstract class FiltersParameters extends FilterParameter {
  private static final long serialVersionUID = -7444113772637341849L;

  private List<FilterParameter> values = null;

  /**
   * Constructs an empty {@link FiltersParameters}.
   */
  public FiltersParameters() {
  }

  /**
   * Constructs a {@link FiltersParameters} cloning an existing
   * {@link FiltersParameters}.
   * 
   * @param filtersParameters
   *          the {@link FiltersParameters} to clone.
   */
  public FiltersParameters(FiltersParameters filtersParameters) {
    this(filtersParameters.getName(), filtersParameters.getValues());
  }

  /**
   * Constructs a {@link FiltersParameters} from a list of values.
   * 
   * @param name
   *          the name of the attribute.
   * @param values
   *          the list of values for this filter.
   */
  public FiltersParameters(String name, List<FilterParameter> values) {
    setName(name);
    setValues(values);
  }

  public List<FilterParameter> getValues() {
    return values;
  }

  public void setValues(List<FilterParameter> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    return "FiltersParameters [values=" + values + "]";
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
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (!(obj instanceof FiltersParameters))
      return false;
    FiltersParameters other = (FiltersParameters) obj;
    if (values == null) {
      if (other.values != null)
        return false;
    } else if (!values.equals(other.values))
      return false;
    return true;
  }

}
