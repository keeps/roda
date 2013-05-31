package pt.gov.dgarq.roda.sipcreator.upload;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import pt.gov.dgarq.roda.sipcreator.Messages;
import pt.gov.dgarq.roda.sipcreator.SIPCreator;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class SIPUpload {

	private static final long serialVersionUID = -1352133016399305981L;

	private JDialog dialog = null;

	private JPanel layout = null;

	private SIPListPanel sipList = null;

	private JPanel panelControl = null;

	private JPanel sendMode = null;
	private JRadioButton httpOption = null;
	private JRadioButton ftpOption = null;
	private JRadioButton mailOption = null;

	private JPanel buttons = null;

	/**
	 * Constructs a new {@link SIPUpload}.
	 */
	public SIPUpload() {
	}

	/**
	 * Show SIP Upload Dialog
	 */
	public void show() {
		getDialog().setVisible(true);
	}

	/**
	 * Hide SIP Upload Dialog
	 */
	public void hide() {
		getDialog().setVisible(false);
	}

	private JDialog getDialog() {
		if (dialog == null) {
			dialog = new JDialog(SIPCreator.getInstance().getMainFrame(),
					Messages.getString("SIPUpload.TITLE"), true);
			dialog.setLayout(new BorderLayout());
			dialog.add(getLayout(), BorderLayout.CENTER);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
		}
		return dialog;
	}

	private JPanel getLayout() {
		if (layout == null) {
			layout = new JPanel(new BorderLayout());
			layout.setBorder(new EmptyBorder(10, 10, 10, 10));

			layout.add(new JLabel(String.format("<html><h3>%1$s</h3></html>",
					Messages.getString("SIPUpload.CHOOSE_SIPS"))),
					BorderLayout.NORTH);
			layout.add(getSIPList().getComponent(), BorderLayout.CENTER);
			layout.add(getControlPanel(), BorderLayout.SOUTH);

		}
		return layout;
	}

	private SIPListPanel getSIPList() {
		if (this.sipList == null) {
			sipList = new SIPListPanel();
		}
		return sipList;
	}

	private JPanel getControlPanel() {
		if (this.panelControl == null) {
			panelControl = new JPanel();
			panelControl.setLayout(new BoxLayout(this.panelControl,
					BoxLayout.Y_AXIS));

			panelControl.add(getSendMode());
			panelControl.add(getButtons());
			buttons.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		}
		return panelControl;
	}

	private JPanel getSendMode() {
		if (sendMode == null) {
			sendMode = new JPanel(new BorderLayout());
			JLabel sendModeTitle = new JLabel(String.format(
					"<html><h3>%1$s</h3></html>", Messages
							.getString("SIPUpload.CHOOSE_SEND_MEANS")));
			JPanel optionsPanel = new JPanel();
			optionsPanel
					.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

			ButtonGroup group = new ButtonGroup();
			group.add(getHTTPOption());
			group.add(getFTPOption());
			group.add(getMailOption());

			getHTTPOption().setSelected(true);

			optionsPanel.add(getHTTPOption());
			optionsPanel.add(getFTPOption());
			optionsPanel.add(getMailOption());

			sendMode.add(sendModeTitle, BorderLayout.NORTH);
			sendMode.add(optionsPanel, BorderLayout.CENTER);

			optionsPanel
					.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		}
		return sendMode;
	}

	private JRadioButton getHTTPOption() {
		if (httpOption == null) {
			httpOption = new JRadioButton(Messages
					.getString("SIPUpload.send_means.HTTP"));
		}
		return httpOption;
	}

	private JRadioButton getFTPOption() {
		if (ftpOption == null) {
			ftpOption = new JRadioButton(Messages
					.getString("SIPUpload.send_means.FTP"));
		}
		return ftpOption;
	}

	private JRadioButton getMailOption() {
		if (mailOption == null) {
			mailOption = new JRadioButton(Messages
					.getString("SIPUpload.send_means.POSTAL_MAIL"));
		}
		return mailOption;
	}

	private JPanel getButtons() {
		if (buttons == null) {
			buttons = new JPanel();
			buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
			JButton cancel = new JButton(Messages.getString("SIPUpload.CANCEL"));
			JButton send = new JButton(Messages.getString("SIPUpload.SEND"));

			cancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					hide();
				}

			});

			send.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (sipList.getCheckedSIPList().size() == 0) {
						return;
					}
					hide();
					SendSIPList.Type type = null;
					if (getHTTPOption().isSelected()) {
						type = SendSIPList.Type.HTTP;
					} else if (getFTPOption().isSelected()) {
						type = SendSIPList.Type.FTP;
					} else if (getMailOption().isSelected()) {
						type = SendSIPList.Type.POSTAL_MAIL;
					}
					if (type != null) {
						SendSIPList sendSIP = new SendSIPList();
						sendSIP.start(sipList.getCheckedSIPList(), type);
					}

				}

			});

			buttons.add(cancel);
			buttons.add(Box.createHorizontalGlue());
			buttons.add(send);

			buttons.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		}
		return buttons;
	}

}
