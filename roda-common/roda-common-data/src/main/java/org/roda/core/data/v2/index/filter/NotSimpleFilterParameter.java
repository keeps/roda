/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class NotSimpleFilterParameter extends FilterParameter {
  private static final long serialVersionUID = -2122986808484304112L;

  private String value;

  /**
   * Constructs an empty {@link NotSimpleFilterParameter}.
   */
  public NotSimpleFilterParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link NotSimpleFilterParameter} cloning an existing
   * {@link NotSimpleFilterParameter}.
   * 
   * @param simpleFilterParameter
   *          the {@link NotSimpleFilterParameter} to clone.
   */
  public NotSimpleFilterParameter(NotSimpleFilterParameter simpleFilterParameter) {
    this(simpleFilterParameter.getName(), simpleFilterParameter.getValue());
  }

  /**
   * Constructs a {@link NotSimpleFilterParameter} with the given parameters.
   * 
   * @param name
   * @param value
   */
  public NotSimpleFilterParameter(String name, String value) {
    setName(name);
    setValue(value);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "NotSimpleFilterParameter(name=" + getName() + ", value=" + getValue() + ")";
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
    if (!(obj instanceof NotSimpleFilterParameter)) {
      return false;
    }
    NotSimpleFilterParameter other = (NotSimpleFilterParameter) obj;
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
