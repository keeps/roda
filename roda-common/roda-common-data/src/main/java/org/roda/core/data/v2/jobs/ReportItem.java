/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

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
  // FIXME see if this is really necessary
  private String otherId = null;
  private String itemId = null;
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
    this();
    this.title = title;
  }

  public ReportItem(String title, String itemId) {
    this();
    this.title = title;
    this.itemId = itemId;
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
  public ReportItem(String title, List<Attribute> attributes) {
    setTitle(title);
    setAttributes(attributes);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "ReportItem [title=" + title + ", otherId=" + otherId + ", itemId=" + itemId + ", attributes=" + attributes
      + "]";
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

  public String getOtherId() {
    return otherId;
  }

  public void setOtherId(String otherId) {
    this.otherId = otherId;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  /**
   * @return the attributes
   */
  public List<Attribute> getAttributes() {
    return attributes;
  }

  /**
   * @param attributes
   *          the attributes to set
   */
  public void setAttributes(List<Attribute> attributes) {
    this.attributes.clear();
    if (attributes != null) {
      this.attributes.addAll(attributes);
    }
  }

  /**
   * @param attribute
   * @return
   */
  public ReportItem addAttribute(Attribute attribute) {
    this.attributes.add(attribute);
    return this;
  }

  /**
   * @param attributes
   */
  public void addAttributes(Attribute... attributes) {
    this.attributes.addAll(Arrays.asList(attributes));
  }

}
