package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class ProcessInfo implements EadCValue, Serializable {

  private static final long serialVersionUID = -8793877008248237077L;

  private String attributeAltrender = null;
  private Note note = null;
  private P[] pList = null;

  /** Constructs a new empty {@link ProcessInfo} */
  public ProcessInfo() {

  }

  /**
   * Constructs a new {@link ProcessInfo} using the provided parameters
   * 
   * @param attributeAltrender
   * @param note
   * @param pList
   */
  public ProcessInfo(String attributeAltrender, Note note, P[] pList) {
    this.attributeAltrender = attributeAltrender;
    this.note = note;
    this.pList = pList;
  }

  public String getAttributeAltrender() {
    return attributeAltrender;
  }

  public void setAttributeAltrender(String attributeAltrender) {
    this.attributeAltrender = attributeAltrender;
  }

  public Note getNote() {
    return note;
  }

  public void setNote(Note note) {
    this.note = note;
  }

  public P[] getpList() {
    return pList;
  }

  public void setpList(P[] pList) {
    this.pList = pList;
  }

  @Override
  public String toString() {
    return "ProcessInfo [attributeAltrender=" + attributeAltrender + ", note=" + note + ", pList="
      + Arrays.toString(pList) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attributeAltrender == null) ? 0 : attributeAltrender.hashCode());
    result = prime * result + ((note == null) ? 0 : note.hashCode());
    result = prime * result + Arrays.hashCode(pList);
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
    if (!(obj instanceof ProcessInfo)) {
      return false;
    }
    ProcessInfo other = (ProcessInfo) obj;
    if (attributeAltrender == null) {
      if (other.attributeAltrender != null) {
        return false;
      }
    } else if (!attributeAltrender.equals(other.attributeAltrender)) {
      return false;
    }
    if (note == null) {
      if (other.note != null) {
        return false;
      }
    } else if (!note.equals(other.note)) {
      return false;
    }
    if (!Arrays.equals(pList, other.pList)) {
      return false;
    }
    return true;
  }

}
