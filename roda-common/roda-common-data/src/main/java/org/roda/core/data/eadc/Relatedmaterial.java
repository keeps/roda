package org.roda.core.data.eadc;

import java.io.Serializable;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class Relatedmaterial implements Serializable {

  private static final long serialVersionUID = 3589982493080494615L;

  private P p = null;
  private Archref archref = null;

  /**
   * Constructs a new empty {@link Relatedmaterial}
   * */
  public Relatedmaterial() {

  }

  /**
   * Constructs a new {@link Relatedmaterial} using the provided parameters
   * 
   * @param p
   * @param archref
   */
  public Relatedmaterial(P p, Archref archref) {
    this.p = p;
    this.archref = archref;
  }

  /**
   * Constructs a new {@link Relatedmaterial}
   * 
   * @param p
   */
  public Relatedmaterial(P p) {
    this.p = p;
  }

  public P getP() {
    return p;
  }

  public void setP(P p) {
    this.p = p;
  }

  public Archref getArchref() {
    return archref;
  }

  public void setArchref(Archref archref) {
    this.archref = archref;
  }

  @Override
  public String toString() {
    return "Relatedmaterial [p=" + p + ", archref=" + archref + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((archref == null) ? 0 : archref.hashCode());
    result = prime * result + ((p == null) ? 0 : p.hashCode());
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
    if (!(obj instanceof Relatedmaterial)) {
      return false;
    }
    Relatedmaterial other = (Relatedmaterial) obj;
    if (archref == null) {
      if (other.archref != null) {
        return false;
      }
    } else if (!archref.equals(other.archref)) {
      return false;
    }
    if (p == null) {
      if (other.p != null) {
        return false;
      }
    } else if (!p.equals(other.p)) {
      return false;
    }
    return true;
  }

}
