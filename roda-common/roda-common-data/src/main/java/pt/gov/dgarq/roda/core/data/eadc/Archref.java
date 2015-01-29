package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class Archref implements Serializable {

  private static final long serialVersionUID = -7166847892315291425L;

  private Unitid[] unitids = null;
  private String unittitle = null;
  private Note note = null;

  /**
   * Constructs a new empty {@link Archref}.
   */
  public Archref() {
  }

  /**
   * Constructs a new {@link Archref} using the provided parameters
   * 
   * @param unitids
   * @param unittitle
   */
  public Archref(Unitid[] unitids, String unittitle, Note note) {
    this.unitids = unitids;
    this.unittitle = unittitle;
    this.note = note;
  }

  /**
   * Constructs a new {@link Archref} cloning an existing {@link Archref}.
   * 
   * @param archref
   */
  public Archref(Archref archref) {
    this(archref.getUnitids(), archref.getUnittitle(), archref.getNote());
  }

  public Unitid[] getUnitids() {
    return unitids;
  }

  public void setUnitids(Unitid[] unitids) {
    this.unitids = unitids;
  }

  public String getUnittitle() {
    return unittitle;
  }

  public void setUnittitle(String unittitle) {
    this.unittitle = unittitle;
  }

  public Note getNote() {
    return note;
  }

  public void setNote(Note note) {
    this.note = note;
  }

  @Override
  public String toString() {
    return "Archref [unitids=" + Arrays.toString(unitids) + ", unittitle=" + unittitle + ", note=" + note + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((note == null) ? 0 : note.hashCode());
    result = prime * result + Arrays.hashCode(unitids);
    result = prime * result + ((unittitle == null) ? 0 : unittitle.hashCode());
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
    if (!(obj instanceof Archref)) {
      return false;
    }
    Archref other = (Archref) obj;
    if (note == null) {
      if (other.note != null) {
        return false;
      }
    } else if (!note.equals(other.note)) {
      return false;
    }
    if (!Arrays.equals(unitids, other.unitids)) {
      return false;
    }
    if (unittitle == null) {
      if (other.unittitle != null) {
        return false;
      }
    } else if (!unittitle.equals(other.unittitle)) {
      return false;
    }
    return true;
  }
}
