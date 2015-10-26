/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class Materialspecs implements EadCValue, Serializable {

  private static final long serialVersionUID = -4991686482686909746L;

  private Materialspec[] materialspecs = null;

  /**
   * Constructs a new empty {@link Materialspecs}
   * */
  public Materialspecs() {

  }

  /**
   * Constructs a new {@link Materialspecs} using the provided parameters
   * 
   * @param materialspecs
   */
  public Materialspecs(Materialspec[] materialspecs) {
    this.materialspecs = materialspecs;
  }

  public Materialspec[] getMaterialspecs() {
    return materialspecs;
  }

  public void setMaterialspecs(Materialspec[] materialspecs) {
    this.materialspecs = materialspecs;
  }

  @Override
  public String toString() {
    return "Materialspecs [materialspecs=" + Arrays.toString(materialspecs) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(materialspecs);
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
    if (!(obj instanceof Materialspecs)) {
      return false;
    }
    Materialspecs other = (Materialspecs) obj;
    if (!Arrays.equals(materialspecs, other.materialspecs)) {
      return false;
    }
    return true;
  }

}
