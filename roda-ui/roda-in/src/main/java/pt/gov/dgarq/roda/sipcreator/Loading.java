/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

/**
 * @author Luis Faria
 * 
 */
public class Loading {
	
	private static final Logger logger = Logger
	.getLogger(Loading.class);

	private static Loading instance = null;

	/**
	 * Get the singleton
	 * 
	 * @return singleton instance
	 */
	public static Loading getInstance() {
		if (instance == null) {
			instance = new Loading();
		}
		return instance;
	}

	private JDialog popup = null;
	private JPanel layout = null;
	private JProgressBar progressBar = null;
	private JLabel messageLabel = null;

	private Loading() {

	}

	/**
	 * Run code while showing loading panel
	 * 
	 * @param message
	 * @param runable
	 */
	public static void run(String message, Runnable runable) {
		getInstance().run_impl(message, runable);
	}

	private synchronized void run_impl(String message, final Runnable runable) {
		final Thread thread = new Thread() {

			public void run() {
				runable.run();
				logger.debug("Run finished");
				hide();
				logger.debug("hide finished");
			}

		};
		getPopup().addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {

			}

			public void componentMoved(ComponentEvent e) {

			}

			public void componentResized(ComponentEvent e) {

			}

			public void componentShown(ComponentEvent e) {
				getPopup().removeComponentListener(this);
				thread.start();
			}

		});
		show(message);

	}

	/**
	 * Show loading popup with custom message
	 * 
	 * @param message
	 */
	private void show(String message) {
		SIPCreator.getInstance().getMainFrame().getMainPanel().setCursor(
				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		getMessageLabel().setText(message);
		getPopup().setVisible(true);

	}

	/**
	 * Hide loading popup
	 */
	private void hide() {
		getPopup().setVisible(false);
		SIPCreator.getInstance().getMainFrame().getMainPanel().setCursor(
				Cursor.getDefaultCursor());
	}

	/**
	 * Set loading message
	 * 
	 * @param message
	 */
	public static void setMessage(String message) {
		getInstance().getMessageLabel().setText(message);
	}

	private JDialog getPopup() {
		if (popup == null) {
			popup = new JDialog(SIPCreator.getInstance().getMainFrame(),
					Messages.getString("Loading.TITLE"), true);
			popup.setLayout(new BorderLayout());
			popup.add(getLayout(), BorderLayout.CENTER);
			popup.pack();
			popup.setLocationRelativeTo(null);
			popup.setResizable(false);
			popup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
		return popup;
	}

	private JPanel getLayout() {
		if (layout == null) {
			layout = new JPanel(new BorderLayout());
			layout.add(getProgressBar(), BorderLayout.NORTH);
			layout.add(getMessageLabel(), BorderLayout.CENTER);
			layout.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
			layout.setPreferredSize(new Dimension(300, 70));
		}
		return layout;
	}

	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
		}
		return progressBar;
	}

	private JLabel getMessageLabel() {
		if (messageLabel == null) {
			messageLabel = new JLabel(getDefaultMessage());
			messageLabel
					.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		}
		return messageLabel;
	}

	private String getDefaultMessage() {
		return Messages.getString("Loading.DEFAULT_MESSAGE");
	}

}
