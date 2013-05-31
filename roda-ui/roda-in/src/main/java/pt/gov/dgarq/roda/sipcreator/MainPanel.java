package pt.gov.dgarq.roda.sipcreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCMetadataException;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;
import pt.gov.dgarq.roda.sipcreator.FondsPanel.FondsListener;
import pt.gov.dgarq.roda.sipcreator.RodaClientFactory.RodaClientFactoryListener;
import pt.gov.dgarq.roda.sipcreator.upload.SIPUpload;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class MainPanel extends JPanel {
	private static final long serialVersionUID = 3214346589346223770L;

	private static final Logger logger = Logger.getLogger(MainPanel.class);

	private JLabel banner = null;

	private JToolBar fondsTopToolbar = null;
	private Action actionUpdateFonds = null;
	private Action actionUploadSIPs = null;

	private JToolBar fondsBottomToolbar = null;
	private Action actionCreateSIP = null;
	private Action actionRemoveSIP = null;

	private FondsPanel panelFonds = null;
	private JPanel navigationPanel = null;

	private JSplitPane splitPaneMain = null;

	private JPanel deckPanel = null;

	private JPanel initPanel = null;
	private JPanel panelStatus = null;
	private JLabel statusLabel = null;

	/**
	 * Constructs a new {@link MainPanel}.
	 * 
	 */
	public MainPanel() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		add(getBanner(), BorderLayout.NORTH);
		add(getMainSplitPane(), BorderLayout.CENTER);
		add(getStatusPanel(), BorderLayout.SOUTH);
		updateVisibles();
		setPreferredSize(new Dimension(950, 800));
	}

	private JLabel getBanner() {
		if (banner == null) {
			banner = new JLabel();
			banner.setIcon(Tools.createImageIcon("banner_roda.png"));
			banner.setBackground(Color.BLACK);
			banner.setOpaque(true);
		}
		return banner;
	}

	private JPanel getStatusPanel() {
		if (this.panelStatus == null) {
			this.panelStatus = new JPanel();
			this.panelStatus.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));
			this.panelStatus.add(getStatusLabel());
		}
		return this.panelStatus;
	}

	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			statusLabel = new JLabel();
			setDefaultStatusMessage();
		}
		return this.statusLabel;
	}

	/**
	 * Set current status message
	 * 
	 * @param message
	 */
	public void setStatusMessage(String message) {
		getStatusLabel().setText(message);
	}

	/**
	 * Set default status message
	 */
	public void setDefaultStatusMessage() {
		getStatusLabel()
				.setText(Messages.getString("MainPanel.status.DEFAULT"));
	}

	private JToolBar getFondsTopToolBar() {
		if (this.fondsTopToolbar == null) {
			this.fondsTopToolbar = new JToolBar();
			this.fondsTopToolbar.setFloatable(false);

			this.fondsTopToolbar.add(new JButton(getUpdateFondsAction()));
			this.fondsTopToolbar.add(new JButton(getUploadSIPsAction()));
		}
		return this.fondsTopToolbar;
	}

	private JToolBar getFondsBottomToolBar() {
		if (this.fondsBottomToolbar == null) {
			this.fondsBottomToolbar = new JToolBar();
			this.fondsBottomToolbar.setFloatable(false);

			this.fondsBottomToolbar.add(new JButton(getCreateSIPAction()));
			this.fondsBottomToolbar.add(new JButton(getRemoveSIPAction()));

		}
		return this.fondsBottomToolbar;
	}

	private JPanel getNavigationalPanel() {
		if (navigationPanel == null) {
			navigationPanel = new JPanel(new BorderLayout());
			navigationPanel.add(getFondsTopToolBar(), BorderLayout.NORTH);
			navigationPanel.add(getFondsPanel(), BorderLayout.CENTER);
			navigationPanel.add(getFondsBottomToolBar(), BorderLayout.SOUTH);
			navigationPanel.setMinimumSize(new Dimension(250, 500));
		}
		return navigationPanel;
	}

	private Action getUpdateFondsAction() {
		if (this.actionUpdateFonds == null) {
			this.actionUpdateFonds = new AbstractAction(Messages
					.getString("MainPanel.action.UPDATE"), Tools
					.createImageIcon("update.png")) {
				private static final long serialVersionUID = 8394556002896257752L;

				public void actionPerformed(ActionEvent e) {
					updateClassificationPlan();
				}
			};
		}
		return this.actionUpdateFonds;
	}

	/**
	 * Update classification plan
	 */
	public void updateClassificationPlan() {
		RodaClientFactory.getInstance().getRodaClient(
				new RodaClientFactoryListener() {

					public void onCancel() {
						// nothing to do
					}

					public void onLogin(final RODAClient rodaClient) {
						String loadingMessage = Messages
								.getString("MainPanel.status.UPDATING_CLASSIFICATION_PLAN");
						Loading.run(loadingMessage, new Runnable() {

							public void run() {
								update(rodaClient);
							}

						});

					}

				});
	}

	private void update(RODAClient rodaClient) {
		try {
			MyClassificationPlanHelper.getInstance().update(rodaClient);
			getFondsPanel().reload();
		} catch (BrowserException e1) {
			JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(),
					Messages.getString("common.ERROR"),
					JOptionPane.ERROR_MESSAGE);
			logger.error("Error updating classification plan", e1);
		} catch (RODAClientException e1) {
			JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(),
					Messages.getString("common.ERROR"),
					JOptionPane.ERROR_MESSAGE);
			logger.error("Error updating classification plan", e1);
		} catch (NoSuchRODAObjectException e1) {
			JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(),
					Messages.getString("common.ERROR"),
					JOptionPane.ERROR_MESSAGE);
			logger.error("Error updating classification plan", e1);
		} catch (InvalidDescriptionObjectException e1) {
			JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(),
					Messages.getString("common.ERROR"),
					JOptionPane.ERROR_MESSAGE);
			logger.error("Error updating classification plan", e1);
		} catch (MetadataException e1) {
			JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(),
					Messages.getString("common.ERROR"),
					JOptionPane.ERROR_MESSAGE);
			logger.error("Error updating classification plan", e1);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(),
					Messages.getString("common.ERROR"),
					JOptionPane.ERROR_MESSAGE);
			logger.error("Error updating classification plan", e1);
		} catch (EadCMetadataException e1) {
			JOptionPane.showMessageDialog(MainPanel.this, e1.getMessage(),
					Messages.getString("common.ERROR"),
					JOptionPane.ERROR_MESSAGE);
			logger.error("Error updating classification plan", e1);
		}
	}

	private Action getCreateSIPAction() {
		if (this.actionCreateSIP == null) {
			this.actionCreateSIP = new AbstractAction(Messages
					.getString("MainPanel.action.CREATE_SIP"), Tools
					.createImageIcon("sip_add.png")) {
				private static final long serialVersionUID = 7591747627055871224L;

				public void actionPerformed(ActionEvent e) {
					try {
						getFondsPanel().createNewSip();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(MainPanel.this,

						Messages.getString("MainPanel.error.CREATE_SIP", e1
								.getMessage()), Messages
								.getString("common.ERROR"),
								JOptionPane.ERROR_MESSAGE);
					}

				}
			};
		}
		return this.actionCreateSIP;
	}

	private Action getRemoveSIPAction() {
		if (this.actionRemoveSIP == null) {
			this.actionRemoveSIP = new AbstractAction(Messages
					.getString("MainPanel.action.REMOVE_SIP"), Tools
					.createImageIcon("sip_delete.png")) {
				private static final long serialVersionUID = 805661207962888054L;

				public void actionPerformed(ActionEvent e) {
					getFondsPanel().removeSips();
				}
			};
		}
		return this.actionRemoveSIP;
	}

	private Action getUploadSIPsAction() {
		if (this.actionUploadSIPs == null) {
			this.actionUploadSIPs = new AbstractAction(Messages
					.getString("MainPanel.action.INGEST"), Tools
					.createImageIcon("commit.png")) {
				private static final long serialVersionUID = 6795263394474408784L;

				public void actionPerformed(ActionEvent e) {
					new SIPUpload().show();
				}
			};
		}
		return this.actionUploadSIPs;
	}

	private JSplitPane getMainSplitPane() {
		if (splitPaneMain == null) {
			splitPaneMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPaneMain.add(getNavigationalPanel());
			splitPaneMain.add(getDeckPanel());
			splitPaneMain.setDividerLocation(250);
			splitPaneMain.setOneTouchExpandable(true);
			setDeckComponent(getInitPanel());
		}
		return splitPaneMain;
	}

	/**
	 * Get fonds panel
	 * 
	 * @return the fonds panel
	 */
	public FondsPanel getFondsPanel() {
		if (panelFonds == null) {
			panelFonds = new FondsPanel();
			panelFonds.addFondsListener(new FondsListener() {

				public void onClassificationPlanSelect(DescriptionObject obj) {
					DOPanel doPanel = new DOPanel(obj);
					setDeckComponent(doPanel);
					updateVisibles();
				}

				public void onSipSelect(SIP sip) {
					SIPPanel sipPanel = new SIPPanel(sip);
					setDeckComponent(sipPanel);
					updateVisibles();
				}

				public void onMultipleSelect(List<Object> selected) {
					JPanel panel = new JPanel();
					panel.setLayout(new BorderLayout());
					JLabel title = new JLabel(
							String
									.format(
											"<html><p style=\"color: #d1d2d3; font-weight: 900; font-size: 26pt\">%1$s</p><html>",
											Messages
													.getString("MainPanel.MULTIPLE_METADATA_EDIT")));

					title.setHorizontalAlignment(JLabel.CENTER);
					panel.add(title, BorderLayout.CENTER);
					panel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
					setDeckComponent(panel);
					updateVisibles();
				}

				public void onNoneSelected() {
					setDeckComponent(getInitPanel());
					updateVisibles();
				}

				public void onSipDOSelect(SIPDescriptionObject sipdo, SIP sip) {
					logger.debug("On SIP DO selected " + sipdo);
					boolean refresh = true;
					if (currentDeckComponent instanceof SIPDOPanel) {
						SIPDOPanel sipDoPanel = (SIPDOPanel) currentDeckComponent;
						if (sipDoPanel.getSipDescriptionObject() == sipdo) {
							logger
									.debug("Found the same SIP DO inside current deck component "
											+ sipDoPanel
													.getSipDescriptionObject());

							refresh = false;
						}
					}

					if (refresh) {
						SIPDOPanel sipDOPanel = new SIPDOPanel(sipdo, sip);
						setDeckComponent(sipDOPanel);
						updateVisibles();
					}
				}

			});
		}
		return panelFonds;
	}

	private JPanel getDeckPanel() {
		if (deckPanel == null) {
			deckPanel = new JPanel(new BorderLayout());
		}
		return deckPanel;
	}

	private Component currentDeckComponent = null;

	private void setDeckComponent(Component component) {
		this.currentDeckComponent = component;
		getDeckPanel().removeAll();
		getDeckPanel().add(component, BorderLayout.CENTER);
		getDeckPanel().repaint();
		getDeckPanel().revalidate();
	}

	private JPanel getInitPanel() {
		if (initPanel == null) {
			initPanel = new JPanel(new BorderLayout());
			JLabel label = new JLabel(
					String
							.format(
									"<html><p style=\"color: #d1d2d3; font-weight: 900; font-size: 26pt\">%1$s</p><html>",
									Messages.getString("MainPanel.WELCOME")));
			label.setHorizontalAlignment(JLabel.CENTER);
			initPanel.add(label, BorderLayout.CENTER);
		}
		return initPanel;
	}

	protected void updateVisibles() {
		getCreateSIPAction().setEnabled(getFondsPanel().canCreateNewSip());
		getRemoveSIPAction().setEnabled(getFondsPanel().canRemoveSips());
	}
}
