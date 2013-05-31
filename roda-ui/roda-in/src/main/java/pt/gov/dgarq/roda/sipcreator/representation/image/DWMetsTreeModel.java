package pt.gov.dgarq.roda.sipcreator.representation.image;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.metadata.mets.DigitalizedWorkMetsHelper;

/**
 * @author Rui Castro
 * @author Luis Faria
 * 
 */
public class DWMetsTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = -1635177211598006706L;

	// private static final Logger logger = Logger
	// .getLogger(DWMetsTreeModel.class);

	private final List<RepresentationFile> partFiles;
	private boolean changed;

	/**
	 * Constructs a new {@link DWMetsTreeModel}.
	 * 
	 * @param repObj
	 * @param metsHelper
	 */
	public DWMetsTreeModel(RepresentationObject repObj,
			DigitalizedWorkMetsHelper metsHelper) {
		this(new ArrayList<RepresentationFile>(Arrays.asList(repObj
				.getPartFiles() == null ? new RepresentationFile[] {} : repObj
				.getPartFiles())), metsHelper);
	}

	/**
	 * Constructs a new {@link DWMetsTreeModel}.
	 * 
	 * @param partFiles
	 * @param dwMetsHelper
	 */
	public DWMetsTreeModel(List<RepresentationFile> partFiles,
			DigitalizedWorkMetsHelper dwMetsHelper) {
		super(new DWMetsDivTreeNode(null, partFiles, dwMetsHelper, dwMetsHelper
				.getRepresentationFileStructureDiv()));
		this.partFiles = partFiles;
		this.changed = false;
	}

	/**
	 * Get DW Mets tree root node
	 */
	@Override
	public DWMetsTreeNode getRoot() {
		return (DWMetsTreeNode) super.getRoot();
	}

	/**
	 * Get part files
	 * 
	 * @return the part files
	 */
	public List<RepresentationFile> getPartFiles() {
		return partFiles;
	}

	/**
	 * Add new section to a section and fire event
	 * 
	 * @param parent
	 *            the parent of the new section
	 * @param id
	 *            the id of the new section
	 * @return the added section
	 */
	public DWMetsDivTreeNode addSection(DWMetsDivTreeNode parent, String id) {
		DWMetsDivTreeNode addedSection = parent.addSection(id);
		int addedSectionIndex = parent.getIndex(addedSection);
		fireTreeNodesInserted(parent, getPathToRoot(parent),
				new int[] { addedSectionIndex },
				new DWMetsDivTreeNode[] { addedSection });
		changed = true;
		return addedSection;
	}

	/**
	 * Add a new file to a section
	 * 
	 * @param parent
	 *            the parent section
	 * @param file
	 *            the file to add
	 * @return the added file
	 */
	public DWMetsFptrTreeNode addFile(DWMetsDivTreeNode parent, File file) {
		DWMetsFptrTreeNode addedFile = parent.addFile(file);
		int addedFileIndex = parent.getIndex(addedFile);
		fireTreeNodesInserted(parent, getPathToRoot(parent),
				new int[] { addedFileIndex },
				new DWMetsFptrTreeNode[] { addedFile });
		changed = true;
		return addedFile;
	}

	/**
	 * Remove a node and fire event
	 * 
	 * @param child
	 * @param removeFile
	 *            true to remove related files
	 * @return true if the node was removed, false otherwise
	 */
	public boolean remove(DWMetsTreeNode child, boolean removeFile) {
		boolean removed = false;
		TreeNode[] pathToRoot = getPathToRoot(child);
		if (pathToRoot.length > 1) {
			TreeNode parentNode = pathToRoot[pathToRoot.length - 2];
			if (parentNode instanceof DWMetsDivTreeNode) {
				DWMetsDivTreeNode parentDiv = (DWMetsDivTreeNode) parentNode;
				removed = remove(child, parentDiv, removeFile);
			}
		}
		return removed;
	}

	protected boolean remove(DWMetsTreeNode child, DWMetsDivTreeNode parent,
			boolean removeFile) {
		boolean removed;
		int childIndex = parent.getIndex(child);
		removed = parent.remove(child, removeFile);
		TreeNode[] parentPathToRoot = getPathToRoot(parent);
		if (removed) {
			fireTreeNodesRemoved(parent, parentPathToRoot,
					new int[] { childIndex }, new DWMetsTreeNode[] { child });
			changed = true;
		}
		return removed;
	}

	/**
	 * Rename a div node and fire event
	 * 
	 * @param divNode
	 *            the div node to rename
	 * @param newName
	 *            the new name
	 */
	public void renameDivNode(DWMetsDivTreeNode divNode, String newName) {
		divNode.setId(newName);
		if (!divNode.getId().equals(newName)) {
			TreeNode[] pathToRoot = getPathToRoot(divNode);
			if (pathToRoot.length > 1) {
				TreeNode parent = pathToRoot[pathToRoot.length - 2];
				TreeNode[] parentPathToRoot = Arrays.asList(pathToRoot)
						.subList(0, pathToRoot.length - 1).toArray(
								new TreeNode[pathToRoot.length - 1]);
				int index = parent.getIndex(divNode);
				fireTreeNodesChanged(parent, parentPathToRoot,
						new int[] { index },
						new DWMetsDivTreeNode[] { divNode });
				changed = true;
			}
		}

	}

	/**
	 * Copy a node under div
	 * 
	 * @param node
	 *            the node to copy
	 * @param target
	 *            the target div
	 * @return the new node
	 */
	public DWMetsTreeNode copy(DWMetsTreeNode node, DWMetsDivTreeNode target) {
		DWMetsTreeNode newNode = target.copyNode(node);
		int index = getIndexOfChild(target, newNode);
		fireTreeNodesInserted(target, getPathToRoot(target),
				new int[] { index }, new Object[] { newNode });
		changed = true;
		return newNode;
	}

	/**
	 * Copy a node to a designated position under a div
	 * 
	 * @param node
	 *            the node to copy
	 * @param target
	 *            the target div
	 * @param beforeIndex
	 *            the designated position
	 * @return the new node
	 */
	public DWMetsTreeNode copy(DWMetsTreeNode node, DWMetsDivTreeNode target,
			int beforeIndex) {
		DWMetsTreeNode newNode = target.copyNode(node, beforeIndex);
		int index = getIndexOfChild(target, newNode);
		fireTreeNodesInserted(target, getPathToRoot(target),
				new int[] { index }, new Object[] { newNode });
		changed = true;
		return newNode;
	}

	/**
	 * Move a node under a div
	 * 
	 * @param node
	 *            the node to move
	 * @param target
	 *            the target div
	 * @return the new node
	 */
	public DWMetsTreeNode move(DWMetsTreeNode node, DWMetsDivTreeNode target) {
		DWMetsTreeNode newNode = copy(node, target);
		remove(node, false);
		return newNode;
	}

	/**
	 * Move a node to a designated position under a div
	 * 
	 * @param node
	 *            the node to move
	 * @param target
	 *            the target div the target div
	 * @param beforeIndex
	 *            the designated position
	 * 
	 * @return the new node
	 */
	public DWMetsTreeNode move(DWMetsTreeNode node, DWMetsDivTreeNode target,
			int beforeIndex) {
		DWMetsTreeNode newNode = copy(node, target, beforeIndex);
		remove(node, false);
		return newNode;
	}

	/**
	 * Change the node position, moving the node
	 * 
	 * @param node
	 *            the node to change the position
	 * @param beforeIndex
	 *            the new position
	 * @return the new node, under the new position
	 */
	public DWMetsTreeNode move(DWMetsTreeNode node, int beforeIndex) {
		DWMetsTreeNode newNode = copy(node, (DWMetsDivTreeNode) node
				.getParent(), beforeIndex);
		remove(node, false);
		return newNode;
	}

	/**
	 * Move the node up one position
	 * 
	 * @param node
	 *            the node to move
	 * @return the new node
	 */
	public DWMetsTreeNode moveUp(DWMetsTreeNode node) {
		int index = getIndexOfChild(node.getParent(), node);
		return move(node, index - 1);
	}

	/**
	 * Move the node down one position
	 * 
	 * @param node
	 *            the node to move
	 * @return the new node
	 */
	public DWMetsTreeNode moveDown(DWMetsTreeNode node) {
		int index = getIndexOfChild(node.getParent(), node);
		return move(node, index + 2);
	}

	/**
	 * Was model changed
	 * 
	 * @return true if changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Set model changed
	 * 
	 * @param changed
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}
