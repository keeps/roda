/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serializable;

/**
 * This is a simple attribute with name and value.
 * 
 * @author Rui Castro
 */
public class Attribute implements Serializable {
  private static final long serialVersionUID = -1200920433672324166L;

  private String name = null;
  private String value = null;

  /**
   * Constructs a new {@link Attribute}.
   */
  public Attribute() {
  }

  /**
   * Constructs a new {@link Attribute} cloning an existing {@link Attribute}.
   * 
   * @param attribute
   *          the {@link Attribute} to clone.
   */
  public Attribute(Attribute attribute) {
    this(attribute.getName(), attribute.getValue());
  }

  /**
   * Constructs a new {@link Attribute} with the given parameters.
   * 
   * @param name
   *          the name of the Attribute.
   * @param value
   *          the value of the {@link Attribute}.
   */
  public Attribute(String name, String value) {
    setName(name);
    setValue(value);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Attribute(name=" + getName() + ", value=" + getValue() + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    if (!(obj instanceof Attribute)) {
      return false;
    }
    Attribute other = (Attribute) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
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
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
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
