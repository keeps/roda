/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.Text;

/**
 * @author Luis Faria
 * 
 */
public class TextFieldDescriptionEditor extends AbstractDescriptionEditor {

	private final JTextField textField;

	private EadCValue lastValue;

	/**
	 * Create a new text field description editor
	 */
	public TextFieldDescriptionEditor() {
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(WIDTH, 30));
		textField.setMaximumSize(new Dimension(WIDTH, 30));
		lastValue = null;
		textField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				// nothing to do
			}

			public void focusLost(FocusEvent e) {
				if (textField.isEditable()) {
					EadCValue value = getValue();
					if (!value.equals(lastValue)) {
						onDataChanged(getValue());
						lastValue = value;
					}
				}
			}

		});
	}

	/**
	 * Get editor component
	 * 
	 * @return the text field
	 */
	public Component getComponent() {
		return textField;
	}

	/**
	 * Get value
	 * 
	 * @return the EAD Component value
	 */
	public EadCValue getValue() {
		return new Text(textField.getText());
	}

	/**
	 * Set value
	 * 
	 * @param value
	 *            the EAD Component value
	 */
	public void setValue(EadCValue value) {
		if (value instanceof Text) {
			Text text = (Text) value;
			textField.setText(text.getText());
			lastValue = value;
		}
	}

	/**
	 * Is editor enabled
	 * 
	 * @return true if enabled
	 */
	public boolean isReadonly() {
		return textField.isEditable();
	}

	/**
	 * Set editor enabled
	 * 
	 * @param readonly
	 * 
	 */
	public void setReadonly(boolean readonly) {
		textField.setEditable(!readonly);
	}

	/**
	 * Is current value valid
	 * 
	 * @return this editor accepts all text, therefore it is always valid
	 */
	public boolean isValid() {
		return true;
	}
}
