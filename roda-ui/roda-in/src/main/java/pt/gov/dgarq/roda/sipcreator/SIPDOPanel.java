package pt.gov.dgarq.roda.sipcreator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangedEvent;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.sipcreator.SIPActionHelper.SaveSipActionListener;
import pt.gov.dgarq.roda.sipcreator.description.DescriptionPanel;
import pt.gov.dgarq.roda.sipcreator.representation.RepresentationPanel;
import pt.gov.dgarq.roda.sipcreator.representation.RepresentationTypeSelectionDialog;
import pt.gov.dgarq.roda.sipcreator.representation.RepresentationTypeSelectionPanel.RepresentationTypeSelectionListener;

/**
 * SIP Description Object Panel
 * 
 * @author Luis Faria
 * 
 */
public class SIPDOPanel extends JPanel implements
		RepresentationTypeSelectionListener {
	private static final long serialVersionUID = 7328648868440369036L;
	private static final Logger logger = Logger.getLogger(SIPDOPanel.class);

	// data
	private final SIPDescriptionObject sipdo;
	private final SIP sip;

	// UI
	private JPanel panelHeader = null;
	private JLabel labelHeader = null;

	private JTabbedPane tabbedPane = null;
	private DescriptionPanel panelDescription = null;
	private List<RepresentationPanel> panelRepresentations = null;

	private JToolBar toolBar = null;

	private Action actionSaveSip = null;
	private Action actionValidateSip = null;

	private Action actionCreateDO = null;
	private Action actionRemoveDO = null;

	private Action actionCreateRep = null;
	private Action actionRemoveRep = null;

	private RepresentationTypeSelectionDialog repTypeSelectDialog = null;

	/**
	 * 
	 * @param sipdo
	 * @param sip
	 */
	public SIPDOPanel(SIPDescriptionObject sipdo, SIP sip) {
		this.sipdo = sipdo;
		this.sip = sip;

		sip.addChangeListener(new DataChangeListener() {

			public void dataChanged(DataChangedEvent evtDataChanged) {
				updatePanel();
			}

		});

		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		panelRepresentations = new ArrayList<RepresentationPanel>();

		add(getHeaderPanel(), BorderLayout.NORTH);
		add(getTabbedPane(), BorderLayout.CENTER);
		add(getToolBar(), BorderLayout.SOUTH);

		updatePanel();
		updateVisibles();
	}

	private JPanel getHeaderPanel() {
		if (this.panelHeader == null) {
			this.panelHeader = new JPanel();
			this.panelHeader.add(getHeaderLabel());
		}
		return this.panelHeader;
	}

	private JLabel getHeaderLabel() {
		if (this.labelHeader == null) {
			this.labelHeader = new JLabel();
			this.labelHeader.setBorder(BorderFactory.createEmptyBorder(5, 0, 0,
					0));
		}
		return this.labelHeader;
	}

	private JTabbedPane getTabbedPane() {
		if (this.tabbedPane == null) {
			this.tabbedPane = new JTabbedPane();
			this.tabbedPane.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					updateVisibles();
				}

			});

		}
		return this.tabbedPane;
	}

	private DescriptionPanel getDescriptionPanel() {
		if (this.panelDescription == null) {
			this.panelDescription = new DescriptionPanel();
		}
		return this.panelDescription;
	}

	private RepresentationPanel createRepresentationPanel(
			SIPRepresentationObject repObj) {
		RepresentationPanel repPanel = new RepresentationPanel(repObj);
		return repPanel;
	}

	private void updatePanel() {

		// title
		if (sipdo.getId() != null) {
			getHeaderLabel().setText(
					String.format(
							"<html><p style='font-size: 20'>%1$s</p></html>",
							MyClassificationPlanHelper.getInstance()
									.getCompleteReference(sipdo, sip)));

		} else {

			getHeaderLabel().setText(
					String.format(
							"<html><p style='font-size: 20'>%1$s</p></html>",
							Messages.getString("SIPPanel.NEW_SIPDO_TITLE")));

		}

		// description
		getDescriptionPanel().setDescriptionObject(sipdo);
		// getDescriptionPanel().setFileChild(sip.getDescriptionObject() !=
		// sipdo);

		// representations
		int repIndex = 0;
		int repPanelIndex = 0;
		while (repIndex < sipdo.getRepresentations().size()
				&& repPanelIndex < panelRepresentations.size()) {
			SIPRepresentationObject rep = sipdo.getRepresentations().get(
					repIndex);
			RepresentationPanel repPanel = panelRepresentations
					.get(repPanelIndex);
			if (repPanel.getRepresentationObject().equals(rep)) {
				repIndex++;
				repPanelIndex++;
			} else {
				panelRepresentations.remove(repPanelIndex);
			}
		}

		// Remove remaining
		while (repPanelIndex < panelRepresentations.size()) {
			panelRepresentations.remove(repPanelIndex);
		}

		// Add remaining
		while (repIndex < sipdo.getRepresentations().size()) {
			panelRepresentations.add(createRepresentationPanel(sipdo
					.getRepresentations().get(repIndex++)));
		}

		// tabs
		int tabIndex = this.tabbedPane.getSelectedIndex();

		// Add description panel if not added yet
		if (getDescriptionPanel().getParent() == null) {
			this.tabbedPane.addTab(Messages.getString("SIPPanel.DESCRIPTION"),
					getDescriptionPanel());
		}

		// Remove all tabs that are not in representation list
		for (int j = 1; j < tabbedPane.getTabCount(); j++) {
			Component component = tabbedPane.getComponentAt(j);
			if (!panelRepresentations.contains(component)) {
				tabbedPane.remove(component);
				logger.debug("Removed " + component + " bc not in "
						+ panelRepresentations);
			}
		}

		// Check if all representation panel are present
		for (RepresentationPanel repPanel : panelRepresentations) {
			if (repPanel.getParent() == null) {
				this.tabbedPane.addTab(Messages
						.getString("SIPPanel.REPRESENTATION"), repPanel);
				tabIndex = tabbedPane.indexOfComponent(repPanel);
			}
		}

		tabIndex = (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) ? tabIndex
				: tabbedPane.getTabCount() - 1;

		tabbedPane.setSelectedIndex(tabIndex);
		updateVisibles();
	}

	private JToolBar getToolBar() {
		if (this.toolBar == null) {
			this.toolBar = new JToolBar();
			this.toolBar.setFloatable(false);

			this.toolBar.add(new JButton(getSaveSIPAction()));
			this.toolBar.add(new JButton(getValidateSIPAction()));

			this.toolBar.addSeparator();

			this.toolBar.add(new JButton(getCreateDOAction()));
			this.toolBar.add(new JButton(getRemoveDOAction()));

			this.toolBar.addSeparator();

			this.toolBar.add(new JButton(getCreateRepAction()));
			this.toolBar.add(new JButton(getRemoveRepAction()));
		}
		return this.toolBar;
	}

	private Action getSaveSIPAction() {
		if (actionSaveSip == null) {
			actionSaveSip = SIPActionHelper.createSaveSipAction(this, sip,
					new SaveSipActionListener() {

						public void onBeforeSipSave() {
							int selectedIndex = tabbedPane.getSelectedIndex();
							for (int i = 0; i < panelRepresentations.size(); i++) {
								RepresentationPanel repPanel = panelRepresentations
										.get(i);
								if (i == selectedIndex - 1) {
									repPanel.stopPreview();
								}
								logger
										.debug("Saving representation panel "
												+ i);
								repPanel.save();
							}

						}

						public void onAfterSipSave() {
							int selectedIndex = tabbedPane.getSelectedIndex();
							if (selectedIndex > 0) {
								panelRepresentations.get(selectedIndex - 1)
										.startPreview();
							}
						}

					});
		}
		return actionSaveSip;
	}

	private Action getValidateSIPAction() {
		if (actionValidateSip == null) {
			actionValidateSip = SIPActionHelper.createIngestValidateAction(
					this, sip);
		}
		return actionValidateSip;
	}

	private Action getCreateDOAction() {
		if (actionCreateDO == null) {
			actionCreateDO = new AbstractAction(Messages
					.getString("SIPPanel.action.CREATE_SIMPLE_DOCUMENT"), Tools
					.createImageIcon("item_add.png")) {
				private static final long serialVersionUID = -1474785370376804549L;

				public void actionPerformed(ActionEvent e) {
					SIPCreator.getInstance().getMainFrame().getMainPanel()
							.getFondsPanel().createNewSipDO(sipdo);
				}
			};
		}
		return actionCreateDO;
	}

	private Action getRemoveDOAction() {
		if (actionRemoveDO == null) {
			actionRemoveDO = new AbstractAction(Messages
					.getString("SIPPanel.action.REMOVE_SIMPLE_DOCUMENT"), Tools
					.createImageIcon("item_remove.png")) {
				private static final long serialVersionUID = -1474785370376804549L;

				public void actionPerformed(ActionEvent e) {
					SIPCreator.getInstance().getMainFrame().getMainPanel()
							.getFondsPanel().removeSipDO(sipdo, sip);
				}
			};
		}
		return actionRemoveDO;
	}

	private Action getCreateRepAction() {
		if (this.actionCreateRep == null) {
			this.actionCreateRep = new AbstractAction(Messages
					.getString("SIPPanel.action.CREATE_REPRESENTATION"), Tools
					.createImageIcon("representation_add.png")) {
				private static final long serialVersionUID = -1474785370376804549L;

				public void actionPerformed(ActionEvent e) {
					getRepTypeSelectionDialog().setVisible(true);
				}
			};

		}
		return this.actionCreateRep;
	}

	private Action getRemoveRepAction() {
		if (this.actionRemoveRep == null) {
			this.actionRemoveRep = new AbstractAction(Messages
					.getString("SIPPanel.action.REMOVE_REPRESENTATION"), Tools
					.createImageIcon("representation_remove.png")) {
				private static final long serialVersionUID = -1474785370376804549L;

				public void actionPerformed(ActionEvent e) {
					Component component = getTabbedPane()
							.getSelectedComponent();
					if (component instanceof RepresentationPanel) {
						RepresentationPanel repPanel = (RepresentationPanel) component;
						SIPRepresentationObject repObj = repPanel
								.getRepresentationObject();
						sipdo.removeRepresentation(repObj);
					}

				}
			};

		}
		return this.actionRemoveRep;
	}

	private RepresentationTypeSelectionDialog getRepTypeSelectionDialog() {
		if (repTypeSelectDialog == null) {
			repTypeSelectDialog = new RepresentationTypeSelectionDialog(
					SIPCreator.getInstance().getMainFrame());
			repTypeSelectDialog.addRepresentationTypeSelectionListener(this);
		}
		return repTypeSelectDialog;
	}

	protected void updateVisibles() {
		// SIP actions
		// getSaveSIPAction().setEnabled(sip.isSaveValid());
		getSaveSIPAction().setEnabled(true);

		// Documents
		actionCreateDO.setEnabled(sipdo.getLevel()
				.equals(DescriptionLevel.FILE)
				&& sipdo.getRepresentations().size() == 0);

		actionRemoveDO.setEnabled(!sip.getDescriptionObject().equals(sipdo));

		// Representations
		actionCreateRep.setEnabled(sipdo.getChildren().size() == 0);
		actionRemoveRep.setEnabled(tabbedPane.getSelectedIndex() > 0);

	}

	/**
	 * Get SIP Description Object
	 * 
	 * @return the SIP description object
	 */
	public SIPDescriptionObject getSipDescriptionObject() {
		return sipdo;
	}

	/**
	 * Get SIP
	 * 
	 * @return the SIP
	 */
	public SIP getSip() {
		return sip;
	}

	/**
	 * Event called when a representation type is selected. This event is
	 * handled by creating a new representation of with the declared type and
	 * sub type.
	 * 
	 * @param type
	 * @param subtype
	 */
	public void onRepresentationTypeSelected(String type, String subtype) {
		SIPRepresentationObject repObj = new SIPRepresentationObject();
		repObj.setId(RepresentationBuilder.createNewRepresentationID());
		repObj
				.setStatuses(new String[] { RepresentationObject.STATUS_ORIGINAL });
		repObj.setType(type);
		repObj.setSubType(subtype);
		sipdo.addRepresentation(repObj);
	}

}
