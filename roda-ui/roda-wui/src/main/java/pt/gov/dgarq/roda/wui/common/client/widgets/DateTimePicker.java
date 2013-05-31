package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Date time picker
 * 
 * @author Luis Faria
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

		hourPicker.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if ((!Character.isDigit(keyCode))
						&& (keyCode != (char) KEY_TAB)
						&& (keyCode != (char) KEY_BACKSPACE)
						&& (keyCode != (char) KEY_DELETE)
						&& (keyCode != (char) KEY_ENTER)
						&& (keyCode != (char) KEY_HOME)
						&& (keyCode != (char) KEY_END)
						&& (keyCode != (char) KEY_LEFT)
						&& (keyCode != (char) KEY_UP)
						&& (keyCode != (char) KEY_RIGHT)
						&& (keyCode != (char) KEY_DOWN)) {
					hourPicker.cancelKey();
				}
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
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
					hourPicker.setText(hourText.substring(0,
							hourText.length() - 1));
				}

			}

		});

		minutePicker.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if ((!Character.isDigit(keyCode))
						&& (keyCode != (char) KEY_TAB)
						&& (keyCode != (char) KEY_BACKSPACE)
						&& (keyCode != (char) KEY_DELETE)
						&& (keyCode != (char) KEY_ENTER)
						&& (keyCode != (char) KEY_HOME)
						&& (keyCode != (char) KEY_END)
						&& (keyCode != (char) KEY_LEFT)
						&& (keyCode != (char) KEY_UP)
						&& (keyCode != (char) KEY_RIGHT)
						&& (keyCode != (char) KEY_DOWN)) {
					minutePicker.cancelKey();
				}
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
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
					minutePicker.setText(minuteText.substring(0, minuteText
							.length() - 1));
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
