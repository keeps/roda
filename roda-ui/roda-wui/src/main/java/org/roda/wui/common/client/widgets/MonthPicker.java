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
package org.roda.wui.common.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class MonthPicker extends ListBox {

  /**
   * The type of the month
   * 
   */
  public static enum MonthType {
    /**
     * 31 days month
     */
    MONTH_31_DAYS,
    /**
     * 30 days month
     */
    MONTH_30_DAYS,
    /**
     * 29 and 28 days month
     */
    FEBRUARY
  }

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  /**
   * Create a new month picker
   */
  public MonthPicker() {
    this.setVisibleItemCount(1);
    init();
    this.addStyleName("monthPicker");
  }

  protected void init() {
    addItem(messages.january());
    addItem(messages.february());
    addItem(messages.march());
    addItem(messages.april());
    addItem(messages.may());
    addItem(messages.june());
    addItem(messages.july());
    addItem(messages.august());
    addItem(messages.september());
    addItem(messages.october());
    addItem(messages.november());
    addItem(messages.december());
    setSelectedIndex(0);
  }

  /**
   * Get selected month
   * 
   * @return the month number, between 1 and 12
   */
  public int getSelectedInt() {
    return getSelectedIndex() + 1;
  }

  /**
   * Get selected month
   * 
   * @return
   */
  public String getSelected() {
    int month = getSelectedInt();
    return month < 10 ? "0" + month : Integer.toString(month);
  }

  /**
   * Set selected month
   * 
   * @param month
   *          the month in the year, from 1 to 12
   */
  public void setSelected(int month) {
    setSelectedIndex(month - 1);
  }

  /**
   * Get selected month type
   * 
   * @return
   */
  public MonthType getSelectedMonthType() {
    int month = getSelectedInt();
    MonthType type;
    switch (month) {
      case 4:
      case 6:
      case 9:
      case 10:
        type = MonthType.MONTH_30_DAYS;
        break;
      case 2:
        type = MonthType.FEBRUARY;
        break;
      default:
        type = MonthType.MONTH_31_DAYS;
        break;
    }
    return type;
  }

}
