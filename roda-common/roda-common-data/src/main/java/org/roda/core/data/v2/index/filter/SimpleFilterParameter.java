/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

/**
 * @author Rui Castro
 * 
 */
public class SimpleFilterParameter extends FilterParameter {
  private static final long serialVersionUID = -2122986808484304112L;

  private String value;

  /**
   * Constructs an empty {@link SimpleFilterParameter}.
   */
  public SimpleFilterParameter() {
  }

  /**
   * Constructs a {@link SimpleFilterParameter} cloning an existing
   * {@link SimpleFilterParameter}.
   * 
   * @param simpleFilterParameter
   *          the {@link SimpleFilterParameter} to clone.
   */
  public SimpleFilterParameter(SimpleFilterParameter simpleFilterParameter) {
    this(simpleFilterParameter.getName(), simpleFilterParameter.getValue());
  }

  /**
   * Constructs a {@link SimpleFilterParameter} with the given parameters.
   * 
   * @param name
   * @param value
   */
  public SimpleFilterParameter(String name, String value) {
    setName(name);
    setValue(value);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "SimpleFilterParameter(name=" + getName() + ", value=" + getValue() + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
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
    if (!(obj instanceof SimpleFilterParameter)) {
      return false;
    }
    SimpleFilterParameter other = (SimpleFilterParameter) obj;
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
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
}
