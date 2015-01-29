package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class Materialspec implements Serializable {

  private static final long serialVersionUID = -7011237705034990125L;

  private String text = null;
  private String attributeLabel = null;

  /** Constructs a new empty {@link Materialspec} */
  public Materialspec() {

  }

  /**
   * Constructs a new {@link Materialspec} using the provided parameters
   * 
   * @param text
   * @param attributeLabel
   */
  public Materialspec(String text, String attributeLabel) {
    this.text = text;
    this.attributeLabel = attributeLabel;
  }

  /**
   * Constructs a new {@link Materialspec}
   * 
   * @param text
   * */
  public Materialspec(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getAttributeLabel() {
    return attributeLabel;
  }

  public void setAttributeLabel(String attributeLabel) {
    this.attributeLabel = attributeLabel;
  }

  @Override
  public String toString() {
    return "Materialspec [text=" + text + ", attributeLabel=" + attributeLabel + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attributeLabel == null) ? 0 : attributeLabel.hashCode());
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
    if (!(obj instanceof Materialspec)) {
      return false;
    }
    Materialspec other = (Materialspec) obj;
    if (attributeLabel == null) {
      if (other.attributeLabel != null) {
        return false;
      }
    } else if (!attributeLabel.equals(other.attributeLabel)) {
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
