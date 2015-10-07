package org.roda.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is an item inside a {@link Report}.
 * 
 * @author Rui Castro
 */
public class ReportItem implements Serializable {
  private static final long serialVersionUID = 2691735201123316994L;

  private String title = null;
  private List<Attribute> attributes = new ArrayList<Attribute>();

  /**
   * Constructs a new {@link ReportItem}.
   */
  public ReportItem() {
  }

  /**
   * Constructs a new {@link ReportItem}.
   * 
   * @param title
   *          the title
   */
  public ReportItem(String title) {
    this(title, null);
  }

  /**
   * Constructs a new {@link ReportItem} cloning an existing {@link ReportItem}.
   * 
   * @param reportItem
   *          the {@link ReportItem} to clone.
   */
  public ReportItem(ReportItem reportItem) {
    this(reportItem.getTitle(), reportItem.getAttributes());
  }

  /**
   * Constructs a new {@link ReportItem} with the given parameters.
   * 
   * @param title
   * @param attributes
   */
  public ReportItem(String title, Attribute[] attributes) {
    setTitle(title);
    setAttributes(attributes);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "ReportItem(title=" + getTitle() + ", attributes=" + attributes + ")";
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the attributes
   */
  public Attribute[] getAttributes() {
    return attributes.toArray(new Attribute[attributes.size()]);
  }

  /**
   * @param attributes
   *          the attributes to set
   */
  public void setAttributes(Attribute[] attributes) {
    this.attributes.clear();
    if (attributes != null) {
      this.attributes.addAll(Arrays.asList(attributes));
    }
  }

  /**
   * @param attribute
   */
  public void addAttribute(Attribute attribute) {
    this.attributes.add(attribute);
  }

  /**
   * @param attributes
   */
  public void addAttributes(Attribute... attributes) {
    this.attributes.addAll(Arrays.asList(attributes));
  }

}
