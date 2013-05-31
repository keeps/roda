/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class YearPicker extends TextBox {

	/**
	 * Year type
	 * 
	 */
	public static enum YearType {
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
		this.addKeyboardListener(new KeyboardListener() {

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
					((TextBox) sender).cancelKey();
				}
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {

			}

		});
	}

	/**
	 * Is year valid
	 * 
	 * @return
	 */
	public boolean isValid() {
		// year must be between 0000 - 2999 (EAD schema limitation)
		return this.getText().matches("\\d{1,4}")
				&& Integer.valueOf(getText()).intValue() < 3000;
	}

	/**
	 * Get selected year
	 * 
	 * @return
	 */
	public int getInt() {
		return Integer.valueOf(getText()).intValue();
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
		this.setText(year + "");
	}

}
