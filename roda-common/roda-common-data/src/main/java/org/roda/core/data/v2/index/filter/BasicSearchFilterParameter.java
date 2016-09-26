/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * 
 */
public class BasicSearchFilterParameter extends FilterParameter {
  private static final long serialVersionUID = -2122986808484304112L;

  private String value;

  /**
   * Constructs an empty {@link BasicSearchFilterParameter}.
   */
  public BasicSearchFilterParameter() {
  }

  /**
   * Constructs a {@link BasicSearchFilterParameter} cloning an existing
   * {@link BasicSearchFilterParameter}.
   * 
   * @param basicSearchFilterParameter
   *          the {@link BasicSearchFilterParameter} to clone.
   */
  public BasicSearchFilterParameter(BasicSearchFilterParameter basicSearchFilterParameter) {
    this(basicSearchFilterParameter.getName(), basicSearchFilterParameter.getValue());
  }

  /**
   * Constructs a {@link BasicSearchFilterParameter} with the given parameters.
   * 
   * @param name
   * @param value
   */
  public BasicSearchFilterParameter(String name, String value) {
    setName(name);
    setValue(value);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "BasicSearchFilterParameter(name=" + getName() + ", value=" + getValue() + ")";
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    if (!(obj instanceof BasicSearchFilterParameter)) {
      return false;
    }
    BasicSearchFilterParameter other = (BasicSearchFilterParameter) obj;
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }
}
