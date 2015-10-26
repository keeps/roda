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
public class Relatedmaterials implements EadCValue, Serializable {

  private static final long serialVersionUID = -4215785610965488799L;

  private Relatedmaterial[] relatedmaterials = null;

  /**
   * Constructs a new empty {@link Relatedmaterials}
   * */
  public Relatedmaterials() {

  }

  /**
   * Constructs a new {@link Relatedmaterials} using the provided parameters
   * 
   * @param relatedmaterials
   */
  public Relatedmaterials(Relatedmaterial[] relatedmaterials) {
    this.relatedmaterials = relatedmaterials;
  }

  public Relatedmaterial[] getRelatedmaterials() {
    return relatedmaterials;
  }

  public void setRelatedmaterials(Relatedmaterial[] relatedmaterials) {
    this.relatedmaterials = relatedmaterials;
  }

  @Override
  public String toString() {
    return "Relatedmaterials [relatedmaterials=" + Arrays.toString(relatedmaterials) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(relatedmaterials);
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
    if (!(obj instanceof Relatedmaterials)) {
      return false;
    }
    Relatedmaterials other = (Relatedmaterials) obj;
    if (!Arrays.equals(relatedmaterials, other.relatedmaterials)) {
      return false;
    }
    return true;
  }
}
