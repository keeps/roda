package pt.gov.dgarq.roda.sipcreator.representation;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public abstract class AbstractRepresentationPanel extends JPanel {
	private static final long serialVersionUID = 7694374114411484700L;
	private static final Logger logger = Logger
			.getLogger(AbstractRepresentationPanel.class);

	private JTabbedPane tabbedpanePreviewMetadata = null;
	private MetadataPanel panelMetadata = null;
	private PreviewPanel panelPreview = null;

	private SIPRepresentationObject representationObject = null;

	/**
	 * Constructs a new {@link AbstractRepresentationPanel}.
	 * 
	 * @param rObject
	 * 
	 * @throws InvalidRepresentationException
	 */
	public AbstractRepresentationPanel(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {
		setRepresentationObject(rObject);
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
	protected void setRepresentationObject(SIPRepresentationObject rObject)
			throws InvalidRepresentationException {
		this.representationObject = rObject;
	}

	protected JTabbedPane getPreviewMetadataPanel() {
		if (this.tabbedpanePreviewMetadata == null) {
			this.tabbedpanePreviewMetadata = new JTabbedPane();

			this.tabbedpanePreviewMetadata.addTab(getPreviewPanel().getTitle(),
					getPreviewPanel());
			this.tabbedpanePreviewMetadata.addTab(
					getMetadataPanel().getTitle(), getMetadataPanel());
		}
		return this.tabbedpanePreviewMetadata;
	}

	protected MetadataPanel getMetadataPanel() {
		if (this.panelMetadata == null) {
			this.panelMetadata = new MetadataPanel();
		}
		return this.panelMetadata;
	}

	protected void updateMetadata() {
		getMetadataPanel().loadingMetadata();
		Component metadata = getMetadata();
		if (metadata != null) {
			getMetadataPanel().setMetadata(metadata);
		} else {
			getMetadataPanel().noMetadata();
		}
		getMetadataPanel().repaint();
		getMetadataPanel().revalidate();
	}

	protected abstract Component getMetadata();

	protected PreviewPanel getPreviewPanel() {
		if (this.panelPreview == null) {
			this.panelPreview = new PreviewPanel();
			panelPreview.addAncestorListener(new AncestorListener() {

				public void ancestorAdded(AncestorEvent event) {
					logger.debug("Preview panel ancestor added");
					startPreview();
				}

				public void ancestorMoved(AncestorEvent event) {
					// nothing to do

				}

				public void ancestorRemoved(AncestorEvent event) {
					stopPreview();
				}

			});
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					updatePreview();
				}

			});
		}
		return this.panelPreview;
	}

	protected void updatePreview() {
		logger.debug("Updating preview");
		getPreviewPanel().loadingPreview();
		Component preview = getPreview();
		if (preview != null) {
			getPreviewPanel().setPreview(preview);
		} else {
			getPreviewPanel().noPreview();
		}
		startPreview();
		getPreviewPanel().repaint();
		getPreviewPanel().revalidate();

	}

	protected abstract Component getPreview();

	protected abstract void startPreview();

	protected abstract void stopPreview();

}
