package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class Archrefs implements Serializable, EadCValue {

  private static final long serialVersionUID = 2822421323097824527L;

  private Archref[] archrefs = null;

  /**
   * Constructs a new empty {@link Archrefs}
   * */
  public Archrefs() {

  }

  /**
   * Constructs a new {@link Archrefs} using the provided parameters
   * 
   * @param archrefs
   */
  public Archrefs(Archref[] archrefs) {
    this.archrefs = archrefs;
  }

  /**
   * @return the archrefs
   */
  public Archref[] getArchrefs() {
    return archrefs;
  }

  /**
   * @param archrefs
   *          the archrefs to set
   */
  public void setArchrefs(Archref[] archrefs) {
    this.archrefs = archrefs;
  }

  @Override
  public String toString() {
    return "Archrefs [Archref=" + Arrays.toString(archrefs) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(archrefs);
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
    if (!(obj instanceof Archrefs)) {
      return false;
    }
    Archrefs other = (Archrefs) obj;
    if (!Arrays.equals(archrefs, other.archrefs)) {
      return false;
    }
    return true;
  }

}
