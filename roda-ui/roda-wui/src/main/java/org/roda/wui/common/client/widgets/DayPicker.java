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

import org.roda.wui.common.client.widgets.MonthPicker.MonthType;
import org.roda.wui.common.client.widgets.YearPicker.YearType;

import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Luis Faria
 * 
 */
public class DayPicker extends ListBox {

  /**
   * The type of day picker
   * 
   */
  public static enum DayPickerType {
    /**
     * Pick a day on a 31 days month
     */
    MONTH_31_DAYS,

    /**
     * Pick a day on a 30 days month
     */
    MONTH_30_DAYS,
    /**
     * Pick a day on a 29 days month (February)
     */
    MONTH_29_DAYS,
    /**
     * Pick a day on a 28 days month (February)
     */
    MONTH_28_DAYS
  }

  private DayPickerType type;

  private boolean initial;

  /**
   * Create a new day picker
   * 
   * @param initial
   *          default is the first or last day
   */
  public DayPicker(boolean initial) {
    this.initial = initial;
    this.setVisibleItemCount(1);
    this.addStyleName("dayPicker");
  }

  /**
   * Update the day picker type
   * 
   * @param yearType
   * @param monthType
   */
  public void update(YearType yearType, MonthType monthType) {
    if (monthType == MonthPicker.MonthType.MONTH_31_DAYS) {
      update(DayPickerType.MONTH_31_DAYS);
    } else if (monthType == MonthPicker.MonthType.MONTH_30_DAYS) {
      update(DayPickerType.MONTH_30_DAYS);
    } else if (monthType == MonthPicker.MonthType.FEBRUARY && yearType == YearPicker.YearType.LEAP_YEAR) {
      update(DayPickerType.MONTH_29_DAYS);
    } else {
      update(DayPickerType.MONTH_28_DAYS);
    }
  }

  /**
   * Update the day picker type
   * 
   * @param type
   */
  public void update(DayPickerType type) {
    if (this.type != type) {
      this.type = type;
      this.clear();
      for (int i = 1; i <= days(); i++) {
        this.addItem(i < 10 ? "0" + i : "" + i);
      }
      setEnabled(true);
      if (initial) {
        setSelectedIndex(0);
      } else {
        setSelectedIndex(getItemCount() - 1);
      }
    }
  }

  protected int days() {
    int days = 0;
    if (type == DayPickerType.MONTH_31_DAYS) {
      days = 31;
    } else if (type == DayPickerType.MONTH_30_DAYS) {
      days = 30;
    } else if (type == DayPickerType.MONTH_29_DAYS) {
      days = 29;
    } else if (type == DayPickerType.MONTH_28_DAYS) {
      days = 28;
    }

    return days;
  }

  /**
   * Get selected day
   * 
   * @return
   */
  public String getSelected() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex == -1) {
      // throw new NoSuchElementException("No item selected");
      selectedIndex = 0;
    }
    return getItemText(selectedIndex);
  }

  /**
   * Get selected day
   * 
   * @return
   */
  public int getSelectedInt() {
    return Integer.valueOf(getSelected()).intValue();
  }

  /**
   * Set selected day
   * 
   * @param selected
   *          The day of the month, from 1-31 in some months
   */
  public void setSelected(int selected) {
    setSelectedIndex(selected - 1);
  }

  /**
   * Get day picker type
   * 
   * @return
   */
  public DayPickerType getType() {
    return type;
  }

}
