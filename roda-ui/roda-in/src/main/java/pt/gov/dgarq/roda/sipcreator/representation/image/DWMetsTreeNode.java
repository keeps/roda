package pt.gov.dgarq.roda.sipcreator.representation.image;

import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.metadata.mets.DigitalizedWorkMetsHelper;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public abstract class DWMetsTreeNode implements MutableTreeNode, Transferable {
	private static final Logger logger = Logger.getLogger(DWMetsTreeNode.class);

	private TreeNode parent = null;

	private DigitalizedWorkMetsHelper dwMetsHelper = null;

	private boolean dragOver = false;

	private final List<RepresentationFile> partFiles;

	/**
	 * Get part files
	 * 
	 * @return the part files
	 */
	public List<RepresentationFile> getPartFiles() {
		return partFiles;
	}

	/**
	 * Constructs a new {@link DWMetsTreeNode}.
	 * 
	 * @param parent
	 * @param partFiles
	 * @param dwMetsHelper
	 */
	public DWMetsTreeNode(TreeNode parent, List<RepresentationFile> partFiles,
			DigitalizedWorkMetsHelper dwMetsHelper) {
		this.parent = parent;
		this.partFiles = partFiles;
		setHelper(dwMetsHelper);
	}

	/**
	 * @return the dwMetsHelper
	 */
	public DigitalizedWorkMetsHelper getHelper() {
		return dwMetsHelper;
	}

	/**
	 * @param dwMetsHelper
	 *            the dwMetsHelper to set
	 */
	private void setHelper(DigitalizedWorkMetsHelper dwMetsHelper) {
		this.dwMetsHelper = dwMetsHelper;
	}

	/**
	 * @see MutableTreeNode#removeFromParent()
	 */
	public void removeFromParent() {
		logger.debug("removeFromParent() nothing to do");
	}

	/**
	 * @see MutableTreeNode#setUserObject(Object)
	 */
	public void setUserObject(Object object) {
		logger.debug("setUserObject called");
	}

	/**
	 * @see MutableTreeNode#setParent(MutableTreeNode)
	 */
	public void setParent(MutableTreeNode newParent) {
		this.parent = newParent;
	}

	/**
	 * @see TreeNode#getParent()
	 */
	public TreeNode getParent() {
		return this.parent;
	}

	/**
	 * Is drag over the node
	 * 
	 * @return true if drag is over
	 */
	public boolean isDragOver() {
		return dragOver;
	}

	/**
	 * Set drag over the node, used by drag listener
	 * 
	 * @param dragOver
	 */
	public void setDragOver(boolean dragOver) {
		this.dragOver = dragOver;
	}

}
