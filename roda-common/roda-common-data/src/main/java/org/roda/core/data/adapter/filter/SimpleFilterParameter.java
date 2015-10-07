package org.roda.core.data.adapter.filter;

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

  /**
   * @see FilterParameter#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = true;

    if (obj != null && obj instanceof SimpleFilterParameter) {
      SimpleFilterParameter other = (SimpleFilterParameter) obj;
      equal = equal && super.equals(other);
      equal = equal && (getValue() == other.getValue() || getValue().equals(other.getValue()));
    } else {
      equal = false;
    }

    return equal;
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
