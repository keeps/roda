/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Luis Faria
 * 
 */
public class RodaClientFactory {
	private static final Logger logger = Logger
			.getLogger(RodaClientFactory.class);

	private static RodaClientFactory instance = null;

	/**
	 * Get RODA Client factory instance
	 * 
	 * @return the singleton
	 */
	public static RodaClientFactory getInstance() {
		if (instance == null) {
			instance = new RodaClientFactory();
		}
		return instance;
	}

	/**
	 * RODA Client Factory listener
	 * 
	 * @author Luis Faria
	 * 
	 */
	public interface RodaClientFactoryListener {
		/**
		 * On RODA Client login
		 * 
		 * @param rodaClient
		 */
		public void onLogin(RODAClient rodaClient);

		/**
		 * On login cancel
		 */
		public void onCancel();
	}

	private JDialog dialog = null;
	private JPanel layout = null;
	private JLabel image = null;
	private JPanel formLayout = null;
	private JLabel usernameLabel = null;
	private JLabel passwordLabel = null;
	private JTextField usernameField = null;
	private JPasswordField passwordField = null;

	private JPanel buttonPanel = null;
	private JButton loginButton = null;
	private JButton cancelButton = null;

	private RODAClient rodaClient = null;

	private RodaClientFactory() {

	}

	private RodaClientFactoryListener listener;

	/**
	 * Get RODA Client
	 * 
	 * @param listener
	 * 
	 */
	public void getRodaClient(RodaClientFactoryListener listener) {
		if (rodaClient == null) {
			this.listener = listener;
			logger.debug("Showing login panel");
			show();
		} else {
			listener.onLogin(rodaClient);
		}

	}

	protected JDialog getDialog() {
		if (dialog == null) {
			dialog = new JDialog(SIPCreator.getInstance().getMainFrame(),
					Messages.getString("Login.TITLE"), true);
			dialog.setLayout(new BorderLayout());
			dialog.add(getLayout(), BorderLayout.CENTER);
			dialog.pack();
			dialog.setResizable(false);
			dialog.setLocationRelativeTo(null);
		}
		return dialog;
	}

	private JLabel getImage() {
		if (image == null) {
			image = new JLabel();
			image.setIcon(Tools.createImageIcon("authentication.png"));
			image.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		}
		return image;
	}

	private Component getLayout() {
		if (layout == null) {
			layout = new JPanel(new BorderLayout());
			layout.add(getImage(), BorderLayout.WEST);
			layout.add(getFormLayout(), BorderLayout.CENTER);
			layout.add(getButtonPanel(), BorderLayout.SOUTH);
			layout.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		return layout;
	}

	private JPanel getFormLayout() {
		if (formLayout == null) {
			formLayout = new JPanel(new SpringLayout());
			formLayout.add(getUsernameLabel());
			formLayout.add(getUsernameField());
			formLayout.add(getPasswordLabel());
			formLayout.add(getPasswordField());

			SpringUtilities.makeCompactGrid(formLayout, 2, 2, 5, 5, 5, 5);

		}
		return formLayout;
	}

	private JPasswordField getPasswordField() {
		if (passwordField == null) {
			passwordField = new JPasswordField(15);
			passwordField.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					login();
				}

			});
		}
		return passwordField;
	}

	private Component getPasswordLabel() {
		if (passwordLabel == null) {
			passwordLabel = new JLabel(Messages.getString("Login.PASSWORD"),
					JLabel.TRAILING);
		}
		return passwordLabel;
	}

	private Component getUsernameField() {
		if (usernameField == null) {
			usernameField = new JTextField(15);
		}
		return usernameField;
	}

	private Component getUsernameLabel() {
		if (usernameLabel == null) {
			usernameLabel = new JLabel(Messages.getString("Login.USERNAME"),
					JLabel.TRAILING);
		}
		return usernameLabel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.add(getCancelButton());
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(getLoginButton());
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		}
		return buttonPanel;
	}

	private JButton getLoginButton() {
		if (loginButton == null) {
			loginButton = new JButton(Messages.getString("Login.action.LOGIN"));
			loginButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					login();

				}

			});
		}
		return loginButton;
	}

	private void login() {
		hide();
		rodaClient = null;
		Loading.run(Messages.getString("Login.LOGIN_IN"), new Runnable() {

			public void run() {
				try {
					CASUtility casUtility = new CASUtility(SIPCreatorConfig
							.getInstance().getCasURL(), SIPCreatorConfig
							.getInstance().getRODACoreServices());
					rodaClient = new RODAClient(SIPCreatorConfig.getInstance()
							.getRODACoreServices(), usernameField.getText(),
							new String(passwordField.getPassword()), casUtility);

				} catch (LoginException e1) {
					JOptionPane.showMessageDialog(getDialog(),
							Messages.getString("Login.error.LOGIN_EXCEPTION"),
							Messages.getString("common.ERROR"),
							JOptionPane.ERROR_MESSAGE);
				} catch (RODAClientException e1) {
					JOptionPane.showMessageDialog(getDialog(), Messages
							.getString("Login.error.RODA_CLIENT_EXCEPTION",
									e1.getMessage()), Messages
							.getString("common.ERROR"),
							JOptionPane.ERROR_MESSAGE);
				} catch (MalformedURLException e1) {
					JOptionPane.showMessageDialog(getDialog(), Messages
							.getString("Login.error.RODA_CLIENT_EXCEPTION",
									e1.getMessage()), Messages
							.getString("common.ERROR"),
							JOptionPane.ERROR_MESSAGE);
				} catch (Throwable e1) {
					JOptionPane.showMessageDialog(getDialog(), Messages
							.getString("Login.error.RODA_CLIENT_EXCEPTION",
									e1.getMessage()), Messages
							.getString("common.ERROR"),
							JOptionPane.ERROR_MESSAGE);
				}
			}

		});

		if (rodaClient == null) {
			show();
		} else if (listener != null) {
			listener.onLogin(rodaClient);
			listener = null;
		}

	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(
					Messages.getString("Login.action.CANCEL"));
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					hide();
					if (listener != null) {
						listener.onCancel();
						listener = null;
					}
				}

			});
		}
		return cancelButton;
	}

	protected void show() {
		getDialog().setVisible(true);
	}

	protected void hide() {
		getDialog().setVisible(false);
	}

}
