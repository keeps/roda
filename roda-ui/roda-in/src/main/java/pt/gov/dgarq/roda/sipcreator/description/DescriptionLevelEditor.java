package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComboBox;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;

/**
 * 
 * @author Luis Faria
 * 
 */
public class DescriptionLevelEditor extends AbstractDescriptionEditor {

	private final JComboBox jComboBox;

	private static final LocalizedDescriptionLevel[] classificationPlanLevels = new LocalizedDescriptionLevel[] {
			new LocalizedDescriptionLevel(DescriptionLevel.FONDS),
			new LocalizedDescriptionLevel(DescriptionLevel.SUBFONDS),
			new LocalizedDescriptionLevel(DescriptionLevel.CLASS),
			new LocalizedDescriptionLevel(DescriptionLevel.SUBCLASS),
			new LocalizedDescriptionLevel(DescriptionLevel.SERIES),
			new LocalizedDescriptionLevel(DescriptionLevel.SUBSERIES)

	};

	private static final LocalizedDescriptionLevel[] sipLevels = new LocalizedDescriptionLevel[] {
			new LocalizedDescriptionLevel(DescriptionLevel.ITEM),
			new LocalizedDescriptionLevel(DescriptionLevel.FILE) };

	private static final LocalizedDescriptionLevel[] fileChildLevels = new LocalizedDescriptionLevel[] { new LocalizedDescriptionLevel(
			DescriptionLevel.ITEM) };

	private DescriptionLevel selected = DescriptionLevel.ITEM;
	private EadCValue lastValue;
	private boolean fileChild;

	/**
	 * Create a new description level editor
	 */
	public DescriptionLevelEditor() {
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
		fileChild = false;
		update();
	}

	protected void update() {
		jComboBox.removeAllItems();
		if (jComboBox.isEnabled() && fileChild) {
			for (LocalizedDescriptionLevel level : fileChildLevels) {
				jComboBox.addItem(level);
			}
		} else if (jComboBox.isEnabled()) {
			for (LocalizedDescriptionLevel level : sipLevels) {
				jComboBox.addItem(level);
			}
		} else {
			for (LocalizedDescriptionLevel level : classificationPlanLevels) {
				jComboBox.addItem(level);
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
		return (LocalizedDescriptionLevel) jComboBox.getSelectedItem();
	}

	/**
	 * Set EAD Component value
	 * 
	 * @param value
	 *            the description level to select
	 */
	public void setValue(EadCValue value) {
		if (value instanceof DescriptionLevel) {
			selected = (DescriptionLevel) value;
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

	/**
	 * If the description object associated with this editor is child of a
	 * description object with level {@link DescriptionLevel#FILE}. 
	 * 
	 * @return true if the above condition applies, false otherwise
	 */
	public boolean isFileChild() {
		return fileChild;
	}

	/**
	 * Set the file child flag, only allowing for {@link DescriptionLevel#ITEM} level to be set.
	 * 
	 * @param fileChild
	 * Whereas the description object associated with this editor is child of a
	 * description object with level {@link DescriptionLevel#FILE}.
	 */
	public void setFileChild(boolean fileChild) {
		this.fileChild = fileChild;
	}

}
