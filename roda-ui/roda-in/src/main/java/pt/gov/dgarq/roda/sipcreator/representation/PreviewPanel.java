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
public class PreviewPanel extends JPanel {

	private static final long serialVersionUID = -3859025143205546060L;

	private JLabel noPreview = null;
	private JLabel loadingPreview = null;

	/**
	 * Create a new Preview panel
	 */
	public PreviewPanel() {
		super(new BorderLayout());
		noPreview();
	}

	/**
	 * Set no preview message
	 */
	public void noPreview() {
		removeAll();
		add(getNoPreviewPanel(), BorderLayout.CENTER);
		repaint();
		revalidate();
	}

	/**
	 * Set preview loading message
	 */
	public void loadingPreview() {
		removeAll();
		add(getLoadingPanel(), BorderLayout.CENTER);
		repaint();
		revalidate();
	}

	/**
	 * Set custom preview message
	 * 
	 * @param message
	 */
	public void preview(String message) {
		removeAll();
		add(getPreviewPanel(message), BorderLayout.CENTER);
		repaint();
		revalidate();
	}

	/**
	 * Set preview
	 * 
	 * @param preview
	 */
	public void setPreview(Component preview) {
		removeAll();
		add(preview, BorderLayout.CENTER);
		repaint();
		revalidate();
	}

	private Component getLoadingPanel() {
		if (loadingPreview == null) {
			loadingPreview = getPreviewPanel(Messages
					.getString("Preview.LOADING"));
		}
		return loadingPreview;
	}

	private Component getNoPreviewPanel() {
		if (noPreview == null) {
			noPreview = getPreviewPanel(Messages
					.getString("Preview.NO_PREVIEW"));
		}
		return noPreview;
	}

	private JLabel getPreviewPanel(String message) {
		JLabel preview = new JLabel(
				String
						.format(
								"<html><p style=\"color: #d1d2d3; font-weight: 900; font-size: 26pt\">%1$s</p><html>",
								message));
		preview.setHorizontalAlignment(JLabel.CENTER);
		return preview;
	}

	/**
	 * Get title
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return Messages.getString("Preview.TITLE");
	}

}
