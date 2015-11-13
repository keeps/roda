/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.browse;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Luis Faria
 * 
 */
public class TimelineInfo implements IsSerializable {
  private String date;
  private List<HotZone> hotZones;
  private List<Phase> phases;
  private String eventsXML;

  /**
   * Create a new time line info
   */
  public TimelineInfo() {
  }

  /**
   * Create a new time line info
   * 
   * @param date
   *          the current date where the time line is set
   * @param hotZones
   *          the time line hot zones
   * @param phases
   *          the time line phases
   * @param eventsXML
   *          an XML with the events
   */
  public TimelineInfo(String date, List<HotZone> hotZones, List<Phase> phases, String eventsXML) {
    this.date = date;
    this.hotZones = hotZones;
    this.phases = phases;
    this.eventsXML = eventsXML;
  }

  /**
   * Get the current date where the time line is set
   * 
   * @return the date
   */
  public String getDate() {
    return date;
  }

  /**
   * Set the current date where the time line is set
   * 
   * @param date
   */
  public void setDate(String date) {
    this.date = date;
  }

  /**
   * Get the time line hot zones
   * 
   * @return the hot zones
   */
  public List<HotZone> getHotZones() {
    return hotZones;
  }

  /**
   * Set the time line hot zones
   * 
   * @param hotZones
   */
  public void setHotZones(List<HotZone> hotZones) {
    this.hotZones = hotZones;
  }

  /**
   * Get the time line phases
   * 
   * @return the phases
   */
  public List<Phase> getPhases() {
    return phases;
  }

  /**
   * Set the time line phases
   * 
   * @param phases
   */
  public void setPhases(List<Phase> phases) {
    this.phases = phases;
  }

  /**
   * Get an XML with the events
   * 
   * @return the events XML
   */
  public String getEventsXML() {
    return eventsXML;
  }

  /**
   * Set an XML with the events
   * 
   * @param eventsXML
   */
  public void setEventsXML(String eventsXML) {
    this.eventsXML = eventsXML;
  }

  public String toString() {
    return "TimeLine (" + date + ", " + hotZones + ", " + phases + ")";
  }

  /**
   * Time line hot zone
   */
  public class HotZone {
    private String start;
    private String end;
    private int magnification;
    private int multiple;

    /**
     * Create a new hot zone
     */
    public HotZone() {
    }

    /**
     * Create a new hot zone
     * 
     * @param start
     *          the zone start date
     * @param end
     *          the zone end date
     * @param magnification
     *          the magnification multiplier
     * @param multiple
     *          the skipping multiplier. A label is painted for every multiple
     *          of unit. For example, if unit is minute and multiple is 15, then
     *          there is a label for every 15 minutes (i.e., 15, 30, 45,...).
     */
    public HotZone(String start, String end, int magnification, int multiple) {
      this.start = start;
      this.end = end;
      this.magnification = magnification;
      this.multiple = multiple;
    }

    /**
     * Get the zone start date
     * 
     * @return the start date
     */
    public String getStart() {
      return start;
    }

    /**
     * Set the zone start date
     * 
     * @param start
     */
    public void setStart(String start) {
      this.start = start;
    }

    /**
     * Get the zone end date
     * 
     * @return the end date
     */
    public String getEnd() {
      return end;
    }

    /**
     * Set the zone end date
     * 
     * @param end
     */
    public void setEnd(String end) {
      this.end = end;
    }

    /**
     * Get the magnification multiplier
     * 
     * @return the magnification
     */
    public int getMagnification() {
      return magnification;
    }

    /**
     * Set the magnification multiplier
     * 
     * @param magnification
     */
    public void setMagnification(int magnification) {
      this.magnification = magnification;
    }

    /**
     * Get the skipping multiplier. A label is painted for every multiple of
     * unit. For example, if unit is minute and multiple is 15, then there is a
     * label for every 15 minutes (i.e., 15, 30, 45,...).
     * 
     * @return the multiplier
     */
    public int getMultiple() {
      return multiple;
    }

    /**
     * Set the skipping multiplier. A label is painted for every multiple of
     * unit. For example, if unit is minute and multiple is 15, then there is a
     * label for every 15 minutes (i.e., 15, 30, 45,...).
     * 
     * @param multiple
     */
    public void setMultiple(int multiple) {
      this.multiple = multiple;
    }

    public String toString() {
      return "HotZone (" + start + ", " + end + ", " + magnification + ", " + multiple + ")";
    }

  }

  /**
   * Time line phase
   * 
   */
  public class Phase {
    private String start;
    private String end;
    private String label;

    /**
     * Create a new phase
     */
    public Phase() {

    }

    /**
     * Create a new phase
     * 
     * @param start
     *          the phase start date
     * @param end
     *          the phase end date
     * @param label
     *          the phase label
     */
    public Phase(String start, String end, String label) {
      this.start = start;
      this.end = end;
      this.label = label;
    }

    /**
     * Get the phase start date
     * 
     * @return the phase start date
     */
    public String getStart() {
      return start;
    }

    /**
     * Set the phase start date
     * 
     * @param start
     */
    public void setStart(String start) {
      this.start = start;
    }

    /**
     * Get the phase end date
     * 
     * @return the end phase date
     */
    public String getEnd() {
      return end;
    }

    /**
     * Set the phase end date
     * 
     * @param end
     */
    public void setEnd(String end) {
      this.end = end;
    }

    /**
     * Get the phase label
     * 
     * @return the phase label
     */
    public String getLabel() {
      return label;
    }

    /**
     * Set the phase label
     * 
     * @param label
     */
    public void setLabel(String label) {
      this.label = label;
    }

    public String toString() {
      return "Phase (" + start + ", " + end + ", " + label + ")";
    }

  }
}
