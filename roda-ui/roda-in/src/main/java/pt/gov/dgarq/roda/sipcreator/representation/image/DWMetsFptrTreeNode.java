package pt.gov.dgarq.roda.sipcreator.representation.image;

import gov.loc.mets.DivType.Fptr;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.metadata.mets.DigitalizedWorkMetsHelper;
import pt.gov.dgarq.roda.sipcreator.representation.RepresentationObjectHelper;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class DWMetsFptrTreeNode extends DWMetsTreeNode {
	private static final Logger logger = Logger
			.getLogger(DWMetsFptrTreeNode.class);

	/**
	 * FPTR Node data flavor
	 */
	public static final DataFlavor FTPR_NODE_FLAVOR = new DataFlavor(
			DWMetsFptrTreeNode.class, "Mets Fptr Tree Node");

	private Fptr fptr = null;

	/**
	 * Constructs a new {@link DWMetsFptrTreeNode}.
	 * 
	 * @param parent
	 * @param partFiles
	 * @param dwMetsHelper
	 * @param fptr
	 */
	public DWMetsFptrTreeNode(TreeNode parent,
			List<RepresentationFile> partFiles,
			DigitalizedWorkMetsHelper dwMetsHelper, Fptr fptr) {
		super(parent, partFiles, dwMetsHelper);
		setFptr(fptr);
	}

	/**
	 * @return the fptr
	 */
	public Fptr getFptr() {
		return fptr;
	}

	/**
	 * @param fptr
	 *            the fptr to set
	 */
	public void setFptr(Fptr fptr) {
		this.fptr = fptr;
	}

	/**
	 * @see DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString() {
		return getHelper().getFileHref(getFptr().getFILEID());
	}

	/**
	 * @see MutableTreeNode#insert(MutableTreeNode, int)
	 */
	public void insert(MutableTreeNode child, int index) {
		logger.debug("insert(child, index) not possible"); //$NON-NLS-1$
	}

	/**
	 * @see MutableTreeNode#remove(int)
	 */
	public void remove(int index) {
		logger.debug("remove(index) nothing to do"); //$NON-NLS-1$
	}

	/**
	 * @see MutableTreeNode#remove(MutableTreeNode)
	 */
	public void remove(MutableTreeNode node) {
		logger.debug("remove(node) nothing to do"); //$NON-NLS-1$
	}

	/**
	 * @see TreeNode#children()
	 */
	public Enumeration<TreeNode> children() {
		return null;
	}

	/**
	 * 
	 * @see TreeNode#getAllowsChildren()
	 */
	public boolean getAllowsChildren() {
		return false;
	}

	/**
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	public TreeNode getChildAt(int childIndex) {
		return null;
	}

	/**
	 * @see TreeNode#getChildCount()
	 */
	public int getChildCount() {
		return 0;
	}

	/**
	 * @see TreeNode#getIndex(TreeNode)
	 */
	public int getIndex(TreeNode node) {
		return 0;
	}

	/**
	 * Always return false.
	 * 
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	public boolean isLeaf() {
		return true;
	}

	/**
	 * Get file URL
	 * 
	 * @return the file URL
	 */
	public String getFileUrl() {
		String filePartId = getHelper().getFileHref(getFptr().getFILEID());
		RepresentationFile partFile = RepresentationObjectHelper
				.lookupPartFile(filePartId, getPartFiles());
		return (partFile != null) ? partFile.getAccessURL() : null;
	}

	/**
	 * Get file original name
	 * 
	 * @return the file original name
	 */
	public String getFileOriginalName() {
		String filePartId = getHelper().getFileHref(getFptr().getFILEID());
		RepresentationFile partFile = RepresentationObjectHelper
				.lookupPartFile(filePartId, getPartFiles());

		return (partFile != null) ? partFile.getOriginalName() : null;
	}

	/**
	 * Get transfer data
	 * 
	 * @param flavor
	 * @return the data on the defined flavor
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(FTPR_NODE_FLAVOR)) {
			return this;
		} else if (flavor.equals(DataFlavor.stringFlavor)) {
			return getFileUrl();
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	/**
	 * Is data flavor supported
	 * 
	 * @param flavor
	 * @return true if supported
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(FTPR_NODE_FLAVOR)
				|| flavor.equals(DataFlavor.stringFlavor);
	}

	/**
	 * Get transfer data flavors
	 * 
	 * @return the flavors
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { FTPR_NODE_FLAVOR, DataFlavor.stringFlavor };
	}

}
