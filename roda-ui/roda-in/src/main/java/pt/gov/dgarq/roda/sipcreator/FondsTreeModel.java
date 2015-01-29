package pt.gov.dgarq.roda.sipcreator;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelManager;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangedEvent;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;
import pt.gov.dgarq.roda.util.NaturalOrderComparator;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class FondsTreeModel extends DefaultTreeModel implements
		DataChangeListener {
	private static final long serialVersionUID = 6662007794704234842L;

	private static final Logger logger = Logger.getLogger(FondsTreeModel.class);

	private final FondsTreeNode nodeFonds;

	/**
	 * Maps an Description Object PID to the tree node
	 */
	private final Map<String, FondsTreeNode> classificationPlanNodes;

	private final Map<SIP, FondsTreeNode> sipNodes;

	private final Map<SIPDescriptionObject, FondsTreeNode> sipDoNodes;

	/**
	 * Constructs a new {@link FondsTreeModel}.
	 */
	public FondsTreeModel() {
		super(null);
		nodeFonds = new FondsTreeNode("Fonds");
		setRoot(nodeFonds);
		classificationPlanNodes = new HashMap<String, FondsTreeNode>();
		sipNodes = new HashMap<SIP, FondsTreeNode>();
		sipDoNodes = new HashMap<SIPDescriptionObject, FondsTreeNode>();
		update();

	}

	protected void sortById(List<DescriptionObject> dobjs) {
		Collections.sort(dobjs, new Comparator<DescriptionObject>() {
			NaturalOrderComparator naturalComparator = new NaturalOrderComparator();

			public int compare(DescriptionObject dobj1, DescriptionObject dobj2) {
				return naturalComparator.compare(dobj1.getId(), dobj2.getId());
			}

		});
	}

	/**
	 * Update fonds tree model
	 */
	public void update() {
		nodeFonds.removeAllChildren();
		classificationPlanNodes.clear();
		List<DescriptionObject> collections = MyClassificationPlanHelper
				.getInstance().getCollections();
		if (collections.size() > 0) {
			sortById(collections);
			populate(nodeFonds, collections);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					JOptionPane.showMessageDialog(
							SIPCreator.getInstance().getMainFrame(),
							Messages.getString("FondsTreeModel.warning.EMPTY_CLASSIFICATION_PLAN"),
							Messages.getString("common.WARNING"),
							JOptionPane.WARNING_MESSAGE);
				}

			});

		}
		updateSipList();
	}

	private FondsTreeNode getTreeNode(DescriptionObject obj) {
		FondsTreeNode node = new FondsTreeNode(obj);
		return node;
	}

	private void populate(FondsTreeNode parent, List<DescriptionObject> children) {
		sortById(children);
		for (DescriptionObject child : children) {
			if (child != null) {
				FondsTreeNode childNode = getTreeNode(child);
				parent.add(childNode);
				classificationPlanNodes.put(child.getPid(), childNode);
				populate(childNode, MyClassificationPlanHelper.getInstance()
						.getSubElements(child.getPid()));
			}
		}
	}

	private FondsTreeNode getTreeNode(SIP sip) {
		FondsTreeNode node = new FondsTreeNode(sip);
		FondsTreeNode dObjNode = getTreeNode(sip.getDescriptionObject());
		node.add(dObjNode);
		sipNodes.put(sip, node);
		return node;
	}

	private FondsTreeNode getTreeNode(SIPDescriptionObject dObj) {
		FondsTreeNode dObjNode = new FondsTreeNode(dObj);
		for (SIPDescriptionObject subDObj : dObj.getChildren()) {
			dObjNode.add(getTreeNode(subDObj));
		}
		sipDoNodes.put(dObj, dObjNode);
		return dObjNode;
	}

	private void updateSipList() {
		for (SIP sip : getSIPs()) {
			final FondsTreeNode sipNode = getTreeNode(sip);
			if (classificationPlanNodes.containsKey(sip.getParentPID())) {
				logger.debug("Adding SIP to tree");
				classificationPlanNodes.get(sip.getParentPID()).add(sipNode);
			} else {
				logger.debug("Adding SIP to root tree");
				// SIP is orphan, add it to fonds node
				nodeFonds.add(sipNode);
			}
			sip.addChangeListener(this);
		}
	}

	private List<SIP> getSIPs() {
		List<SIP> ret = new Vector<SIP>();
		File sipDraftDir = SIPCreatorConfig.getInstance().getSipDraftDir();
		File sipSentDir = SIPCreatorConfig.getInstance().getSipSentDir();
		logger.debug("Loading Draft SIPs");
		for (File sipDir : sipDraftDir.listFiles()) {
			if (sipDir.isDirectory()) {
				logger.debug("Loading SIP in " + sipDir.getName());
				try {
					SIP sip = new SIP(SIPUtility.readSIP(sipDir), false);
					ret.add(sip);
				} catch (SIPException e) {
					logger.error(
							"Error reading sip from "
									+ sipDir.getAbsolutePath(), e);
				}
			} else {
				logger.info("Ignoring file " + sipDir.getName());
			}
		}

		logger.debug("Loading Sent SIPs");
		for (File sipDir : sipSentDir.listFiles()) {
			if (sipDir.isDirectory()) {
				logger.debug("Loading SIP in " + sipDir.getName());
				try {
					SIP sip = new SIP(SIPUtility.readSIP(sipDir), true);
					ret.add(sip);
				} catch (SIPException e) {
					logger.error(
							"Error reading sip from "
									+ sipDir.getAbsolutePath(), e);
				}
			} else {
				logger.info("Ignoring file " + sipDir.getName());
			}
		}

		logger.debug("SIP list size before sort " + ret.size());
		Tools.sortSipList(ret);
		logger.debug("SIP list size before sort " + ret.size());
		return ret;
	}

	/**
	 * Create new SIP
	 * 
	 * @param parentPid
	 *            the PID of the parent
	 * @return the new SIP
	 * @throws IOException
	 */
	public FondsTreeNode createNewSIP(String parentPid) throws IOException {
		FondsTreeNode ret = null;
		SIP sip = new SIP();
		sip.setSent(false);
		// TODO get SIP initial description object from properties
		SIPDescriptionObject dObj = new SIPDescriptionObject();
		dObj.setLevel(DescriptionLevelManager.getFirstLeafDescriptionLevel());
		dObj.setCountryCode("PT");
		dObj.setRepositoryCode("KEEPS");
		sip.setDescriptionObject(dObj);
		sip.setParentPID(parentPid);

		final FondsTreeNode sipNode = getTreeNode(sip);
		ret = sipNode;
		FondsTreeNode parentNode;
		if (classificationPlanNodes.containsKey(parentPid)) {
			parentNode = classificationPlanNodes.get(parentPid);

		} else {
			parentNode = nodeFonds;
		}

		sip.addChangeListener(this);

		parentNode.add(ret);
		int index = getIndexOfChild(parentNode, ret);
		fireTreeNodesInserted(parentNode, getPathToRoot(parentNode),
				new int[] { index }, new Object[] { ret });

		sipNodes.put(sip, sipNode);

		return ret;
	}

	/**
	 * Remove a SIP
	 * 
	 * @param selectedNode
	 * @return true if SIP was removed
	 */
	public boolean removeSip(FondsTreeNode selectedNode) {
		boolean ret = false;
		Object userObject = selectedNode.getUserObject();
		if (userObject instanceof SIP) {
			SIP sip = (SIP) userObject;
			if (sip.getDirectory() != null) {
				try {
					FileUtils.deleteDirectory(sip.getDirectory());
					ret = true;
				} catch (IOException e) {
					logger.error("Error removing sip " + sip, e);
				}
			}
			removeNodeFromParent(selectedNode);
			ret = true;
			sipNodes.remove(sip);
		}
		return ret;
	}

	/**
	 * Create a new SIP Description Object
	 * 
	 * @param parent
	 *            the SIP Description Object parent
	 * @return the created fonds tree node
	 */
	public FondsTreeNode createNewSipDO(SIPDescriptionObject parent) {
		FondsTreeNode newSipDoNode = null;
		if (DescriptionLevelManager.getRepresentationsDescriptionLevels()
				.contains(parent.getLevel())
				&& !DescriptionLevelManager.getLeafDescriptionLevels()
						.contains(parent.getLevel())) {
			// create new SIP DO
			// TODO get SIP DO initial description object from properties
			SIPDescriptionObject dObj = new SIPDescriptionObject();
			dObj.setLevel(DescriptionLevelManager
					.getFirstLeafDescriptionLevel());
			dObj.setCountryCode("PT");
			dObj.setRepositoryCode("KEEPS");

			// Update data
			parent.addChild(dObj);
			newSipDoNode = getTreeNode(dObj);

			// update tree
			FondsTreeNode parentNode = sipDoNodes.get(parent);
			parentNode.add(newSipDoNode);

			// update node list
			sipDoNodes.put(dObj, newSipDoNode);

			// fire events
			int index = getIndexOfChild(parentNode, newSipDoNode);
			fireTreeNodesInserted(parentNode, getPathToRoot(parentNode),
					new int[] { index }, new Object[] { newSipDoNode });

		}
		return newSipDoNode;
	}

	/**
	 * Remove SIP Description Object
	 * 
	 * @param sipdo
	 *            the SIP Description Object to remove
	 * @param sip
	 *            the SIP to which the description object belongs to
	 * @return the parent FondsTreeNode or null if remove unsuccessful
	 */
	public FondsTreeNode removeSipDO(SIPDescriptionObject sipdo, SIP sip) {
		FondsTreeNode parentNode = null;
		if (!sip.getDescriptionObject().equals(sipdo)) {
			// get parent
			SIPDescriptionObject parent = getSipDOParent(sipdo,
					sip.getDescriptionObject());

			// remove from parent
			parent.removeChild(sipdo);

			// remove from tree
			parentNode = sipDoNodes.get(parent);
			FondsTreeNode node = sipDoNodes.get(sipdo);

			int index = getIndexOfChild(parentNode, node);

			parentNode.remove(node);
			sipDoNodes.remove(sipdo);

			// fire events
			fireTreeNodesRemoved(parentNode, getPathToRoot(parentNode),
					new int[] { index }, new Object[] { node });

		}
		return parentNode;
	}

	private SIPDescriptionObject getSipDOParent(SIPDescriptionObject sipdo,
			SIPDescriptionObject root) {
		SIPDescriptionObject ret = null;
		if (root.getChildren().contains(sipdo)) {
			ret = root;
		} else {
			for (SIPDescriptionObject child : root.getChildren()) {
				SIPDescriptionObject sipDOParent = getSipDOParent(sipdo, child);
				if (sipDOParent != null) {
					ret = sipDOParent;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * Get root node, fonds node
	 * 
	 * @return return root node
	 */
	public FondsTreeNode getRoot() {
		return nodeFonds;
	}

	/**
	 * Move sip to a new location
	 * 
	 * @param sip
	 * @param parentPID
	 * @return the added node
	 * @throws IOException
	 * @throws SIPException
	 */
	public FondsTreeNode move(SIP sip, String parentPID) throws IOException,
			SIPException {

		sip.removeChangeListener(this);

		// Change SIP state
		sip.setParentPID(parentPID);
		sip.setSent(false);

		// Change fonds tree model state
		FondsTreeNode sipNode = sipNodes.get(sip);
		FondsTreeNode newParent = classificationPlanNodes.get(parentPID);
		FondsTreeNode oldParent = (FondsTreeNode) sipNode.getParent();
		int oldIndex = oldParent.getIndex(sipNode);
		oldParent.remove(sipNode);
		newParent.add(sipNode);
		int newIndex = newParent.getIndex(sipNode);

		// Save SIP
		sip.save();

		sip.addChangeListener(this);

		// Fire change events
		fireTreeNodesRemoved(oldParent, getPathToRoot(oldParent),
				new int[] { oldIndex }, new Object[] { sipNode });
		fireTreeNodesInserted(newParent, getPathToRoot(newParent),
				new int[] { newIndex }, new Object[] { sipNode });

		return sipNode;

	}

	/**
	 * Check if any SIP was changed after last save
	 * 
	 * @return true if at least one SIP changed
	 */
	public boolean anySIPChanged() {
		boolean ret = false;
		for (SIP sip : sipNodes.keySet()) {
			if (sip.isChanged()) {
				logger.debug("SIP " + sip.getCompleteReference()
						+ " has changes");
				ret = true;
				break;
			}
		}
		return ret;
	}

	/**
	 * Save all changed SIPs
	 * 
	 * @return true if save successful, false if at least one SIP could not be
	 *         saved
	 */
	public boolean saveAllSIP() {
		final boolean[] success = new boolean[] { true };
		Loading.run(Messages.getString("FondsTreeModel.SAVING_ALL_SIP"),
				new Runnable() {

					public void run() {
						for (SIP sip : sipNodes.keySet()) {
							if (sip.isSaveValid() && sip.isChanged()) {
								Loading.setMessage(Messages.getString(
										"FondsTreeModel.SAVING_SIP",
										sip.getCompleteReference()));
								try {
									success[0] = sip.save() && success[0];
								} catch (SIPException e) {
									success[0] = false;
								} catch (IOException e) {
									success[0] = false;
								}
							}
						}

					}

				});

		return success[0];
	}

	/**
	 * Get SIP List
	 * 
	 * @return the SIP list
	 */
	public Set<SIP> getSIPSet() {
		return sipNodes.keySet();
	}

	/**
	 * Method called when a Data Changed Event occurs, in which this class is a
	 * listener
	 * 
	 * @param evtDataChanged
	 *            the data changed event
	 */
	public void dataChanged(DataChangedEvent evtDataChanged) {
		SIP sip = (SIP) evtDataChanged.getSource();
		FondsTreeNode sipNode = sipNodes.get(sip);

		TreeNode parent = sipNode.getParent();
		TreeNode[] parentPath = getPathToRoot(parent);
		int index = parent.getIndex(sipNode);
		fireTreeNodesChanged(parent, parentPath, new int[] { index },
				new Object[] { sipNode });
		// reload(sipNode);

	}
}
