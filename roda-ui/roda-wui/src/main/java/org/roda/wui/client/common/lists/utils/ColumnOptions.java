package org.roda.wui.client.common.lists.utils;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;

public class ColumnOptions {

  private String name;
  private String field;
  private String header;
  private boolean nowrap;
  private boolean alignRight;
  private double width;
  private Unit widthUnit;
  private boolean sortable;
  private List<String> sortBy;
  private RenderingHint renderingHint;

  public enum RenderingHint {
    FILE_SIZE,

    DATE_FORMAT_TITLE, DATE_FORMAT_SIMPLE, DATETIME_FORMAT_SIMPLE;
  }

  /**
   * @return the header
   */
  public String getHeader() {
    return header;
  }

  /**
   * @param header
   *          the header to set
   */
  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * @return the nowrap
   */
  public boolean isNowrap() {
    return nowrap;
  }

  /**
   * @param nowrap
   *          the nowrap to set
   */
  public void setNowrap(boolean nowrap) {
    this.nowrap = nowrap;
  }

  /**
   * @return the alignRight
   */
  public boolean isAlignRight() {
    return alignRight;
  }

  /**
   * @param alignRight
   *          the alignRight to set
   */
  public void setAlignRight(boolean alignRight) {
    this.alignRight = alignRight;
  }

  /**
   * @return the width
   */
  public double getWidth() {
    return width;
  }

  /**
   * @param width
   *          the width to set
   */
  public void setWidth(double width) {
    this.width = width;
  }

  /**
   * @return the widthUnit
   */
  public Unit getWidthUnit() {
    return widthUnit;
  }

  /**
   * @param widthUnit
   *          the widthUnit to set
   */
  public void setWidthUnit(Unit widthUnit) {
    this.widthUnit = widthUnit;
  }

  /**
   * @return the sortable
   */
  public boolean isSortable() {
    return sortable;
  }

  /**
   * @param sortable
   *          the sortable to set
   */
  public void setSortable(boolean sortable) {
    this.sortable = sortable;
  }

  /**
   * @return the sortBy
   */
  public List<String> getSortBy() {
    return sortBy;
  }

  /**
   * @param sortBy
   *          the sortBy to set
   */
  public void setSortBy(List<String> sortBy) {
    this.sortBy = sortBy;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the field
   */
  public String getField() {
    return field;
  }

  /**
   * @param field
   *          the field to set
   */
  public void setField(String field) {
    this.field = field;
  }

  /**
   * @return the renderingHint
   */
  public RenderingHint getRenderingHint() {
    return renderingHint;
  }

  /**
   * @param renderingHint
   *          the renderingHint to set
   */
  public void setRenderingHint(RenderingHint renderingHint) {
    this.renderingHint = renderingHint;
  }

}
