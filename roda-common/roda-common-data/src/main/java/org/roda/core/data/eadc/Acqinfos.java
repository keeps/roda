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

public class Acqinfos implements EadCValue, Serializable {

  private static final long serialVersionUID = 3303395527726826628L;

  private Acqinfo[] acqinfos = null;

  /**
   * Constructs a new empty {@link Acqinfos}
   * */
  public Acqinfos() {

  }

  /**
   * Constructs a new {@link Acqinfos} using provided parameters
   * 
   * @param acqinfos
   */
  public Acqinfos(Acqinfo[] acqinfos) {
    this.acqinfos = acqinfos;
  }

  public Acqinfo[] getAcqinfos() {
    return acqinfos;
  }

  public void setAcqinfos(Acqinfo[] acqinfos) {
    this.acqinfos = acqinfos;
  }

  @Override
  public String toString() {
    return "Acqinfos [acqinfos=" + Arrays.toString(acqinfos) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(acqinfos);
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
    if (!(obj instanceof Acqinfos)) {
      return false;
    }
    Acqinfos other = (Acqinfos) obj;
    if (!Arrays.equals(acqinfos, other.acqinfos)) {
      return false;
    }
    return true;
  }

}
