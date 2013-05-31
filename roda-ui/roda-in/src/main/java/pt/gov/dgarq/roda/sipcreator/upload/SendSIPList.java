package pt.gov.dgarq.roda.sipcreator.upload;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;
import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.RodaClientFactory;
import pt.gov.dgarq.roda.sipcreator.SIP;
import pt.gov.dgarq.roda.sipcreator.SIPCreator;
import pt.gov.dgarq.roda.sipcreator.SIPCreatorConfig;
import pt.gov.dgarq.roda.sipcreator.RodaClientFactory.RodaClientFactoryListener;

/**
 * 
 * @author Luis Faria
 * 
 */
public class SendSIPList {

	private static final Logger logger = Logger.getLogger(SendSIPList.class);

	/**
	 * Send type
	 * 
	 */
	public enum Type {
		/**
		 * Send by HTTP
		 */
		HTTP,
		/**
		 * Send by FTP
		 */
		FTP,
		/**
		 * Send by postal mail
		 */
		POSTAL_MAIL
	}

	private JDialog dialog = null;

	private JPanel layout = null;

	private JLabel status = null;

	private JProgressBar progressBar = null;

	private JLabel progressLabel = null;

	private JButton cancel = null;

	private List<SIP> sips;

	private Type type;

	private Thread workingThread = null;

	/**
	 * Send SIP List
	 * 
	 */
	public SendSIPList() {

	}

	protected JDialog getDialog() {
		if (dialog == null) {
			dialog = new JDialog(SIPCreator.getInstance().getMainFrame(),
					Messages.getString("SendSIPList.TITLE"), true);
			dialog.setLayout(new BorderLayout());
			dialog.add(getLayout(), BorderLayout.CENTER);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setResizable(false);
			dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
		return dialog;
	}

	private Component getLayout() {
		if (layout == null) {
			layout = new JPanel();
			layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
			layout.add(getStatus());
			layout.add(getProgressBar());
			layout.add(getProgressLabel());
			layout.add(Box.createVerticalGlue());
			layout.add(getCancelButton());
			layout.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			layout.setPreferredSize(new Dimension(600, 120));
		}
		return layout;
	}

	private JLabel getStatus() {
		if (status == null) {
			status = new JLabel();
			status.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		}
		return status;
	}

	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar(0, sips.size()
					* ProgressState.values().length);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			progressBar.setPreferredSize(new Dimension(400, 20));
		}
		return progressBar;
	}

	private JLabel getProgressLabel() {
		if (progressLabel == null) {
			progressLabel = new JLabel();
			progressLabel
					.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		}
		return progressLabel;
	}

	private JButton getCancelButton() {
		if (cancel == null) {
			cancel = new JButton(Messages.getString("SendSIPList.CANCEL"));
			cancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					cancel();
				}

			});
		}
		return cancel;
	}

	private RODAClient rodaClient = null;
	private boolean work = true;

	protected enum ProgressState {
		CREATING_TEMP_FOLDER, CREATING_PACKAGE, UPLOADING, MOVE_TO_SEND
	}

	protected File createSIPFile(SIP sip) throws IOException {
		File sipFile;
		File outbox = SIPCreatorConfig.getInstance().getSipOutboxDir();
		String baseFileName = sip.getCompleteReference().replace('/', '_');
		String fileNameExt = ".sip";
		sipFile = new File(outbox.getAbsoluteFile(), baseFileName + ".sip");
		if (sipFile.exists()) {
			sipFile = File.createTempFile(baseFileName, fileNameExt, outbox);
		} else {
			sipFile.createNewFile();
		}

		return sipFile;
	}

	private Thread getWorkingThread() {
		if (workingThread == null) {
			workingThread = new Thread() {
				public void run() {
					if (type == Type.HTTP && rodaClient == null) {
						hide();
						return;
					}
					int index = 0;
					for (SIP sip : sips) {
						File sipPackage;
						try {
							if (!work) {
								break;
							}
							setProgress(index, sip,
									ProgressState.CREATING_TEMP_FOLDER);
							sipPackage = createSIPFile(sip);
							if (!work) {
								break;
							}
							setProgress(index, sip,
									ProgressState.CREATING_PACKAGE);
							File sipDir = sip.getDirectory();
							SIPUtility.writeSIPPackage(sip, sipPackage);
							// re-setting directory because writeSIPPackage
							// changes it
							sip.setDirectory(sipDir);
							if (!work) {
								break;
							}
							setProgress(index, sip, ProgressState.UPLOADING);
							if (type == Type.HTTP) {
								OnlineSendUtility.sendSIP(sipPackage,
										rodaClient);
							} else if (type == Type.FTP
									|| type == Type.POSTAL_MAIL) {
								OfflineSendUtility.sendSIP(sipPackage);
							}
							setProgress(index, sip, ProgressState.MOVE_TO_SEND);
							logger.debug("Marking SIP as sent");
							sip.setSent(true);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(SendSIPList.this
									.getDialog(), e.getMessage(), Messages
									.getString("common.ERROR"),
									JOptionPane.ERROR_MESSAGE);
						} catch (SIPException e) {
							JOptionPane.showMessageDialog(SendSIPList.this
									.getDialog(), e.getMessage(), Messages
									.getString("common.ERROR"),
									JOptionPane.ERROR_MESSAGE);
						}
						index++;
					}
					logger.debug("Repainting fonds panel");
					SIPCreator.getInstance().getMainFrame().getMainPanel()
							.getFondsPanel().repaint();
					hide();
					if (type == Type.FTP) {
						OfflineSendUtility
								.showInstructions("/FTPInstructions.html");
					} else if (type == Type.POSTAL_MAIL) {
						OfflineSendUtility
								.showInstructions("/PostalMailInstructions.html");
					}

				}
			};
		}
		return workingThread;
	}

	protected void setProgress(int sipIndex, SIP sip, ProgressState state) {
		getDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		String message = null;
		String sipCompleteReference = sip.getCompleteReference();

		if (state.equals(ProgressState.CREATING_TEMP_FOLDER)) {
			message = Messages.getString(
					"SendSIPList.status.CREATING_TEMP_FOLDER",
					sipCompleteReference);
		} else if (state.equals(ProgressState.CREATING_PACKAGE)) {
			message = Messages.getString("SendSIPList.status.CREATING_PACKAGE",
					sipCompleteReference);
		} else if (state.equals(ProgressState.UPLOADING)) {
			message = Messages.getString("SendSIPList.status.UPLOADING",
					sipCompleteReference);
		} else if (state.equals(ProgressState.MOVE_TO_SEND)) {
			message = Messages.getString("SendSIPList.status.MOVE_TO_SEND",
					sipCompleteReference);
		}
		if (message != null) {
			getStatus().setText(message);
		}
		getProgressBar().setValue(
				sipIndex * ProgressState.values().length
						+ Arrays.asList(ProgressState.values()).indexOf(state));
		getProgressLabel().setText(
				Messages.getString("SendSIPList.PROGRESS", sipIndex + 1, sips
						.size()));

	}

	/**
	 * Start processing SIPs
	 * 
	 * @param sips
	 *            the SIPs to process
	 * @param type
	 *            the type of send
	 * 
	 */
	public void start(List<SIP> sips, Type type) {
		this.sips = sips;
		this.type = type;

		if (type == Type.HTTP) {
			RodaClientFactory.getInstance().getRodaClient(
					new RodaClientFactoryListener() {

						public void onCancel() {
							// do nothing
						}

						public void onLogin(RODAClient rodaClient) {
							SendSIPList.this.rodaClient = rodaClient;
							getWorkingThread().start();
							show();
						}

					});
		} else {
			if (OfflineSendUtility.configureExportFolder(getLayout())) {
				getWorkingThread().start();
				show();
			}
		}
	}

	/**
	 * Cancel upload
	 */
	public void cancel() {
		work = false;
		getStatus().setText(Messages.getString("SendSIPList.status.CANCELING"));

	}

	private void show() {
		getDialog().setVisible(true);
	}

	private void hide() {
		getDialog().setVisible(false);
	}

}
