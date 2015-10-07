package org.roda.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a RODA report.
 * 
 * @author Rui Castro
 */
public class Report implements Serializable {
  private static final long serialVersionUID = 8031045207181692901L;

  /**
   * A plugin report
   */
  public static final String TYPE_PLUGIN_REPORT = "PLUGIN_REPORT";

  private String id = null;
  private String type = null;
  private String title = null;
  private List<Attribute> attributes = new ArrayList<Attribute>();
  private List<ReportItem> items = new ArrayList<ReportItem>();

  /**
   * Constructs a new {@link Report}.
   */
  public Report() {
  }

  /**
   * Constructs a new {@link Report} cloning an existing {@link Report}.
   * 
   * @param report
   *          the {@link Report} to clone.
   */
  public Report(Report report) {
    this(report.getId(), report.getType(), report.getTitle(), report.getAttributes(), report.getItems());
  }

  /**
   * Constructs a new {@link Report} with the given parameters.
   * 
   * @param id
   * @param type
   * @param title
   * @param attributes
   * @param items
   */
  public Report(String id, String type, String title, Attribute[] attributes, ReportItem[] items) {
    setId(id);
    setType(type);
    setTitle(title);
    setAttributes(attributes);
    setItems(items);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Report(id=" + getId() + ", type=" + getType() + ", title=" + getTitle() + ", attributes=" + attributes
      + ", items=" + items + ")";
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Report) {
      Report other = (Report) obj;
      return getId() == other.getId() || getId().equals(other.getId());
    } else {
      return false;
    }
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = type;
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
   * @return the items
   */
  public ReportItem[] getItems() {
    return items.toArray(new ReportItem[items.size()]);
  }

  /**
   * @param items
   *          the items to set
   */
  public void setItems(ReportItem[] items) {
    this.items.clear();
    if (items != null) {
      this.items.addAll(Arrays.asList(items));
    }
  }

  /**
   * @param item
   */
  public void addItem(ReportItem item) {
    this.items.add(item);
  }

}
