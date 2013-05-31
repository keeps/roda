package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.Text;
import pt.gov.dgarq.roda.sipcreator.Messages;

/**
 * 
 * @author Luis Faria
 * 
 */
public class AccessRestrictEditor extends AbstractDescriptionEditor {

	private final JComboBox jComboBox;

	private static final List<String> options = new ArrayList<String>();
	static {
		options.add(Messages.getString("AccessRestrict.NO_RESTRICTIONS"));
		options.add(Messages.getString("AccessRestrict.TOP_SECRET"));
		options.add(Messages.getString("AccessRestrict.SECRET"));
		options.add(Messages.getString("AccessRestrict.CONFIDENTIAL"));
		options.add(Messages.getString("AccessRestrict.RESERVED"));
		options.add(Messages.getString("AccessRestrict.PERSONAL_DATA"));
		options.add(Messages.getString("AccessRestrict.ENTERPRISE_DATA"));
	}

	private String selected = options.get(0);
	private EadCValue lastValue;

	/**
	 * Create a new description level editor
	 */
	public AccessRestrictEditor() {
		jComboBox = new JComboBox();
		Dimension size = new Dimension(WIDTH, 30);
		jComboBox.setMaximumSize(size);
		jComboBox.setPreferredSize(size);
		jComboBox.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				// nothing to do
			}

			public void focusLost(FocusEvent e) {
				if (jComboBox.isEnabled()) {
					EadCValue value = getValue();
					if (!value.equals(lastValue)) {
						onDataChanged(getValue());
					}
				}
			}

		});
		lastValue = null;
		update();
	}

	protected void update() {
		jComboBox.removeAllItems();
		if (jComboBox.isEnabled()) {
			for (String option : options) {
				jComboBox.addItem(option);
			}
		}
		jComboBox.setSelectedItem(selected);
	}

	/**
	 * Get editor component
	 * 
	 * @return the editor component
	 */
	public Component getComponent() {
		return jComboBox;
	}

	/**
	 * Get EAD Component value
	 * 
	 * @return the selected description level
	 */
	public EadCValue getValue() {
		return new Text((String) jComboBox.getSelectedItem());
	}

	/**
	 * Set EAD Component value
	 * 
	 * @param value
	 *            the description level to select
	 */
	public void setValue(EadCValue value) {
		if (value instanceof DescriptionLevel) {
			selected = ((Text) value).getText();
			update();
			lastValue = value;
		}
	}

	/**
	 * Is description level editor read only, i.e. not editable
	 * 
	 * @return true if read only
	 */
	public boolean isReadonly() {
		return !jComboBox.isEnabled();
	}

	/**
	 * Set description level read only, i.e not editable
	 * 
	 * @param readonly
	 */
	public void setReadonly(boolean readonly) {
		jComboBox.setEnabled(!readonly);
		update();
	}

	/**
	 * Is chosen description level valid
	 * 
	 * @return true if it is valid
	 */
	public boolean isValid() {
		return true;
	}

}
