package pt.gov.dgarq.roda.sipcreator;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;

/**
 * 
 * @author Rui Castro
 * @author Luis Faria
 * 
 */
public class FondsPanel extends JPanel {
	private static final long serialVersionUID = 7117882288249167496L;

	// private static final Logger logger = Logger.getLogger(FondsPanel.class);

	private FondsTree treeFonds = null;
	private FondsTreeModel treeModelFonds = null;
	private List<FondsListener> listeners;

	/**
	 * Constructs a new {@link FondsPanel}.
	 */
	public FondsPanel() {
		initComponents();
		listeners = new ArrayList<FondsListener>();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(getFondsTree());
		add(scrollPane, BorderLayout.CENTER);
	}

	private FondsTree getFondsTree() {
		if (this.treeFonds == null) {
			this.treeFonds = new FondsTree(getFondsTreeModel());
			this.treeFonds
					.addTreeSelectionListener(new TreeSelectionListener() {

						public void valueChanged(TreeSelectionEvent e) {
							TreePath[] selectedPaths = treeFonds
									.getSelectionPaths();
							if (selectedPaths == null
									|| selectedPaths.length == 0) {
								onNoneSelected();
							} else if (selectedPaths.length > 1) {
								List<Object> selected = new ArrayList<Object>(
										selectedPaths.length);
								for (TreePath selectedPath : selectedPaths) {
									selected.add(selectedPath
											.getLastPathComponent());
								}
								onMultipleSelect(selected);
							} else if (selectedPaths.length == 1) {
								Object selected = selectedPaths[0]
										.getLastPathComponent();
								if (selected instanceof FondsTreeNode) {
									Object userObject = ((FondsTreeNode) selected)
											.getUserObject();
									if (userObject instanceof SIPDescriptionObject) {
										onSipDOSelect(
												(SIPDescriptionObject) userObject,
												getRootSIPFromPath(selectedPaths[0]));
									} else if (userObject instanceof DescriptionObject) {
										onClassificationPlanSelect((DescriptionObject) userObject);
									} else if (userObject instanceof SIP) {
										onSipSelect((SIP) userObject);
									}
								}

							}

						}

					});

		}
		return this.treeFonds;
	}

	/**
	 * Search in path, from bottom to top, for a SIP
	 * 
	 * @param path
	 * @return the SIP if any or null.
	 */
	private SIP getRootSIPFromPath(TreePath path) {
		SIP sip = null;
		for (int i = path.getPathCount() - 1; i >= 0; i--) {
			Object node = path.getPathComponent(i);
			if (node instanceof FondsTreeNode) {
				Object userObject = ((FondsTreeNode) node).getUserObject();
				if (userObject instanceof SIP) {
					sip = (SIP) userObject;
					break;
				}
			}
		}

		return sip;
	}

	/**
	 * Get fonds tree model
	 * 
	 * @return the model
	 */
	private FondsTreeModel getFondsTreeModel() {
		if (this.treeModelFonds == null) {
			treeModelFonds = new FondsTreeModel();
		}
		return this.treeModelFonds;
	}

	/**
	 * Create a new SIP under selected classification plan item
	 * 
	 * @return the created SIP
	 * @throws IOException
	 */
	public SIP createNewSip() throws IOException {
		SIP ret = null;
		DefaultMutableTreeNode sipNode = null;
		if (treeFonds.getSelectionPath() != null) {
			Object selected = treeFonds.getSelectionPath()
					.getLastPathComponent();
			if (selected instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selected;
				Object userObject = selectedNode.getUserObject();
				if (userObject instanceof SIPDescriptionObject) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedNode.getParent();
					while(node != null && !(node.getUserObject() instanceof SIP)) {
						node = (DefaultMutableTreeNode) node.getParent();
					}
					
					String parentPid = null;
					if(node != null) {
						SIP sip = (SIP) node.getUserObject();
						parentPid = sip.getParentPID();
					}
					
					sipNode = getFondsTreeModel().createNewSIP(parentPid);
					ret = (SIP) sipNode.getUserObject();
				}
				else if (userObject instanceof DescriptionObject) {
					sipNode = getFondsTreeModel().createNewSIP(
							((DescriptionObject) userObject).getPid());
					ret = (SIP) sipNode.getUserObject();
				} else if (userObject instanceof SIP) {
					sipNode = getFondsTreeModel().createNewSIP(
							((SIP) userObject).getParentPID());
					ret = (SIP) sipNode.getUserObject();

				}
			}
		} else {
			sipNode = getFondsTreeModel().createNewSIP(null);
			ret = (SIP) sipNode.getUserObject();

		}

		if (sipNode != null) {
			// set the SIP root document selected
			treeFonds.setSelectionPath(new TreePath(getFondsTreeModel()
					.getPathToRoot(sipNode.getFirstChild())));
		}
		return ret;
	}

	/**
	 * Check if it is possible to create a new SIP
	 * 
	 * @return true if create new SIP is possible
	 */
	public boolean canCreateNewSip() {
		boolean ret = false;
		if (treeFonds.getSelectionPath() != null) {
			Object selected = treeFonds.getSelectionPath()
					.getLastPathComponent();
			if (selected instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selected;
				Object userObject = selectedNode.getUserObject();
				if (userObject instanceof DescriptionObject) {
					ret = true;
				} else if (userObject instanceof SIP) {
					ret = true;
				}
			}
		} else {
			ret = true;
		}
		return ret;
	}

	/**
	 * Remove the selected SIP
	 * 
	 * @return true if SIP was removed
	 */
	public boolean removeSips() {
		boolean ret = false;
		TreePath[] selectionPaths = treeFonds.getSelectionPaths();
		if (selectionPaths != null) {
			for (TreePath selectedPath : selectionPaths) {
				Object selected = selectedPath.getLastPathComponent();
				if (selected instanceof FondsTreeNode) {
					FondsTreeNode selectedNode = (FondsTreeNode) selected;
					ret = getFondsTreeModel().removeSip(selectedNode);
				}
			}
		}
		return ret;
	}

	/**
	 * Check if remove SIPs is possible with current selected nodes
	 * 
	 * @return true if remove SIPs is possible
	 */
	public boolean canRemoveSips() {
		boolean ret = false;
		TreePath[] selectionPaths = treeFonds.getSelectionPaths();
		if (selectionPaths != null) {
			for (TreePath selectedPath : selectionPaths) {
				Object selected = selectedPath.getLastPathComponent();
				if (selected instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selected;
					if (selectedNode.getUserObject() instanceof SIP) {
						ret = true;
						break;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Check if any SIP has changed since last save
	 * 
	 * @return true if at least one SIP changed
	 */
	public boolean isAnySIPChanged() {
		return getFondsTreeModel().anySIPChanged();
	}

	/**
	 * Save all changed SIPs
	 * 
	 * @return true if save successful, false if at least one SIP could not be
	 *         saved
	 */
	public boolean saveAllSIP() {
		return getFondsTreeModel().saveAllSIP();
	}

	/**
	 * Get the complete set of SIPs
	 * 
	 * @return a SIP set
	 */
	public Set<SIP> getSIPSet() {
		return getFondsTreeModel().getSIPSet();
	}

	/**
	 * Create a new SIP Description Object
	 * 
	 * @param parent
	 * @return the created SIP Description Object
	 */
	public SIPDescriptionObject createNewSipDO(SIPDescriptionObject parent) {
		FondsTreeNode newSipDONode = getFondsTreeModel().createNewSipDO(parent);
		// set new SIP Description Object node selected
		treeFonds.setSelectionPath(new TreePath(getFondsTreeModel()
				.getPathToRoot(newSipDONode)));
		return (SIPDescriptionObject) newSipDONode.getUserObject();
	}

	/**
	 * Remove a SIP Description Object
	 * 
	 * @param target
	 *            the SIP Description Object to remove
	 * @param sip
	 *            the SIP it belongs to
	 * @return true if removed
	 */
	public boolean removeSipDO(SIPDescriptionObject target, SIP sip) {
		boolean removed = true;
		FondsTreeNode parentNode = getFondsTreeModel().removeSipDO(target, sip);
		if (parentNode != null) {
			// set parent SIP Description Object node selected
			treeFonds.setSelectionPath(new TreePath(getFondsTreeModel()
					.getPathToRoot(parentNode)));
		} else {
			removed = false;
		}
		return removed;
	}

	/**
	 * Add a new fonds panel listener
	 * 
	 * @param listener
	 */
	public void addFondsListener(FondsListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove fonds panel listener
	 * 
	 * @param listener
	 */
	public void removeFondsListener(FondsListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Reload all data
	 */
	public void reload() {
		getFondsTreeModel().update();
		getFondsTreeModel().reload();
	}

	protected void onClassificationPlanSelect(DescriptionObject dObj) {
		for (FondsListener listener : listeners) {
			listener.onClassificationPlanSelect(dObj);
		}
	}

	protected void onSipSelect(SIP sip) {
		for (FondsListener listener : listeners) {
			listener.onSipSelect(sip);
		}
	}

	protected void onSipDOSelect(SIPDescriptionObject sipdo, SIP sip) {
		for (FondsListener listener : listeners) {
			listener.onSipDOSelect(sipdo, sip);
		}
	}

	protected void onMultipleSelect(List<Object> selected) {
		for (FondsListener listener : listeners) {
			listener.onMultipleSelect(selected);
		}
	}

	protected void onNoneSelected() {
		for (FondsListener listener : listeners) {
			listener.onNoneSelected();
		}
	}

	/**
	 * Interface to listen to Fonds Panel events
	 */
	public interface FondsListener {
		/**
		 * Event fired when a description object, part of the classification
		 * plan, is selected
		 * 
		 * @param dObj
		 */
		public void onClassificationPlanSelect(DescriptionObject dObj);

		/**
		 * Event fired when a SIP is selected
		 * 
		 * @param sip
		 */
		public void onSipSelect(SIP sip);

		/**
		 * Event fired when a SIP description object is selected
		 * 
		 * @param sipdo
		 *            the SIP Description Object that was selected
		 * @param sip
		 *            the SIP where the Description Object belongs to
		 */
		public void onSipDOSelect(SIPDescriptionObject sipdo, SIP sip);

		/**
		 * Event fired when multiple items are selected. These can be
		 * Classification Plan description objects or SIPs
		 * 
		 * @param selected
		 */
		public void onMultipleSelect(List<Object> selected);

		/**
		 * Event fired when, for some reason, no item is selected
		 */
		public void onNoneSelected();
	}

}
