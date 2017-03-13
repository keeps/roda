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

import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 * 
 */
public class YearPicker extends TextBox {

  /**
   * Year type
   * 
   */
  public enum YearType {

    /**
     * 366 days year
     */
    LEAP_YEAR,
    /**
     * 365 days year
     */
    NORMAL_YEAR
  }

  /**
   * Create a new year picker
   */
  public YearPicker() {
    this.addStyleName("yearPicker");
    /*
     * this.addKeyPressHandler(new KeyPressHandler() {
     * 
     * @Override public void onKeyPress(KeyPressEvent event) { char keyCode =
     * event.getCharCode(); Widget sender = (Widget) event.getSource(); if
     * ((!Character.isDigit(keyCode)) && (keyCode != (char) KeyCodes.KEY_TAB) &&
     * (keyCode != (char) KeyCodes.KEY_BACKSPACE) && (keyCode != (char)
     * KeyCodes.KEY_DELETE) && (keyCode != (char) KeyCodes.KEY_ENTER) &&
     * (keyCode != (char) KeyCodes.KEY_HOME) && (keyCode != (char)
     * KeyCodes.KEY_END) && (keyCode != (char) KeyCodes.KEY_LEFT) && (keyCode !=
     * (char) KeyCodes.KEY_UP) && (keyCode != (char) KeyCodes.KEY_RIGHT) &&
     * (keyCode != (char) KeyCodes.KEY_DOWN)) { ((TextBox) sender).cancelKey();
     * } } });
     */
  }

  /**
   * Is year valid
   * 
   * @return
   */
  public boolean isValid() {
    // year must be between 0000 - 2999 (EAD schema limitation)
    return this.getText().matches("\\d{1,4}") && getInt() < 3000;
  }

  /**
   * Get selected year
   * 
   * @return
   */
  public int getInt() {
    return Integer.parseInt(getText());
  }

  /**
   * Get selected year
   * 
   * @return
   */
  public String getString() {
    return this.getText();
  }

  /**
   * Get selected year type
   * 
   * @return
   */
  public YearType getYearType() {
    int year = getInt();
    YearType type;

    if (year % 400 == 0) {
      type = YearType.LEAP_YEAR;
    } else if (year % 100 == 0) {
      type = YearType.NORMAL_YEAR;
    } else if (year % 4 == 0) {
      type = YearType.LEAP_YEAR;
    } else {
      type = YearType.NORMAL_YEAR;
    }

    return type;
  }

  /**
   * Set year
   * 
   * @param year
   */
  public void set(int year) {
    this.setText(Integer.toString(year));
  }
}
