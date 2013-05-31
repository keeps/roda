package pt.gov.dgarq.roda.sipcreator;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;

/**
 * Class to help in SIP Actions
 * 
 * @author Luis Faria
 * 
 */
public class SIPActionHelper {

	private static final Logger logger = Logger
			.getLogger(SIPActionHelper.class);

	/**
	 * Check if save is valid
	 * 
	 * @param parent
	 * @param sip
	 * 
	 * @param showWarning
	 *            where to show warning in validating
	 * @return true if it is valid
	 */
	public static boolean isSaveValid(Component parent, SIP sip,
			boolean showWarning) {
		boolean ret = false;
		try {
			SIPUtility.validateForSaving(sip);
			ret = true;
		} catch (SIPException e2) {
			if (showWarning) {
				JOptionPane.showMessageDialog(parent, Messages.getString(
						"SIPPanel.error.SIP_EXCEPTION", e2.getMessage()),
						Messages.getString("common.ERROR"),
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (InvalidDescriptionObjectException e2) {
			if (showWarning) {
				JOptionPane.showMessageDialog(parent, Messages.getString(
						"SIPPanel.error.INVALID_DESCRIPTIVE_METADATA", e2
								.getMessage()), Messages
						.getString("common.ERROR"), JOptionPane.ERROR_MESSAGE);
			}
		}
		return ret;
	}

	/**
	 * Save SIP Action listener
	 * 
	 * @author Luis Faria
	 * 
	 */
	public interface SaveSipActionListener {
		/**
		 * Called right before saving the SIP
		 */
		public void onBeforeSipSave();

		/**
		 * Called right after saving the SIP
		 */
		public void onAfterSipSave();
	}

	/**
	 * Create a save SIP action
	 * 
	 * @param parent
	 *            the parent component
	 * @param sip
	 *            the SIP to save
	 * @param listener
	 *            interface to listen to save SIP events
	 * @return the action
	 */
	public static Action createSaveSipAction(final Component parent,
			final SIP sip, final SaveSipActionListener listener) {
		return new AbstractAction(Messages.getString("SIPPanel.action.SAVE"),
				Tools.createImageIcon("save.png")) {
			private static final long serialVersionUID = 6891016572198110708L;

			public void actionPerformed(ActionEvent e) {
				listener.onBeforeSipSave();
				if (isSaveValid(parent, sip, true)) {
					Loading.run(Messages.getString(
							"SIPPanel.loading.SAVING_SIP",
							MyClassificationPlanHelper.getInstance()
									.getCompleteReference(
											sip.getDescriptionObject(), sip)),
							new Runnable() {
								public void run() {
									boolean saved;
									try {
										saved = sip.save();
										setEnabled(!saved);
										listener.onAfterSipSave();
									} catch (Exception e) {
										logger.warn("Error saving SIP", e);
										JOptionPane
												.showMessageDialog(
														parent,
														Messages
																.getString(
																		"SIPPanel.error.ERROR_SAVING_SIP",
																		e
																				.getMessage()),
														Messages
																.getString("common.ERROR"),
														JOptionPane.ERROR_MESSAGE);

									}

								}
							});
				}

			}
		};
	}

	/**
	 * Create ingest validate action
	 * 
	 * @param parent
	 * @param sip
	 * @return the action
	 */
	public static Action createIngestValidateAction(final Component parent,
			final SIP sip) {
		return new AbstractAction(Messages
				.getString("SIPPanel.action.INGEST_VALIDATE"), Tools
				.createImageIcon("valid.png")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				try {
					SIPUtility.validateForIngest(sip);
					JOptionPane.showMessageDialog(parent, Messages
							.getString("SIPPanel.VALID_MESSAGE"), Messages
							.getString("SIPPanel.VALIDATE_TITLE"),
							JOptionPane.INFORMATION_MESSAGE);
				} catch (SIPException e2) {
					JOptionPane.showMessageDialog(parent, Messages.getString(
							"SIPPanel.error.SIP_EXCEPTION", e2.getMessage()),
							Messages.getString("SIPPanel.VALIDATE_TITLE"),
							JOptionPane.ERROR_MESSAGE);

				} catch (InvalidDescriptionObjectException e2) {
					JOptionPane.showMessageDialog(parent, Messages.getString(
							"SIPPanel.error.INVALID_DESCRIPTIVE_METADATA", e2
									.getMessage()), Messages
							.getString("SIPPanel.VALIDATE_TITLE"),
							JOptionPane.ERROR_MESSAGE);
				}

			}

		};
	}
}
