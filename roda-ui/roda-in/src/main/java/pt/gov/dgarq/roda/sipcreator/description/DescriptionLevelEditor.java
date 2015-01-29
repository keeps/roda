package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComboBox;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelManager;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;

/**
 * 
 * @author Luis Faria
 * 
 */
public class DescriptionLevelEditor extends AbstractDescriptionEditor {

	private final JComboBox jComboBox;

	private static final LocalizedDescriptionLevel[] classificationPlanLevels;
	static {
		int size = DescriptionLevelManager
				.getAllButRepresentationsDescriptionLevels().size();
		classificationPlanLevels = new LocalizedDescriptionLevel[size];
		for (int i = 0; i < size; i++) {
			classificationPlanLevels[i] = new LocalizedDescriptionLevel(
					DescriptionLevelManager
							.getAllButRepresentationsDescriptionLevels().get(i));
		}
	}

	private static final LocalizedDescriptionLevel[] sipLevels;
	static {
		int size = DescriptionLevelManager
				.getRepresentationsDescriptionLevels().size();
		sipLevels = new LocalizedDescriptionLevel[size];
		for (int i = 0; i < size; i++) {
			sipLevels[i] = new LocalizedDescriptionLevel(
					DescriptionLevelManager
							.getRepresentationsDescriptionLevels().get(i));
		}
	}

	private static final LocalizedDescriptionLevel[] fileChildLevels;
	static {
		int size = DescriptionLevelManager.getRepresentationsDescriptionLevels().size();
		fileChildLevels = new LocalizedDescriptionLevel[size];
		for (int i = 0; i < size; i++) {
			fileChildLevels[i] = new LocalizedDescriptionLevel(
					DescriptionLevelManager.getRepresentationsDescriptionLevels().get(i));
		}
	}

	private DescriptionLevel selected = DescriptionLevelManager
			.getRepresentationsDescriptionLevels().get(0);
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
	 * Set the file child flag, only allowing for {@link DescriptionLevel#ITEM}
	 * level to be set.
	 * 
	 * @param fileChild
	 *            Whereas the description object associated with this editor is
	 *            child of a description object with level
	 *            {@link DescriptionLevel#FILE}.
	 */
	public void setFileChild(boolean fileChild) {
		this.fileChild = fileChild;
	}

}
