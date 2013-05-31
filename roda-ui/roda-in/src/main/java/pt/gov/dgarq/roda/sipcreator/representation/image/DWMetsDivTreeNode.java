package pt.gov.dgarq.roda.sipcreator.representation.image;

import gov.loc.mets.DivType;
import gov.loc.mets.FileType;
import gov.loc.mets.DivType.Fptr;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.metadata.mets.DigitalizedWorkMetsHelper;
import pt.gov.dgarq.roda.core.metadata.mets.MetsFileAlreadyExistsException;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.sipcreator.representation.RepresentationObjectHelper;

/**
 * @author Rui Castro
 * @author Luis Faria
 */
public class DWMetsDivTreeNode extends DWMetsTreeNode {
	private static final Logger logger = Logger
			.getLogger(DWMetsDivTreeNode.class);

	/**
	 * Div Node data flavor
	 */
	public static final DataFlavor DIV_NODE_FLAVOR = new DataFlavor(
			DWMetsDivTreeNode.class, "Mets Div Tree Node");

	private DivType div = null;

	private Vector<DWMetsTreeNode> children = new Vector<DWMetsTreeNode>();

	/**
	 * Constructs a new {@link DWMetsDivTreeNode}.
	 * 
	 * @param parent
	 * @param partFiles
	 * @param dwMetsHelper
	 * @param div
	 */
	public DWMetsDivTreeNode(TreeNode parent,
			List<RepresentationFile> partFiles,
			DigitalizedWorkMetsHelper dwMetsHelper, DivType div) {
		super(parent, partFiles, dwMetsHelper);
		setDiv(div);
	}

	/**
	 * @return the div
	 */
	public DivType getDiv() {
		return div;
	}

	/**
	 * Get the section id
	 * 
	 * @return the section div id
	 */
	public String getId() {
		return getDiv().getLABEL();
	}

	/**
	 * @see DefaultMutableTreeNode#toString()
	 */
	@Override
	public String toString() {
		return getId();
	}

	/**
	 * @see MutableTreeNode#insert(MutableTreeNode, int)
	 */
	public void insert(MutableTreeNode child, int index) {
		if (child instanceof DWMetsDivTreeNode) {
			getDiv().setDivArray(index, ((DWMetsDivTreeNode) child).getDiv());
		} else if (child instanceof DWMetsFptrTreeNode) {
			getDiv()
					.setFptrArray(index, ((DWMetsFptrTreeNode) child).getFptr());
		} else {
			logger.warn("insert(child,index) but child is not a subclass of "
					+ DWMetsTreeNode.class.getSimpleName());
		}
	}

	/**
	 * @see MutableTreeNode#remove(int)
	 */
	public void remove(int index) {
		remove(children.get(index));
	}

	/**
	 * @see MutableTreeNode#remove(MutableTreeNode)
	 */
	public void remove(MutableTreeNode node) {
		if (node instanceof DWMetsDivTreeNode) {
			getDiv().removeDiv(getDiv().getDivList().indexOf(node));
		} else if (node instanceof DWMetsFptrTreeNode) {
			getDiv().removeFptr(getDiv().getFptrList().indexOf(node));
		} else {
			logger.warn("remove(node) but node is not a subclass of " //$NON-NLS-1$
					+ DWMetsTreeNode.class.getSimpleName());
		}
	}

	/**
	 * @see TreeNode#children()
	 */
	public Enumeration<DWMetsTreeNode> children() {
		return new Enumeration<DWMetsTreeNode>() {
			final Iterator<DWMetsTreeNode> iterator = children.iterator();

			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			public DWMetsTreeNode nextElement() {
				return iterator.next();
			}
		};
	}

	/**
	 * @see TreeNode#getAllowsChildren()
	 */
	public boolean getAllowsChildren() {
		return true;
	}

	/**
	 * @see TreeNode#getChildAt(int)
	 */
	public TreeNode getChildAt(int childIndex) {
		return children.get(childIndex);
	}

	/**
	 * @see TreeNode#getChildCount()
	 */
	public int getChildCount() {
		return children.size();
	}

	/**
	 * @see TreeNode#getIndex(TreeNode)
	 */
	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	/**
	 * Always false
	 * 
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	public boolean isLeaf() {
		// return children.size()==0;
		return false;
	}

	/**
	 * @param div
	 *            the div to set
	 */
	private void setDiv(DivType div) {
		this.div = div;
		populateChildList();
	}

	protected void populateChildList() {
		List<DivType> divList = getDiv().getDivList();

		List<Fptr> fptrList = getDiv().getFptrList();
		for (Fptr fptr : fptrList) {
			children.add(new DWMetsFptrTreeNode(this, getPartFiles(),
					getHelper(), fptr));
		}

		for (DivType divType : divList) {
			children.add(new DWMetsDivTreeNode(this, getPartFiles(),
					getHelper(), divType));
		}
	}

	/**
	 * Add a new section
	 * 
	 * @param id
	 *            section id
	 * @return the new section
	 */
	public DWMetsDivTreeNode addSection(String id) {
		DivType createdDiv = getHelper().createDiv(getDiv(), id);
		DWMetsDivTreeNode createdNode = new DWMetsDivTreeNode(this,
				getPartFiles(), getHelper(), createdDiv);
		children.add(createdNode);
		return createdNode;
	}

	/**
	 * Add a new File
	 * 
	 * @param file
	 *            the file to add
	 * @return the file node
	 */
	public DWMetsFptrTreeNode addFile(File file) {
		FileType fileType;
		DWMetsFptrTreeNode fptrNode = null;
		String filePartId = RepresentationObjectHelper
				.getNextFilePartId(getPartFiles());
		RepresentationFile partFile;
		try {
			partFile = new RepresentationFile(filePartId, file.getName(),
					RepresentationBuilder.getFileMimetype(file), file.length(),
					file.toURI().toURL().toString());
			getPartFiles().add(partFile);
			try {
				fileType = getHelper().createFile(getHelper().getNextFileID(),
						filePartId);
			} catch (MetsFileAlreadyExistsException e) {
				fileType = getHelper().getFileByUrl(filePartId);
			}

			Fptr createdFptr = getHelper().createFptr(getDiv(), fileType);
			fptrNode = new DWMetsFptrTreeNode(this, getPartFiles(),
					getHelper(), createdFptr);
			children.add(fptrNode);
		} catch (MalformedURLException e1) {
			logger.error("Error adding file", e1);
		}

		return fptrNode;
	}

	/**
	 * Copy node into under this node
	 * 
	 * @param node
	 *            the node to copy
	 * @return the new node
	 */
	public DWMetsTreeNode copyNode(DWMetsTreeNode node) {
		DWMetsTreeNode newNode = null;
		if (node instanceof DWMetsDivTreeNode) {
			DWMetsDivTreeNode divNode = (DWMetsDivTreeNode) node;
			DivType newDiv = getHelper().copyDiv(divNode.getDiv(), getDiv());
			newNode = new DWMetsDivTreeNode(this, getPartFiles(), getHelper(),
					newDiv);

		} else if (node instanceof DWMetsFptrTreeNode) {
			DWMetsFptrTreeNode fptrNode = (DWMetsFptrTreeNode) node;
			Fptr newFptr = getHelper().copyFptr(fptrNode.getFptr(), getDiv());
			newNode = new DWMetsFptrTreeNode(this, getPartFiles(), getHelper(),
					newFptr);
		}
		children.add(newNode);
		return newNode;

	}

	/**
	 * Copy node into a designated position under this node
	 * 
	 * @param node
	 *            the node to copy
	 * @param beforeIndex
	 * @return the new node
	 */
	public DWMetsTreeNode copyNode(DWMetsTreeNode node, int beforeIndex) {
		DWMetsTreeNode newNode = null;
		if (node instanceof DWMetsDivTreeNode) {
			DWMetsDivTreeNode divNode = (DWMetsDivTreeNode) node;
			DivType newDiv = getHelper().copyDiv(divNode.getDiv(), getDiv(),
					beforeIndex);
			newNode = new DWMetsDivTreeNode(this, getPartFiles(), getHelper(),
					newDiv);

		} else if (node instanceof DWMetsFptrTreeNode) {
			DWMetsFptrTreeNode fptrNode = (DWMetsFptrTreeNode) node;
			Fptr newFptr = getHelper().copyFptr(fptrNode.getFptr(), getDiv(),
					beforeIndex);
			newNode = new DWMetsFptrTreeNode(this, getPartFiles(), getHelper(),
					newFptr);
		}
		children.insertElementAt(newNode, beforeIndex);
		return newNode;

	}

	/**
	 * Remove a node
	 * 
	 * @param node
	 *            the node to remove
	 * @param removeFile
	 *            where to force the removal of the file
	 * @return true if the node was removed, false otherwise
	 */
	public boolean remove(DWMetsTreeNode node, boolean removeFile) {
		boolean ret;
		if (node instanceof DWMetsDivTreeNode) {
			DWMetsDivTreeNode divNode = (DWMetsDivTreeNode) node;
			ret = getHelper().removeDiv(getDiv(), divNode.getDiv());
			ret &= children.remove(node);
		} else if (node instanceof DWMetsFptrTreeNode) {
			DWMetsFptrTreeNode fileNode = (DWMetsFptrTreeNode) node;
			if (removeFile) {
				RepresentationFile partFile = RepresentationObjectHelper
						.lookupPartFileAccessUrl(fileNode.getFileUrl(),
								getPartFiles());
				getPartFiles().remove(partFile);
				getHelper().removeFile(
						getHelper().getFile(fileNode.getFptr().getFILEID()));

			}
			ret = getHelper().removeFptr(getDiv(), fileNode.getFptr());
			ret &= children.remove(node);
		} else {
			ret = false;
		}
		return ret;
	}

	/**
	 * Rename the div
	 * 
	 * @param newId
	 *            the new name
	 */
	public void setId(String newId) {
		getHelper().renameDiv(getDiv(), newId);
	}

	/**
	 * Set user object
	 * 
	 * @param object
	 */
	public void setUserObject(Object object) {
		if (object instanceof String) {
			setId((String) object);
			logger.debug("Setted id " + object);
		}
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
		if (flavor.equals(DIV_NODE_FLAVOR)) {
			return this;
		} else if (flavor.equals(DataFlavor.stringFlavor)) {
			return getId();
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
		return flavor.equals(DIV_NODE_FLAVOR)
				|| flavor.equals(DataFlavor.stringFlavor);
	}

	/**
	 * Get transfer data flavors
	 * 
	 * @return supported data flavors
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DIV_NODE_FLAVOR, DataFlavor.stringFlavor };
	}

}
