/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.util.List;

/**
 * This filter match one the of the values.
 * 
 * @author Rui Castro
 */
public class OneOfManyFilterParameter extends FilterParameter {
  private static final long serialVersionUID = -8705013718226758378L;

  private List<String> values = null;

  /**
   * Constructs an empty {@link OneOfManyFilterParameter}.
   */
  public OneOfManyFilterParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link OneOfManyFilterParameter} cloning an existing
   * {@link OneOfManyFilterParameter}.
   * 
   * @param oneOfManyFilterParameter
   *          the {@link OneOfManyFilterParameter} to clone.
   */
  public OneOfManyFilterParameter(OneOfManyFilterParameter oneOfManyFilterParameter) {
    this(oneOfManyFilterParameter.getName(), oneOfManyFilterParameter.getValues());
  }

  /**
   * Constructs a {@link OneOfManyFilterParameter} from a list of values.
   * 
   * @param name
   *          the name of the attribute.
   * @param values
   *          the list of values for this filter.
   */
  public OneOfManyFilterParameter(String name, List<String> values) {
    setName(name);
    setValues(values);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "OneOfManyFilterParameter(name=" + getName() + ", values=" + getValues() + ")";
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
    if (!(obj instanceof OneOfManyFilterParameter)) {
      return false;
    }
    OneOfManyFilterParameter other = (OneOfManyFilterParameter) obj;
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
  public List<String> getValues() {
    return values;
  }

  /**
   * @param values
   *          the values to set
   */
  public void setValues(List<String> values) {
    this.values = values;
  }

}
