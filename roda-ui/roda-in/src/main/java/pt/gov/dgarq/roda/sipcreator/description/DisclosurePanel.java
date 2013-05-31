package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangedEvent;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.SpringUtilities;
import pt.gov.dgarq.roda.sipcreator.Tools;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class DisclosurePanel extends JPanel {
	private static final long serialVersionUID = 5233114313220382199L;

	private DescriptionObject descObj;

	private final List<DescriptionElement> elements;

	private JPanel panelHeader = null;

	private JLabel labelHeader;
	private String title = null;

	private JPanel contents = null;

	private boolean optionalVisible = false;

	private boolean readonly = false;

	/**
	 * Create a new disclosure panel
	 * 
	 * @param title
	 * @param descObj
	 */
	public DisclosurePanel(String title, DescriptionObject descObj) {
		this.descObj = descObj;
		this.elements = new ArrayList<DescriptionElement>();
		setTitle(title);
		initComponents();
	}

	/**
	 * Get the description object
	 * 
	 * @return the description object
	 */
	public DescriptionObject getDescriptionObject() {
		return descObj;
	}

	/**
	 * Set the description object, recursively setting it on all containing
	 * elements
	 * 
	 * @param descriptionObject
	 */
	public void setDescriptionObject(DescriptionObject descriptionObject) {
		descObj = descriptionObject;
		for (DescriptionElement element : elements) {
			element.setDescriptionObject(descriptionObject);
		}

	}

	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(getHeaderPanel());
		add(getContents());
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

	}

	private JPanel getHeaderPanel() {
		if (this.panelHeader == null) {
			this.panelHeader = new JPanel();
			this.panelHeader.setLayout(new BoxLayout(this.panelHeader,
					BoxLayout.X_AXIS));

			this.panelHeader.add(getHeaderLabel());
			this.panelHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
		}
		return this.panelHeader;
	}

	private JLabel getHeaderLabel() {
		if (this.labelHeader == null) {
			this.labelHeader = new JLabel(
					String
							.format(
									"<html><p style='font-size: 14; font-weight: bold'>%1$s</p></html>", getTitle())); //$NON-NLS-1$
			this.labelHeader.setOpaque(true);
			this.labelHeader.setBackground(new Color(88, 89, 91));
			this.labelHeader.setForeground(Color.WHITE);
			this.labelHeader.setBorder(BorderFactory.createEmptyBorder(2, 5, 2,
					5));

		}
		return this.labelHeader;
	}

	/**
	 * Get the disclosure panel title
	 * 
	 * @return the disclosure panel title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Set the disclosure panel title
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		if (this.title != title) {
			this.title = title;
		}
	}

	protected JPanel getContents() {
		if (contents == null) {
			contents = new JPanel(new SpringLayout());
			contents.setAlignmentX(Component.LEFT_ALIGNMENT);
		}
		return contents;

	}

	/**
	 * Add a new element to the disclosure panel
	 * 
	 * @param element
	 *            the element
	 * @param editor
	 *            the element editor
	 * @param mandatory
	 *            is the new element mandatory. If true element will be shown
	 *            even when show optional fields is false
	 */
	public void addElement(String element, DescriptionEditor editor,
			boolean mandatory) {
		DescriptionElement descElement = new DescriptionElement(element,
				editor, mandatory);
		descElement.setDescriptionObject(descObj);
		elements.add(descElement);
	}

	/**
	 * Are optional elements visible
	 * 
	 * @return true if all elements are visible, false if only mandatory
	 *         elements are visible
	 */
	public boolean isOptionalVisible() {
		return optionalVisible;
	}

	/**
	 * Set optional elements visible
	 * 
	 * @param optionalVisible
	 *            true if all elements are visible, false if only mandatory
	 *            elements are visible
	 */
	public void setOptionalVisible(boolean optionalVisible) {
		this.optionalVisible = optionalVisible;
		update();
	}

	/**
	 * Is disclosure panel read only, i. e. not editable
	 * 
	 * @return true if read only
	 */
	public boolean isReadOnly() {
		return readonly;
	}

	/**
	 * Set disclosure panel read only, i. e. not editable
	 * 
	 * @param readonly
	 * 
	 */
	public void setReadOnly(boolean readonly) {
		this.readonly = readonly;
		update();
	}

	/**
	 * Put elements in the right place
	 */
	public void update() {
		getContents().removeAll();
		int elementCount = 0;
		for (DescriptionElement element : elements) {
			if (optionalVisible || element.isMandatory()) {
				JTextArea jLabel = new JTextArea(element.getLabel() + ":");
				Tools.makeTextAreaLookLikeLable(jLabel);
				element.getEditor().setReadonly(readonly);
				final Component editor = element.getEditor().getComponent();
				// jLabel.setLabelFor(editor);
				getContents().add(jLabel);
				getContents().add(editor);

				jLabel.addFocusListener(new FocusListener() {

					public void focusGained(FocusEvent e) {
						editor.requestFocus();
					}

					public void focusLost(FocusEvent e) {
						// do nothing
					}

				});

				Dimension size = new Dimension(150, editor.getHeight());
				jLabel.setMaximumSize(size);
				jLabel.setPreferredSize(size);

				elementCount++;
			}
		}
		if (elementCount > 0) {
			SpringUtilities.makeCompactGrid(getContents(), elementCount, 2, 25,
					5, 5, 5);
			setVisible(true);
		} else {
			setVisible(false);
		}
	}

	/**
	 * Description element container class
	 */
	public class DescriptionElement {
		private final String element;
		private final DescriptionEditor editor;
		private final boolean mandatory;
		private DescriptionObject descObj = null;

		/**
		 * Create a new description element
		 * 
		 * @param element
		 *            the element code
		 * @param editor
		 *            the element editor
		 * @param mandatory
		 *            is element mandatory
		 */
		public DescriptionElement(String element, DescriptionEditor editor,
				boolean mandatory) {

			this.editor = editor;
			this.element = element;
			this.mandatory = mandatory;

			editor.addDataChangedListener(new DataChangeListener() {

				public void dataChanged(DataChangedEvent e) {
					EadCValue value = getEditor().getValue();
					if (descObj != null) {
						descObj.setValue(getElement(), value);
					}
				}

			});

		}

		/**
		 * Get element code
		 * 
		 * @return the element code
		 */
		public String getElement() {
			return element;
		}

		/**
		 * Get element editor
		 * 
		 * @return the element editor
		 */
		public DescriptionEditor getEditor() {
			return editor;
		}

		/**
		 * Is element mandatory
		 * 
		 * @return true if it is mandatory
		 */
		public boolean isMandatory() {
			return mandatory;
		}

		/**
		 * Set description object, setting the correct value in the element
		 * editor and updating call back on editor change
		 * 
		 * @param descObj
		 */
		public void setDescriptionObject(DescriptionObject descObj) {
			this.descObj = descObj;
			if (descObj != null) {
				editor.setValue(descObj.getValue(element));
			} else {
				editor.setValue(null);
			}
		}

		/**
		 * Get description object
		 * 
		 * @return the description object
		 */
		public DescriptionObject getDescriptionObject() {
			return descObj;
		}

		/**
		 * Get element label, as defined in messages
		 * 
		 * @return the element localized label
		 */
		public String getLabel() {
			return Messages.getString("DescriptionField." + getElement());
		}

	}

}
