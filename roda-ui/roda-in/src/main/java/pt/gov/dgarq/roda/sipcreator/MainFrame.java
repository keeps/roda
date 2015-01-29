package pt.gov.dgarq.roda.sipcreator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.configuration.PropertiesConfiguration;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelManager;
import pt.gov.dgarq.roda.sipcreator.SIPCreatorConfig.UpdateInterface;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class MainFrame extends JFrame {
	private static final long serialVersionUID = -1380478100836821695L;

	private MainPanel sipCreatorMainPanel = null;
	private Popup splash = null;

	/**
	 * Constructs a new {@link MainFrame}.
	 */
	public MainFrame() {
		getSplash().show();
		configureUI();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowListener() {

			public void windowActivated(WindowEvent e) {
				// nothing to do
			}

			public void windowClosed(WindowEvent e) {
				// nothing to do
			}

			public void windowClosing(WindowEvent e) {
				if (getMainPanel().getFondsPanel().isAnySIPChanged()) {
					int option = JOptionPane
							.showConfirmDialog(
									MainFrame.this,
									Messages.getString("MainFrame.warning.EXIT_CONFIRM_SAVE_ALL"),
									Messages.getString("common.WARNING"),
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.WARNING_MESSAGE);
					if (option == JOptionPane.YES_OPTION) {
						getMainPanel().getFondsPanel().saveAllSIP();
						System.exit(0);
					} else if (option == JOptionPane.NO_OPTION) {
						System.exit(0);
					} else {
						// do nothing
					}

				} else {
					System.exit(0);
				}
			}

			public void windowDeactivated(WindowEvent e) {
				// nothing to do
			}

			public void windowDeiconified(WindowEvent e) {
				// nothing to do

			}

			public void windowIconified(WindowEvent e) {
				// nothing to do
			}

			public void windowOpened(WindowEvent e) {
				// nothing to do

			}

		});
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				initComponents();
				getSplash().hide();
				SIPCreatorConfig.getInstance().checkForUpdate(
						new UpdateInterface() {

							public void onOutdated(String newVersion,
									String oldVersion) {
								JOptionPane
										.showMessageDialog(
												MainFrame.this,
												Messages.getString(
														"MainFrame.OUTDATED",
														newVersion,
														SIPCreatorConfig
																.getInstance()
																.getUpdateDownloadUrl()),
												Messages.getString("common.WARNING"),
												JOptionPane.WARNING_MESSAGE);
							}

							public void onUpdated(String version) {
								// nothing to do
							}

						});
			}

		});

	}

	private void configureUI() {
		String base = "/pt/gov/dgarq/roda/sipcreator/";
		UIManager.put("Tree.expandedIcon",
				Tools.createImageIcon(base + "tree_collapse.gif"));
		UIManager.put("Tree.collapsedIcon",
				Tools.createImageIcon(base + "tree_expand.gif"));
		setIconImage(Tools.createImageIcon("roda-logo.png").getImage());
	}

	private void initComponents() {
		// FIXME see if this is the best place to initialize the description
		// levels
		try {
			Properties descriptionLevels = new Properties();

			PropertiesConfiguration properties = new PropertiesConfiguration(
					"roda-description-levels-hierarchy.properties");

			descriptionLevels.load(new FileInputStream(properties.getFile()));
			new DescriptionLevelManager(descriptionLevels);

		} catch (Exception e) {
			System.err.println(e);
		}

		setTitle(Messages.getString("MainFrame.TITLE", SIPCreatorConfig
				.getInstance().getVersion()));
		setLayout(new BorderLayout());
		add(getMainPanel(), BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	/**
	 * Get main panel
	 * 
	 * @return the main panel
	 */
	public MainPanel getMainPanel() {
		if (this.sipCreatorMainPanel == null) {
			this.sipCreatorMainPanel = new MainPanel();
		}
		return this.sipCreatorMainPanel;
	}

	private Popup getSplash() {
		if (splash == null) {
			JLabel image = new JLabel();
			ImageIcon icon = Tools.createImageIcon("/splash.png");
			image.setIcon(icon);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int top = (int) (screenSize.height / 2.0 - icon.getIconHeight() / 2.0);
			int left = (int) (screenSize.width / 2.0 - icon.getIconWidth() / 2.0);
			splash = PopupFactory.getSharedInstance().getPopup(this, image,
					left, top);
		}
		return splash;
	}
}
