package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;

/**
 * This class represents a EAD Component note.
 * 
 * @author Rui Castro
 * @author Hélder Silva
 * 
 */
public class Note implements Serializable {
  private static final long serialVersionUID = 6675330096663546895L;

  private String attributeLabel = null;
  private String attributeAltrender = null;
  private P p = null;
  private ItemList list = null;

  /**
   * Constructs an empty {@link Note}.
   */
  public Note() {
  }

  /**
   * Constructs a new {@link Note} cloning an existing one.
   * 
   * @param note
   *          the {@link Note} to clone.
   */
  public Note(Note note) {
    this(note.getAttributeLabel(), note.getAttributeAltrender(), note.getP(), note.getList());
  }

  /**
   * Constructs a new {@link Note} using the provided parameters
   * 
   * @param attributeLabel
   * @param attributeAltrender
   * @param p
   * @param list
   */
  public Note(String attributeLabel, String attributeAltrender, P p, ItemList list) {
    this.attributeLabel = attributeLabel;
    this.attributeAltrender = attributeAltrender;
    this.p = p;
    this.list = list;
  }

  /**
   * Constructs a new {@link Note}
   * 
   * @param p
   */
  public Note(P p) {
    this.p = p;
  }

  public String getAttributeLabel() {
    return attributeLabel;
  }

  public void setAttributeLabel(String attributeLabel) {
    this.attributeLabel = attributeLabel;
  }

  public String getAttributeAltrender() {
    return attributeAltrender;
  }

  public void setAttributeAltrender(String attributeAltrender) {
    this.attributeAltrender = attributeAltrender;
  }

  public P getP() {
    return p;
  }

  public void setP(P p) {
    this.p = p;
  }

  public ItemList getList() {
    return list;
  }

  public void setList(ItemList list) {
    this.list = list;
  }

  @Override
  public String toString() {
    return "Note [attributeLabel=" + attributeLabel + ", attributeAltrender=" + attributeAltrender + ", p=" + p
      + ", list=" + list + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attributeAltrender == null) ? 0 : attributeAltrender.hashCode());
    result = prime * result + ((attributeLabel == null) ? 0 : attributeLabel.hashCode());
    result = prime * result + ((list == null) ? 0 : list.hashCode());
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
    if (!(obj instanceof Note)) {
      return false;
    }
    Note other = (Note) obj;
    if (attributeAltrender == null) {
      if (other.attributeAltrender != null) {
        return false;
      }
    } else if (!attributeAltrender.equals(other.attributeAltrender)) {
      return false;
    }
    if (attributeLabel == null) {
      if (other.attributeLabel != null) {
        return false;
      }
    } else if (!attributeLabel.equals(other.attributeLabel)) {
      return false;
    }
    if (list == null) {
      if (other.list != null) {
        return false;
      }
    } else if (!list.equals(other.list)) {
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
