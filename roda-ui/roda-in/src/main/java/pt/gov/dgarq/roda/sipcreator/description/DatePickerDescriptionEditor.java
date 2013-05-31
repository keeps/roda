/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.Text;

import com.toedter.calendar.JDateChooser;

/**
 * @author Luis Faria
 * 
 */
public class DatePickerDescriptionEditor extends AbstractDescriptionEditor {

	private static final Logger logger = Logger
			.getLogger(DatePickerDescriptionEditor.class);

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd");

	private final JDateChooser jDateChooser;

	private EadCValue lastValue;

	/**
	 * Create a new Date Picker description editor
	 */
	public DatePickerDescriptionEditor() {
		this.jDateChooser = new JDateChooser();
		this.jDateChooser.setDateFormatString("yyyy-MM-dd");
		Dimension size = new Dimension(WIDTH, 30);
		jDateChooser.setMaximumSize(size);
		jDateChooser.setPreferredSize(size);

		jDateChooser.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("date")
						&& jDateChooser.isEnabled()) {
					EadCValue value = getValue();
					if (!value.equals(lastValue)) {
						onDataChanged(getValue());
					}
				}
			}

		});
		lastValue = null;
	}

	/**
	 * Get date picker component
	 * 
	 * @return the date picker component
	 */
	public Component getComponent() {
		return jDateChooser;
	}

	/**
	 * Get EAD Component value as a Text with the date in ISO 8601 format
	 * 
	 * @return the EAD Component value
	 */
	public EadCValue getValue() {
		Text text = null;
		Date date = jDateChooser.getDate();
		if (date != null) {
			String isoDate = DATE_FORMAT.format(date);
			text = new Text(isoDate);
		}
		return text;
	}

	/**
	 * Set EAD Component value as a Text with the date in ISO 8601 format
	 * 
	 * @param value
	 *            the EAD Component value
	 */
	public void setValue(EadCValue value) {
		if (value instanceof Text) {
			Text text = (Text) value;
			try {
				jDateChooser.setDate(DateParser.parse(text.getText()));
			} catch (InvalidDateException e) {
				logger.error("Error setting date value", e);
			}
			lastValue = value;
		}

	}

	/**
	 * Is date picker read only, i.e. not editable
	 * 
	 * @return true if read only
	 */
	public boolean isReadonly() {
		return !jDateChooser.isEnabled();
	}

	/**
	 * Set date picker read only, i.e. not editable
	 * 
	 * @param readonly
	 */
	public void setReadonly(boolean readonly) {
		jDateChooser.setEnabled(!readonly);
	}

	/**
	 * Is date valid
	 * 
	 * @return true if valid
	 */
	public boolean isValid() {
		return true;
	}

}
