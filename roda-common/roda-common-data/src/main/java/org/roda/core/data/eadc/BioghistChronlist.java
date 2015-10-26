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
 * @author Rui Castro
 * 
 */
public class BioghistChronlist implements EadCValue, Serializable {
  private static final long serialVersionUID = 8300124633865047333L;

  private BioghistChronitem[] bioghistChronitems = null;

  /**
   * Constructs a new empty {@link BioghistChronlist}.
   */
  public BioghistChronlist() {
  }

  /**
   * Constructs a new {@link BioghistChronlist} clonning an existing
   * {@link BioghistChronlist}.
   * 
   * @param bioghistChronlist
   */
  public BioghistChronlist(BioghistChronlist bioghistChronlist) {
    this(bioghistChronlist.getBioghistChronitems());
  }

  /**
   * Constructs a new {@link BioghistChronlist} with the given
   * <code>bioghistChronitems</code>.
   * 
   * @param bioghistChronitems
   */
  public BioghistChronlist(BioghistChronitem[] bioghistChronitems) {
    setBioghistChronitems(bioghistChronitems);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof BioghistChronlist) {
      BioghistChronlist other = (BioghistChronlist) obj;

      // return this.bioghistChronitems == other.bioghistChronitems
      // || equals(this.bioghistChronitems, other.bioghistChronitems);

      return Arrays.asList(this.bioghistChronitems).equals(Arrays.asList(other.bioghistChronitems));

    } else {
      return false;
    }
  }

  @SuppressWarnings("unused")
  private boolean equals(Object[] rows1, Object[] rows2) {
    boolean equal = true;
    if (rows1.length == rows2.length) {
      for (int i = 0; i < rows1.length && equal; i++) {
        equal &= rows1[i].equals(rows2[i]);
      }
    } else {
      equal = false;
    }
    return equal;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {

    BioghistChronitem[] bioghistChronitems2 = getBioghistChronitems();

    if (bioghistChronitems2 == null) {
      bioghistChronitems2 = new BioghistChronitem[0];
    }

    return "BioghistChronlist (" + Arrays.asList(bioghistChronitems2) + ")";
  }

  /**
   * @return the bioghistChronitems
   */
  public BioghistChronitem[] getBioghistChronitems() {
    return bioghistChronitems;
  }

  /**
   * @param bioghistChronitems
   *          the bioghistChronitems to set
   */
  public void setBioghistChronitems(BioghistChronitem[] bioghistChronitems) {
    this.bioghistChronitems = bioghistChronitems;
  }
}
