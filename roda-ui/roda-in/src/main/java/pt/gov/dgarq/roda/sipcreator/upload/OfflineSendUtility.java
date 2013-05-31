/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.upload;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.SIPCreator;
import pt.gov.dgarq.roda.sipcreator.Tools;

/**
 * @author Luis Faria
 * 
 */
public class OfflineSendUtility {

	private static final Logger logger = Logger
			.getLogger(OfflineSendUtility.class);
	private static JFileChooser fileChooser;
	private static File exportFolder = null;

	private static JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(Messages
					.getString("OfflineSend.TARGET_DIRECTORY"));
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
		}
		return fileChooser;
	}

	/**
	 * Configure the export folder where to move the SIPs
	 * 
	 * @param parent
	 *            the parent component for the file chooser
	 * @return true if the export folder was successfully configured
	 */
	public static boolean configureExportFolder(Component parent) {
		int result = getFileChooser().showOpenDialog(parent);
		File selectedFile = fileChooser.getSelectedFile();

		if (result == JFileChooser.APPROVE_OPTION) {
			exportFolder = selectedFile;
		}
		return selectedFile != null;
	}

	/**
	 * Send a SIP to the export directory
	 * 
	 * @param sipPackage
	 *            the SIP package
	 * @throws IOException
	 */
	public static void sendSIP(File sipPackage) throws IOException {
		FileUtils.moveFileToDirectory(sipPackage, exportFolder, true);
	}

	private static JDialog dialog = null;

	private static JDialog getInstructionsDialog(String resource) {
		if (dialog == null) {
			dialog = new JDialog(SIPCreator.getInstance().getMainFrame(),
					Messages.getString("OfflineSend.dialog.TITLE"), false);
			dialog.getContentPane().add(getInstructionsPanel(resource, dialog));
			dialog.setSize(500, 400);
			dialog.setLocationRelativeTo(null);
		}
		return dialog;
	}

	private static JPanel getInstructionsPanel(String resource, final JDialog parent) {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel title = new JLabel(Messages
				.getString("OfflineSend.instructions.TITLE"));
		String instructions;
		try {
			String instructionsTemplate = Tools
					.readInputStreamAsString(
							OfflineSendUtility.class
									.getResourceAsStream(resource),
							"UTF-8");
			instructions = String.format(instructionsTemplate, exportFolder
					.getPath());
		} catch (IOException e) {
			instructions = e.getMessage();
			logger.error("Error getting offline send instructions", e);
		}
		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		JLabel html = new JLabel(instructions);
		JButton closeButton = new JButton(Messages
				.getString("OfflineSend.instructions.CLOSE"));

		closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parent.setVisible(false);
			}

		});

		center.add(html);
		center.add(Box.createVerticalGlue());

		panel.add(title, BorderLayout.NORTH);
		panel.add(center, BorderLayout.CENTER);
		panel.add(closeButton, BorderLayout.SOUTH);

		title.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		title.setFont(new Font("Helvetica", Font.BOLD, 12));
		center.setBorder(BorderFactory.createEtchedBorder());
		html.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		closeButton.setMargin(new Insets(5, 100, 5, 100));

		return panel;
	}

	/**
	 * Show offine instructions for sending SIPs
	 * @param resource 
	 * the HTML file resource path
	 */
	public static void showInstructions(String resource) {
		getInstructionsDialog(resource).setVisible(true);
	}

}
