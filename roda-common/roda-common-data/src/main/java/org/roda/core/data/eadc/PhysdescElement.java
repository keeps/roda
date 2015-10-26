/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;

/**
 * @author Rui Castro
 */
public class PhysdescElement implements EadCValue, Serializable {
  private static final long serialVersionUID = 7460255059585302251L;

  private String value = null;

  private String unit = null;

  /**
   * Constructs a new empty {@link PhysdescElement}.
   */
  public PhysdescElement() {
  }

  /**
   * Constructs a new {@link PhysdescElement} cloning an existing
   * {@link PhysdescElement}.
   * 
   * @param physdescElement
   *          the {@link PhysdescElement} to clone.
   */
  public PhysdescElement(PhysdescElement physdescElement) {
    this(physdescElement.getValue(), physdescElement.getUnit());
  }

  /**
   * @param value
   * @param unit
   */
  public PhysdescElement(String value, String unit) {
    this.value = value;
    this.unit = unit;
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof PhysdescElement) {
      PhysdescElement other = (PhysdescElement) obj;
      return (getValue() == other.getValue() || getValue().equals(other.getValue()))
        && (getUnit() == other.getUnit() || getValue().equals(other.getValue()));
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return value + " (" + unit + ")";
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

  /**
   * @return the unit
   */
  public String getUnit() {
    return unit;
  }

  /**
   * @param unit
   *          the unit to set
   */
  public void setUnit(String unit) {
    this.unit = unit;
  }

}
