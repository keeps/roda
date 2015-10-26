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
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class P implements Serializable {

  private static final long serialVersionUID = -4151732729427209467L;

  private String attributeAltrender = null;
  private String text = null;
  private String date = null;
  private String num = null;
  private String corpname = null;
  private Notes notes = null;

  private Archrefs archrefs = null;

  /** Constructs a new empty {@link P} */
  public P() {

  }

  /**
   * Constructs a new {@link P}
   * 
   * @param text
   * */
  public P(String text) {
    this.text = text;
  }

  /**
   * Constructs a new {@link P} using the provided parameters
   * 
   * @param attributeAltrender
   * @param text
   * @param date
   * @param num
   * @param corpname
   * @param notes
   * @param archrefs
   */
  public P(String attributeAltrender, String text, String date, String num, String corpname, Notes notes,
    Archrefs archrefs) {
    this.attributeAltrender = attributeAltrender;
    this.text = text;
    this.date = date;
    this.num = num;
    this.corpname = corpname;
    this.notes = notes;
    this.archrefs = archrefs;
  }

  public String getAttributeAltrender() {
    return attributeAltrender;
  }

  public void setAttributeAltrender(String attributeAltrender) {
    this.attributeAltrender = attributeAltrender;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Notes getNotes() {
    return notes;
  }

  public void setNotes(Notes notes) {
    this.notes = notes;
  }

  public Archrefs getArchrefs() {
    return archrefs;
  }

  public void setArchrefs(Archrefs archrefs) {
    this.archrefs = archrefs;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getNum() {
    return num;
  }

  public void setNum(String num) {
    this.num = num;
  }

  public String getCorpname() {
    return corpname;
  }

  public void setCorpname(String corpname) {
    this.corpname = corpname;
  }

  @Override
  public String toString() {
    return "P [attributeAltrender=" + attributeAltrender + ", text=" + text + ", date=" + date + ", num=" + num
      + ", corpname=" + corpname + ", notes=" + notes + ", archrefs=" + archrefs + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((archrefs == null) ? 0 : archrefs.hashCode());
    result = prime * result + ((attributeAltrender == null) ? 0 : attributeAltrender.hashCode());
    result = prime * result + ((corpname == null) ? 0 : corpname.hashCode());
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((notes == null) ? 0 : notes.hashCode());
    result = prime * result + ((num == null) ? 0 : num.hashCode());
    result = prime * result + ((text == null) ? 0 : text.hashCode());
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
    if (!(obj instanceof P)) {
      return false;
    }
    P other = (P) obj;
    if (archrefs == null) {
      if (other.archrefs != null) {
        return false;
      }
    } else if (!archrefs.equals(other.archrefs)) {
      return false;
    }
    if (attributeAltrender == null) {
      if (other.attributeAltrender != null) {
        return false;
      }
    } else if (!attributeAltrender.equals(other.attributeAltrender)) {
      return false;
    }
    if (corpname == null) {
      if (other.corpname != null) {
        return false;
      }
    } else if (!corpname.equals(other.corpname)) {
      return false;
    }
    if (date == null) {
      if (other.date != null) {
        return false;
      }
    } else if (!date.equals(other.date)) {
      return false;
    }
    if (notes == null) {
      if (other.notes != null) {
        return false;
      }
    } else if (!notes.equals(other.notes)) {
      return false;
    }
    if (num == null) {
      if (other.num != null) {
        return false;
      }
    } else if (!num.equals(other.num)) {
      return false;
    }
    if (text == null) {
      if (other.text != null) {
        return false;
      }
    } else if (!text.equals(other.text)) {
      return false;
    }
    return true;
  }

}
