package pt.gov.dgarq.roda.core.data.eadc;

/**
 * @author Rui Castro
 */
public class Text implements EadCValue {

  private String text;

  /**
   * Construst a new empty {@link Text}.
   */
  public Text() {
    this((String) null);
  }

  /**
   * Construst a new {@link Text} clonning an existing {@link Text}.
   * 
   * @param text
   *          the {@link Text} to clone.
   */
  public Text(Text text) {
    this(text.getText());
  }

  /**
   * Construst a new empty {@link Text}.
   * 
   * @param text
   *          the text.
   */
  public Text(String text) {
    setText(text);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof Text) {
      Text other = (Text) obj;
      return this.text == other.text || this.text.equals(other.text);
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return getText();
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @param text
   *          the text to set
   */
  public void setText(String text) {
    this.text = text;
  }

}
