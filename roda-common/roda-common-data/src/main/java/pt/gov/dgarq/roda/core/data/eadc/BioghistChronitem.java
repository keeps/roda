package pt.gov.dgarq.roda.core.data.eadc;

import java.io.Serializable;

/**
 * 
 * @author Rui Castro
 */
public class BioghistChronitem implements Serializable {
  private static final long serialVersionUID = -5243174023351498541L;

  private String event = null;

  private String dateInitial = null;

  private String dateFinal = null;

  /**
   * Constructs a new empty {@link BioghistChronitem}.
   */
  public BioghistChronitem() {
  }

  /**
   * Constructs a new {@link BioghistChronitem} clonning an existing
   * {@link BioghistChronitem}.
   * 
   * @param item
   */
  public BioghistChronitem(BioghistChronitem item) {
    this(item.getEvent(), item.getDateInitial(), item.getDateFinal());
  }

  /**
   * @param event
   * @param dateInitial
   * @param dateFinal
   */
  public BioghistChronitem(String event, String dateInitial, String dateFinal) {
    setEvent(event);
    setDateInitial(dateInitial);
    setDateFinal(dateFinal);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof BioghistChronitem) {
      BioghistChronitem other = (BioghistChronitem) obj;
      return (getEvent() == other.getEvent() || getEvent().equals(other.getEvent()))
        && (getDateInitial() == other.getDateInitial() || getDateInitial().equals(other.getDateInitial()))
        && (getDateFinal() == other.getDateFinal() || getDateFinal().equals(other.getDateFinal()));
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "BioghistCronitem { " + dateInitial + "/" + dateFinal + ", " + event + " }";
  }

  /**
   * @return the dateFinal
   */
  public String getDateFinal() {
    return dateFinal;
  }

  /**
   * @param dateFinal
   *          the dateFinal to set
   */
  public void setDateFinal(String dateFinal) {
    this.dateFinal = dateFinal;
  }

  /**
   * @return the dateInitial
   */
  public String getDateInitial() {
    return dateInitial;
  }

  /**
   * @param dateInitial
   *          the dateInitial to set
   */
  public void setDateInitial(String dateInitial) {
    this.dateInitial = dateInitial;
  }

  /**
   * @return the text
   * 
   * @deprecated use {@link BioghistChronitem#getEvent()} instead.
   */
  public String getText() {
    return getEvent();
  }

  /**
   * @param text
   *          the text to set
   * 
   * @deprecated use {@link BioghistChronitem#setEvent(String)} instead.
   */
  public void setText(String text) {
    setEvent(text);
  }

  /**
   * @return the event
   */
  public String getEvent() {
    return event;
  }

  /**
   * @param event
   *          the event to set
   */
  public void setEvent(String event) {
    this.event = event;
  }

}
