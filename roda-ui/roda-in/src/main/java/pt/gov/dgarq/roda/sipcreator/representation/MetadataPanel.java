/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import pt.gov.dgarq.roda.sipcreator.Messages;

/**
 * @author Luis Faria
 * 
 */
public class MetadataPanel extends JPanel {
	private static final long serialVersionUID = 863445423936300263L;

	private JLabel noMetadata = null;
	private JLabel loadingPreview = null;

	/**
	 * Create a new metadata panel
	 */
	public MetadataPanel() {
		super(new BorderLayout());
		noMetadata();
	}

	/**
	 * Set no metadata message
	 */
	public void noMetadata() {
		removeAll();
		add(getNoMetadataPanel(), BorderLayout.CENTER);
		repaint();
		revalidate();
	}

	/**
	 * Set loading message
	 */
	public void loadingMetadata() {
		removeAll();
		add(getLoadingPanel(), BorderLayout.CENTER);
		repaint();
		revalidate();
	}

	/**
	 * Set metadata
	 * 
	 * @param metadata
	 */
	public void setMetadata(Component metadata) {
		removeAll();
		add(metadata, BorderLayout.CENTER);
		repaint();
		revalidate();
	}

	private Component getLoadingPanel() {
		if (loadingPreview == null) {
			loadingPreview = new JLabel(
					String
							.format(
									"<html><p style=\"color: #d1d2d3; font-weight: 900; font-size: 26pt\">%1$s</p><html>",
									Messages.getString("Metadata.LOADING")));
			loadingPreview.setHorizontalAlignment(JLabel.CENTER);

		}
		return loadingPreview;
	}

	private Component getNoMetadataPanel() {
		if (noMetadata == null) {
			noMetadata = new JLabel(
					String
							.format(
									"<html><p style=\"color: #d1d2d3; font-weight: 900; font-size: 26pt\">%1$s</p><html>",
									Messages.getString("Metadata.NO_METADATA")));
			noMetadata.setHorizontalAlignment(JLabel.CENTER);
		}
		return noMetadata;
	}

	/**
	 * Get title
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return Messages.getString("Metadata.TITLE");
	}

}
