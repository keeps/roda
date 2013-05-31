/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.Text;

/**
 * @author Luis Faria
 * 
 */
public class TextAreaDescriptionEditor extends AbstractDescriptionEditor {

	private final JScrollPane scroll;
	private final JTextArea textArea;

	private EadCValue lastValue;

	/**
	 * Create a new text field description editor
	 */
	public TextAreaDescriptionEditor() {
		textArea = new JTextArea();
		scroll = new JScrollPane(textArea);
		scroll.setPreferredSize(new Dimension(WIDTH, 75));
		scroll.setMaximumSize(new Dimension(WIDTH, 125));
		textArea.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				// nothing to do
			}

			public void focusLost(FocusEvent e) {
				if (textArea.isEditable()) {
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
		return scroll;
	}

	/**
	 * Get value
	 * 
	 * @return the EAD Component value
	 */
	public EadCValue getValue() {
		return new Text(textArea.getText());
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
			textArea.setText(text.getText());
			lastValue = value;
		}
	}

	/**
	 * Is editor enabled
	 * 
	 * @return true if enabled
	 */
	public boolean isReadonly() {
		return !textArea.isEditable();
	}

	/**
	 * Set editor enabled
	 * 
	 * @param readonly
	 * 
	 */
	public void setReadonly(boolean readonly) {
		textArea.setEditable(!readonly);
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
