/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import java.util.Date;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Date time picker
 *
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 */
public class DateTimePicker {

  private final HorizontalPanel layout;
  private final DatePicker datePicker;
  private final TextBox hourPicker;
  private final Label timeSeparator;
  private final TextBox minutePicker;

  /**
   * Create a new date time picker
   */
  public DateTimePicker() {
    layout = new HorizontalPanel();
    datePicker = new DatePicker();
    hourPicker = new TextBox();
    timeSeparator = new Label(":");
    minutePicker = new TextBox();

    layout.add(datePicker);
    layout.add(hourPicker);
    layout.add(timeSeparator);
    layout.add(minutePicker);

    hourPicker.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        char keyCode = event.getCharCode();
        Widget sender = (Widget) event.getSource();
        if ((!Character.isDigit(keyCode)) && (keyCode != (char) KeyCodes.KEY_TAB)
          && (keyCode != (char) KeyCodes.KEY_BACKSPACE) && (keyCode != (char) KeyCodes.KEY_DELETE)
          && (keyCode != (char) KeyCodes.KEY_ENTER) && (keyCode != (char) KeyCodes.KEY_HOME)
          && (keyCode != (char) KeyCodes.KEY_END) && (keyCode != (char) KeyCodes.KEY_LEFT)
          && (keyCode != (char) KeyCodes.KEY_UP) && (keyCode != (char) KeyCodes.KEY_RIGHT)
          && (keyCode != (char) KeyCodes.KEY_DOWN)) {
          hourPicker.cancelKey();
        }
      }
    });

    hourPicker.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        Widget sender = (Widget) event.getSource();
        int hour;
        String hourText = hourPicker.getText();
        boolean cancel = false;
        try {
          hour = Integer.valueOf(hourText);
          if (hour < 0 || hour > 23) {
            cancel = true;
          } else if (hour > 2) {
            minutePicker.setFocus(true);
          }
        } catch (NumberFormatException e) {
          cancel = true;
        }
        if (cancel && hourText.length() > 0) {
          hourPicker.setText(hourText.substring(0, hourText.length() - 1));
        }
      }
    });

    minutePicker.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        char keyCode = event.getCharCode();
        Widget sender = (Widget) event.getSource();
        if ((!Character.isDigit(keyCode)) && (keyCode != (char) KeyCodes.KEY_TAB)
          && (keyCode != (char) KeyCodes.KEY_BACKSPACE) && (keyCode != (char) KeyCodes.KEY_DELETE)
          && (keyCode != (char) KeyCodes.KEY_ENTER) && (keyCode != (char) KeyCodes.KEY_HOME)
          && (keyCode != (char) KeyCodes.KEY_END) && (keyCode != (char) KeyCodes.KEY_LEFT)
          && (keyCode != (char) KeyCodes.KEY_UP) && (keyCode != (char) KeyCodes.KEY_RIGHT)
          && (keyCode != (char) KeyCodes.KEY_DOWN)) {
          minutePicker.cancelKey();
        }
      }
    });

    minutePicker.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        Widget sender = (Widget) event.getSource();
        int minute;
        String minuteText = minutePicker.getText();
        boolean cancel = false;
        try {
          minute = Integer.valueOf(minuteText);
          if (minute < 0 || minute > 59) {
            cancel = true;
          }
        } catch (NumberFormatException e) {
          cancel = true;
        }

        if (cancel && minuteText.length() > 0) {
          minutePicker.setText(minuteText.substring(0, minuteText.length() - 1));
        }
      }
    });

    layout.addStyleName("wui-date-time-picker");
    hourPicker.addStyleName("picker-time-hour");
    minutePicker.addStyleName("picker-time-minute");
    timeSeparator.addStyleName("picker-time-separator");
  }

  /**
   * Get widget
   *
   * @return
   */
  public Widget getWidget() {
    return layout;
  }

  /**
   * Get the date defined in this picker
   *
   * @return
   */
  public Date getDate() {
    long date = datePicker.getDate().getTime();
    long hours = Integer.valueOf(hourPicker.getText()) * 3600000;
    long minutes = Integer.valueOf(minutePicker.getText()) * 60000;
    return new Date(date + hours + minutes);
  }

  /**
   * Set the date
   *
   * @param date
   */
  @SuppressWarnings("deprecation")
  public void setDate(Date date) {
    datePicker.setDate(date);
    hourPicker.setText("" + date.getHours());
    minutePicker.setText("" + date.getMinutes());
  }
}
