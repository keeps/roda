/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation;

import java.awt.Frame;

import javax.swing.JDialog;

import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.representation.RepresentationTypeSelectionPanel.RepresentationTypeSelectionListener;

/**
 * @author Luis Faria
 * 
 */
public class RepresentationTypeSelectionDialog extends JDialog {
	private static final long serialVersionUID = 7437188406995670839L;

	private final RepresentationTypeSelectionPanel panel;

	/**
	 * Create a new representation type selection dialog
	 * 
	 * @param owner
	 */
	public RepresentationTypeSelectionDialog(Frame owner) {
		super(owner, Messages
				.getString("RepresentationTypeSelectionDialog.TITLE"), true);
		this.panel = new RepresentationTypeSelectionPanel();
		this.panel
				.addRepresentationTypeSelectionListener(new RepresentationTypeSelectionListener() {

					public void onRepresentationTypeSelected(String type,
							String subtype) {
						setVisible(false);

					}

				});
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(null);

	}

	/**
	 * Add a new representation type selection listener
	 * 
	 * @param listener
	 */
	public void addRepresentationTypeSelectionListener(
			RepresentationTypeSelectionListener listener) {
		panel.addRepresentationTypeSelectionListener(listener);
	}

	/**
	 * Remove a representation type selection listener
	 * 
	 * @param listener
	 */
	public void removeRepresentationTypeSelectionListener(
			RepresentationTypeSelectionListener listener) {
		panel.removeRepresentationTypeSelectionListener(listener);
	}

}
