package pt.gov.dgarq.roda.sipcreator.representation.database;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.representation.InvalidRepresentationException;
import pt.gov.dgarq.roda.sipcreator.representation.MetadataPanel;
import pt.gov.dgarq.roda.sipcreator.representation.PreviewPanel;

/**
 * @author Rui Castro
 * @author Luis Faria
 * 
 */
public class RdbRepresentationPanel extends JPanel {
	private static final long serialVersionUID = 4021415781383360357L;

	private JLabel labelHeader = null;

	private JSplitPane splitPane = null;

	private JTabbedPane viewTabs = null;

	private PreviewPanel previewPanel = null;

	private MetadataPanel metadataPanel = null;

	private DbmsSelectPanel dbmsSelectPanel = null;

	private SIPRepresentationObject representationObject = null;

	/**
	 * Constructs a new {@link RdbRepresentationPanel}.
	 * 
	 * @param rObject
	 * 
	 * @throws InvalidRepresentationException
	 */
	public RdbRepresentationPanel(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {

		setRepresentationObject(rObject);
		initComponents();
	}

	/**
	 * @return the representationObject
	 */
	public SIPRepresentationObject getRepresentationObject() {
		return representationObject;
	}

	/**
	 * @param rObject
	 *            the representationObject to set
	 * 
	 * @throws InvalidRepresentationException
	 */
	public void setRepresentationObject(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {

		if (rObject == null) {
			throw new InvalidRepresentationException("Representation is null"); //$NON-NLS-1$
		} else if (!RepresentationObject.RELATIONAL_DATABASE
				.equalsIgnoreCase(rObject.getType())) {
			throw new InvalidRepresentationException(
					"Invalid representation type - " + rObject.getType()); //$NON-NLS-1$
		} else {
			this.representationObject = rObject;
		}

	}

	private void initComponents() {
		setLayout(new BorderLayout());
		add(getHeaderPanel(), BorderLayout.NORTH);
		add(getSplitPane(), BorderLayout.CENTER);
	}

	private JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					getDbmsSelectPanel(), getViewTabs());
			splitPane.setDividerLocation(300);
			getViewTabs().setMinimumSize(new Dimension(250, 50));
		}

		return splitPane;
	}

	private DbmsSelectPanel getDbmsSelectPanel() {
		if (this.dbmsSelectPanel == null) {
			this.dbmsSelectPanel = new DbmsSelectPanel(
					getRepresentationObject());
		}
		return this.dbmsSelectPanel;
	}

	private JTabbedPane getViewTabs() {
		if (viewTabs == null) {
			viewTabs = new JTabbedPane();

			viewTabs.addTab(getPreviewPanel().getTitle(), getPreviewPanel());
			viewTabs.addTab(getMetadataPanel().getTitle(), getMetadataPanel());

			viewTabs.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
		}
		return viewTabs;
	}

	private PreviewPanel getPreviewPanel() {
		if (previewPanel == null) {
			previewPanel = new PreviewPanel();
		}
		return previewPanel;
	}

	private MetadataPanel getMetadataPanel() {
		if (metadataPanel == null) {
			metadataPanel = new MetadataPanel();
		}
		return metadataPanel;
	}

	private JLabel getHeaderPanel() {
		if (this.labelHeader == null) {
			this.labelHeader = new JLabel(
					String
							.format(
									"<html><p style='font-size: 16; font-weight: bold'>%1$s</p><html>",
									Messages
											.getString("Creator.relational_database.TITLE")));
			this.labelHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
					5));
		}
		return this.labelHeader;
	}

}
