package pt.gov.dgarq.roda.sipcreator.representation.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.sipcreator.SIPCreator;
import pt.gov.dgarq.roda.sipcreator.Tools;
import pt.gov.dgarq.roda.sipcreator.representation.DragNDropHelper;

/**
 * @author Luis Faria
 * 
 */
public class DWMetsTree extends JTree implements TreeSelectionListener,
		DragGestureListener, DropTargetListener, DragSourceListener, Autoscroll {

	private static final long serialVersionUID = -6853144354857110666L;

	private static final String IMAGE_BASE = "/pt/gov/dgarq/roda/sipcreator/digitalizedWork/";
	private static final Logger logger = Logger.getLogger(DWMetsTree.class);

	/** Stores the selected node info */
	protected TreePath selectedTreePath = null;
	protected DWMetsTreeNode selectedNode = null;

	/** Variables needed for DnD */
	private DragSource dragSource = null;

	private final DWMetsTreeModel model;
	private final DWRepresentationPanel repPanel;

	/**
	 * Create a new digitalized work Mets tree
	 * 
	 * @param model
	 * @param repPanel
	 */
	public DWMetsTree(DWMetsTreeModel model, DWRepresentationPanel repPanel) {
		super(model);
		this.model = model;
		this.repPanel = repPanel;
		DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 2562760934145767385L;

			private final Icon picture = Tools.createImageIcon(IMAGE_BASE
					+ "picture.png");

			private final Icon folder = Tools.createImageIcon(IMAGE_BASE
					+ "folder.png");

			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, selected,
						expanded, leaf, row, hasFocus);
				if (value instanceof DWMetsTreeNode) {
					DWMetsTreeNode metsNode = (DWMetsTreeNode) value;
					if (metsNode.isDragOver()) {
						setBorder(BorderFactory.createLineBorder(Color.BLACK));
					} else {
						setBorder(BorderFactory.createEmptyBorder());
					}
				}
				if (value instanceof DWMetsFptrTreeNode) {
					DWMetsFptrTreeNode fptrNode = (DWMetsFptrTreeNode) value;

					String text = fptrNode.getFileOriginalName();
					if (text == null) {
						text = fptrNode.getFileUrl();
					}
					setText(fptrNode.getFileOriginalName());
					setToolTipText(fptrNode.getFileUrl());

				}
				return this;
			}

			public Icon getClosedIcon() {
				return folder;
			}

			public Icon getOpenIcon() {
				return folder;
			}

			public Icon getLeafIcon() {
				return picture;
			}

		};

		DefaultTreeCellEditor cellEditor = new DefaultTreeCellEditor(this,
				cellRenderer, new DefaultCellEditor(new JTextField())) {
			protected boolean canEditImmediately(EventObject event) {
				boolean ret = false;
				if (event == null) {
					ret = true;
				} else if (event instanceof MouseEvent) {
					MouseEvent mouseEvent = (MouseEvent) event;
					ret = mouseEvent.getClickCount() > 1
							&& inHitRegion(mouseEvent.getX(), mouseEvent.getY());
				}
				return ret;
			}

		};

		this.setCellRenderer(cellRenderer);
		this.setCellEditor(cellEditor);
		this.setEditable(true);

		addTreeSelectionListener(this);

		dragSource = DragSource.getDefaultDragSource();

		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_COPY_OR_MOVE, this);
		dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
		new DropTarget(this, this);

		setSelectionPath(new TreePath(new TreeNode[] { getDWMetsTreeModel()
				.getRoot() }));

	}

	/**
	 * Get digitalized work Mets tree model
	 * 
	 * @return the digitalized work Mets tree model
	 */
	public DWMetsTreeModel getDWMetsTreeModel() {
		return model;
	}

	/**
	 * Get selected node
	 * 
	 * @return the selected node
	 */
	public DWMetsTreeNode getSelectedNode() {
		return selectedNode;
	}

	/**
	 * Value changed listener action
	 * 
	 * @param evt
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		selectedTreePath = evt.getNewLeadSelectionPath();
		if (selectedTreePath == null) {
			selectedNode = null;
			return;
		}
		selectedNode = (DWMetsTreeNode) selectedTreePath.getLastPathComponent();

	}

	/**
	 * Drag gesture recognized listener action
	 * 
	 * @param dge
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		// Get the selected node
		DWMetsTreeNode dragNode = getSelectedNode();
		if (dragNode != null) {

			// Get the Transferable Object
			Transferable transferable = dragNode;

			// Select the appropriate cursor;
			// Cursor cursor = DragSource.DefaultCopyNoDrop;
			// int action = dge.getDragAction();
			// if (action == DnDConstants.ACTION_MOVE)
			// cursor = DragSource.DefaultMoveNoDrop;

			// begin the drag
			dragSource.startDrag(dge, null, transferable, this);
		}
	}

	/**
	 * Drag enter listener action
	 * 
	 * @param e
	 */
	public void dragEnter(DropTargetDragEvent e) {
		// nothing to do
	}

	/**
	 * Drag exit listener action
	 * 
	 * @param e
	 */
	public void dragExit(DropTargetEvent e) {
	}

	private DWMetsTreeNode highlightNode = null;

	private void highlightDestination(TreePath path, boolean isBackground) {
		if (path != null) {
			Object destination = path.getLastPathComponent();
			if (path.getLastPathComponent() instanceof DWMetsTreeNode) {
				DWMetsTreeNode metsNode = (DWMetsTreeNode) destination;
				if (highlightNode != null && highlightNode != metsNode) {
					highlightNode.setDragOver(false);
				}
				metsNode.setDragOver(true);
				highlightNode = metsNode;
				repaint();
			}

		} else if (isBackground) {
			DWMetsTreeNode metsNode = getDWMetsTreeModel().getRoot();
			if (highlightNode != null && highlightNode != metsNode) {
				highlightNode.setDragOver(false);
			}
			metsNode.setDragOver(true);
			highlightNode = metsNode;
			repaint();
		} else if (highlightNode != null) {
			highlightNode.setDragOver(false);
			repaint();
		}
	}

	private static final double autoScrollThreshold = 10;
	private static final int autoScrollAdjustment = 20;

	/**
	 * Auto scroll to a defined location
	 * 
	 * @param loc
	 *            the location
	 */
	public void autoscroll(Point loc) {
		if (getParent() instanceof JViewport
				&& getParent().getParent() instanceof JScrollPane) {
			JViewport viewPort = (JViewport) getParent();
			JScrollPane scroll = (JScrollPane) viewPort.getParent();
			JScrollBar vScrollBar = scroll.getVerticalScrollBar();

			int y = loc.y - viewPort.getViewPosition().y;
			int bottom = viewPort.getExtentSize().height;
			int currValue = vScrollBar.getValue();

			if (y < autoScrollThreshold) {
				vScrollBar.setValue(Math.max(currValue - autoScrollAdjustment,
						0));
			} else if (y > bottom - autoScrollThreshold) {
				vScrollBar.setValue(currValue + autoScrollAdjustment);
			}

		}

	}

	/**
	 * Drag over listener action
	 * 
	 * @param e
	 */
	public void dragOver(DropTargetDragEvent e) {
		Point cursorLocationBis = e.getLocation();
		TreePath destinationPath = getPathForLocation(cursorLocationBis.x,
				cursorLocationBis.y);
		boolean destinationIsBackground = destinationPath == null
				&& getParent().contains(cursorLocationBis);

		highlightDestination(destinationPath, destinationIsBackground);
		autoscroll(cursorLocationBis);

		// if destination path is okay accept drop...
		Transferable transferable = e.getTransferable();

		if (destinationIsBackground || testDrop(destinationPath, transferable)) {
			e.acceptDrag(DnDConstants.ACTION_MOVE);
			SIPCreator.setDefaultStatusMessage();
		}
		// ...otherwise reject drop
		else {
			e.rejectDrag();
			if (highlightNode != null) {
				highlightNode.setDragOver(false);
				repaint();
			}
		}

	}

	/**
	 * Drop listener action
	 * 
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent e) {
		try {
			Transferable tr = e.getTransferable();
			DWMetsTreeNode newNodes[] = null;

			// get new parent node
			Point loc = e.getLocation();
			TreePath destination = getPathForLocation(loc.x, loc.y);
			if (destination == null && getParent().contains(loc)) {
				destination = new TreePath(
						new TreeNode[] { getDWMetsTreeModel().getRoot() });
				setSelectionPath(destination);
			}

			DWMetsTreeNode newParent = (DWMetsTreeNode) destination
					.getLastPathComponent();

			// is it a Mets Div Tree node?
			if (tr.isDataFlavorSupported(DWMetsDivTreeNode.DIV_NODE_FLAVOR)) {
				logger.debug("Div node flavor");
				// get source div
				DWMetsDivTreeNode divNode = (DWMetsDivTreeNode) tr
						.getTransferData(DWMetsDivTreeNode.DIV_NODE_FLAVOR);
				// test drop target
				if (testDrop(destination, divNode)
						&& newParent instanceof DWMetsDivTreeNode) {
					e.acceptDrop(DnDConstants.ACTION_MOVE);
					// move div
					DWMetsTreeNode newNode = getDWMetsTreeModel().move(divNode,
							(DWMetsDivTreeNode) newParent);
					newNodes = newNode != null ? new DWMetsTreeNode[] { newNode }
							: null;
				} else {
					e.rejectDrop();
				}

			}
			// is it a Mets Fptr Tree node?
			else if (tr
					.isDataFlavorSupported(DWMetsFptrTreeNode.FTPR_NODE_FLAVOR)) {
				logger.debug("Fptr node flavor");
				// get source fptr node
				DWMetsFptrTreeNode fptrNode = (DWMetsFptrTreeNode) tr
						.getTransferData(DWMetsFptrTreeNode.FTPR_NODE_FLAVOR);
				// test drop target
				if (testDrop(destination, fptrNode)) {
					e.acceptDrop(DnDConstants.ACTION_MOVE);
					if (newParent instanceof DWMetsDivTreeNode) {
						// move fptr
						DWMetsTreeNode newNode = getDWMetsTreeModel().move(
								fptrNode, (DWMetsDivTreeNode) newParent);
						newNodes = newNode != null ? new DWMetsTreeNode[] { newNode }
								: null;
					} else {
						// move fptr and set position
						int index = getDWMetsTreeModel().getIndexOfChild(
								newParent.getParent(), newParent);
						DWMetsTreeNode newNode = getDWMetsTreeModel().move(
								fptrNode,
								(DWMetsDivTreeNode) newParent.getParent(),
								index + 1);
						newNodes = newNode != null ? new DWMetsTreeNode[] { newNode }
								: null;
					}
				} else {
					e.rejectDrop();
				}

			}
			// is it a file list?
			else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
					&& newParent instanceof DWMetsDivTreeNode) {
				logger.debug("java file list flavor");
				e.acceptDrop(DnDConstants.ACTION_COPY);
				List<File> files = DragNDropHelper.filterFiles((List<File>) tr
						.getTransferData(DataFlavor.javaFileListFlavor));
				newNodes = new DWMetsTreeNode[files.size()];
				int i = 0;
				for (File file : files) {
					newNodes[i++] = addFile(file, (DWMetsDivTreeNode) newParent);
				}
				repPanel.getRepresentationObject().setSubType(
						repPanel.getSuggestedSubType());
			} else {
				// Is it a file list in Linux (KDE/Gnome)?
				DataFlavor[] flavors = tr.getTransferDataFlavors();
				File[] files = null;
				for (int zz = 0; zz < flavors.length; zz++) {
					if (flavors[zz].isRepresentationClassReader()) {
						e.acceptDrop(DnDConstants.ACTION_COPY);
						Reader reader = flavors[zz].getReaderForText(tr);
						BufferedReader br = new BufferedReader(reader);

						files = Tools.createFileArray(br);

						// Mark that drop is completed.
						e.getDropTargetContext().dropComplete(true);
						break;
					}
				}

				if (files != null) {
					logger.debug("java file list in Linux (KDE/Gnome)");
					List<File> fileList = DragNDropHelper.filterFiles(Arrays
							.asList(files));
					e.acceptDrop(DnDConstants.ACTION_COPY);
					newNodes = new DWMetsTreeNode[fileList.size()];
					int i = 0;
					for (File file : fileList) {
						newNodes[i++] = addFile(file,
								(DWMetsDivTreeNode) newParent);
					}
					repPanel.getRepresentationObject().setSubType(
							repPanel.getSuggestedSubType());
				}
				// is it a string?
				else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)
						&& newParent instanceof DWMetsDivTreeNode) {
					logger.debug("string flavor");
					e.acceptDrop(DnDConstants.ACTION_COPY);
					// get source fptr node
					String divId = (String) tr
							.getTransferData(DataFlavor.stringFlavor);
					DWMetsTreeNode newNode = getDWMetsTreeModel().addSection(
							(DWMetsDivTreeNode) newParent, divId);
					newNodes = newNode != null ? new DWMetsTreeNode[] { newNode }
							: null;
				} else {
					e.rejectDrop();
				}
			}

			if (newNodes != null) {
				e.getDropTargetContext().dropComplete(true);

				for (DWMetsTreeNode newNode : newNodes) {
					if (newNode != null) {
						TreePath parentPath = new TreePath(getDWMetsTreeModel()
								.getPathToRoot(newNode));
						// expandPath(parentPath);
						setSelectionPath(parentPath);
					} else {
						logger.error("Found null node");
					}
				}
			} else {
				e.rejectDrop();
			}
		} catch (IOException io) {
			e.rejectDrop();
		} catch (UnsupportedFlavorException ufe) {
			e.rejectDrop();
		}

		if (highlightNode != null) {
			highlightNode.setDragOver(false);
			repaint();
		}

	}

	private DWMetsTreeNode addFile(File file, DWMetsDivTreeNode parent) {
		DWMetsTreeNode newNode = null;
		if (file.isDirectory()) {
			DWMetsDivTreeNode newDivNode = getDWMetsTreeModel().addSection(
					parent, file.getName());
			for (File subFile : file.listFiles()) {
				if (DragNDropHelper.isFileAllowed(subFile)) {
					addFile(subFile, newDivNode);
				}
			}
			newNode = newDivNode;
		} else {

			newNode = getDWMetsTreeModel().addFile(parent, file);
		}
		return newNode;
	}

	/**
	 * Drop Action Changed listener action
	 * 
	 * @param arg0
	 */
	public void dropActionChanged(DropTargetDragEvent arg0) {
		// nothing to do

	}

	/**
	 * Drag Drop End listener action
	 * 
	 * @param arg0
	 */
	public void dragDropEnd(DragSourceDropEvent arg0) {
		// nothing to do
	}

	/**
	 * Drag Enter listener action
	 * 
	 * @param arg0
	 */
	public void dragEnter(DragSourceDragEvent arg0) {
		// nothing to do

	}

	/**
	 * Drag Exit listener action
	 * 
	 * @param arg0
	 */
	public void dragExit(DragSourceEvent arg0) {
		// nothing to do

	}

	/**
	 * Drag Over listener action
	 * 
	 * @param arg0
	 */
	public void dragOver(DragSourceDragEvent arg0) {
		// nothing to do

	}

	/**
	 * Drop Action Changed listener action
	 * 
	 * @param arg0
	 */
	public void dropActionChanged(DragSourceDragEvent arg0) {
		// nothing to do

	}

	@SuppressWarnings("unchecked")
	private boolean testDrop(TreePath destination, Transferable tr) {
		boolean valid;
		try {
			if (tr.isDataFlavorSupported(DWMetsDivTreeNode.DIV_NODE_FLAVOR)) {
				DWMetsDivTreeNode source = (DWMetsDivTreeNode) tr
						.getTransferData(DWMetsDivTreeNode.DIV_NODE_FLAVOR);
				valid = testDrop(destination, source);

			} else if (tr
					.isDataFlavorSupported(DWMetsFptrTreeNode.FTPR_NODE_FLAVOR)) {
				DWMetsFptrTreeNode source = (DWMetsFptrTreeNode) tr
						.getTransferData(DWMetsFptrTreeNode.FTPR_NODE_FLAVOR);
				valid = testDrop(destination, source);

			} else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<File> files = (List<File>) tr
						.getTransferData(DataFlavor.javaFileListFlavor);
				valid = testDrop(destination, files);

			} else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String divId = (String) tr
						.getTransferData(DataFlavor.stringFlavor);
				valid = testDrop(destination, divId);

			} else {
				valid = false;
			}
		} catch (UnsupportedFlavorException e) {
			valid = false;
		} catch (IOException e) {
			valid = false;
		}
		return valid;
	}

	private boolean testDrop(TreePath destination, DWMetsTreeNode source) {
		boolean valid;
		TreePath sourcePath = new TreePath(getDWMetsTreeModel().getPathToRoot(
				source));
		String msg = testDropWithTreeSource(destination, sourcePath);
		if (msg != null) {
			showError(msg);
			valid = false;
		} else {
			clearErrors();
			valid = true;
		}
		return valid;
	}

	private boolean testDrop(TreePath destination, List<File> files) {
		boolean valid;
		String msg = testDropDestination(destination);
		if (msg != null) {
			showError(msg);
			valid = false;
		} else {
			clearErrors();
			valid = true;
		}
		return valid;
	}

	private boolean testDrop(TreePath destination, String divId) {
		boolean valid;
		String msg = testDropDestination(destination);
		if (msg != null) {
			showError(msg);
			valid = false;
		} else {
			clearErrors();
			valid = true;
		}
		return valid;
	}

	private void showError(final String msg) {
		SIPCreator.setStatusMessage("Cannot drag there: " + msg);
	}

	private void clearErrors() {
		SIPCreator.setDefaultStatusMessage();
	}

	private String testDropDestination(TreePath destination) {
		String msg = null;
		boolean destinationPathIsNull = destination == null;
		if (destinationPathIsNull) {
			msg = "Invalid drop location.";
		} else {
			DWMetsTreeNode destinationNode = (DWMetsTreeNode) destination
					.getLastPathComponent();
			if (!destinationNode.getAllowsChildren())
				msg = "This node does not allow children";

		}
		return msg;
	}

	private String testDropWithTreeSource(TreePath destination, TreePath source) {
		String msg = null;

		boolean destinationPathIsNull = destination == null;
		if (destinationPathIsNull) {
			msg = "Invalid drop location.";
		} else if (destination.equals(source)) {
			msg = "Destination cannot be same as source";
		}

		else if (source.isDescendant(destination)) {
			msg = "Destination node cannot be a descendant";
		} else {
			DWMetsTreeNode destinationNode = (DWMetsTreeNode) destination
					.getLastPathComponent();
			DWMetsTreeNode sourceNode = (DWMetsTreeNode) source
					.getLastPathComponent();
			if (destinationNode.isLeaf() && !sourceNode.isLeaf()) {
				msg = "Cannot move a section under a file";
			}
		}
		return msg;
	}

	/**
	 * Get auto scroll insets
	 * 
	 * @return the insets
	 */
	public Insets getAutoscrollInsets() {
		return ((JViewport) getParent()).getInsets();
	}

}
