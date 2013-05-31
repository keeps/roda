/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
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
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;

/**
 * @author Luis Faria
 * 
 */
public class FondsTree extends JTree implements TreeSelectionListener,
		DragGestureListener, DropTargetListener, DragSourceListener, Autoscroll {

	private static final long serialVersionUID = -2723382504248995613L;
	private static final Logger logger = Logger.getLogger(FondsTree.class);

	/** Stores the selected node info */
	protected TreePath selectedTreePath = null;
	protected FondsTreeNode selectedNode = null;

	/** Variables needed for DnD */
	private DragSource dragSource = null;

	private final FondsTreeModel model;

	/**
	 * Create a new digitalized work Mets tree
	 * 
	 * @param model
	 */
	public FondsTree(FondsTreeModel model) {
		super(model);
		this.model = model;
		this.setCellRenderer(new FondsTreeCellRenderer());
		setRootVisible(false);
		setShowsRootHandles(true);

		addTreeSelectionListener(this);

		dragSource = DragSource.getDefaultDragSource();

		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_COPY_OR_MOVE, this);
		dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
		new DropTarget(this, this);

	}

	/**
	 * Get digitalized work Mets tree model
	 * 
	 * @return the digitalized work Mets tree model
	 */
	private FondsTreeModel getFondsTreeModel() {
		return model;
	}

	/**
	 * Get selected node
	 * 
	 * @return the selected node
	 */
	public FondsTreeNode getSelectedNode() {
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
		selectedNode = (FondsTreeNode) selectedTreePath.getLastPathComponent();

	}

	/**
	 * Drag gesture recognized listener action
	 * 
	 * @param dge
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		// Get the selected node
		FondsTreeNode dragNode = getSelectedNode();
		if (dragNode != null) {
			Object userObject = dragNode.getUserObject();
			if (userObject instanceof SIP) {
				SIP sip = (SIP) userObject;

				// Get the Transferable Object
				Transferable transferable = sip;

				// begin the drag
				dragSource.startDrag(dge, null, transferable, this);
			}
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

	private FondsTreeNode highlightNode = null;

	private void highlightDestination(TreePath path, boolean isBackground) {
		if (path != null) {
			Object destination = path.getLastPathComponent();
			if (path.getLastPathComponent() instanceof FondsTreeNode) {
				FondsTreeNode fondsNode = (FondsTreeNode) destination;
				if (highlightNode != null && highlightNode != fondsNode) {
					highlightNode.setDragOver(false);
				}
				fondsNode.setDragOver(true);
				highlightNode = fondsNode;
				repaint();
			}

		} else if (isBackground) {
			FondsTreeNode fondsNode = getFondsTreeModel().getRoot();
			if (highlightNode != null && highlightNode != fondsNode) {
				highlightNode.setDragOver(false);
			}
			fondsNode.setDragOver(true);
			highlightNode = fondsNode;
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
	public void drop(DropTargetDropEvent e) {
		try {
			Transferable tr = e.getTransferable();
			FondsTreeNode newNode = null;

			// get new parent node
			Point loc = e.getLocation();
			TreePath destination = getPathForLocation(loc.x, loc.y);
			if (destination == null && getParent().contains(loc)) {
				destination = new TreePath(new TreeNode[] { getFondsTreeModel()
						.getRoot() });
				setSelectionPath(destination);
			}

			FondsTreeNode newParent = (FondsTreeNode) destination
					.getLastPathComponent();

			// is it a SIP?
			if (tr.isDataFlavorSupported(SIP.SIP_FLAVOR)) {
				logger.debug("SIP node flavor");
				// get source SIP
				SIP sip = (SIP) tr.getTransferData(SIP.SIP_FLAVOR);
				// test drop target
				if (testDrop(destination, sip)) {
					if (!sip.confirmChange()) {
						e.rejectDrop();
					} else if (newParent.getUserObject() instanceof SIPDescriptionObject) {
						// moving SIP under a SIP Description Object is forbidden
						e.rejectDrop();
					}
					else if (newParent.getUserObject() instanceof DescriptionObject) {
						DescriptionObject descObj = (DescriptionObject) newParent
								.getUserObject();
						// move SIP under new node
						newNode = getFondsTreeModel().move(sip,
								descObj.getPid());
					} else if (newParent.getUserObject() instanceof SIP) {
						SIP sibling = (SIP) newParent.getUserObject();
						// move SIP to be sibling
						newNode = getFondsTreeModel().move(sip,
								sibling.getParentPID());
					} else {
						e.rejectDrop();
					}
				}

			} else {
				e.rejectDrop();
			}

			if (newNode != null) {
				try {
					e.acceptDrop(DnDConstants.ACTION_MOVE);
				} catch (java.lang.IllegalStateException ils) {
					e.rejectDrop();
				}

				e.getDropTargetContext().dropComplete(true);
				TreePath parentPath = new TreePath(getFondsTreeModel()
						.getPathToRoot(newNode));
				expandPath(parentPath);
				setSelectionPath(parentPath);
			} else {
				e.rejectDrop();
			}
		} catch (IOException io) {
			e.rejectDrop();
		} catch (UnsupportedFlavorException ufe) {
			e.rejectDrop();
		} catch (SIPException sipe) {
			e.rejectDrop();
		}

		if (highlightNode != null) {
			highlightNode.setDragOver(false);
			repaint();
		}

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

	private boolean testDrop(TreePath destination, Transferable tr) {
		boolean valid;
		try {
			if (tr.isDataFlavorSupported(SIP.SIP_FLAVOR)) {
				SIP source = (SIP) tr.getTransferData(SIP.SIP_FLAVOR);
				if (destination != null) {
					Object node = destination.getLastPathComponent();
					if (node instanceof FondsTreeNode) {
						FondsTreeNode fondsNode = (FondsTreeNode) node;
						if (fondsNode.getUserObject() instanceof DescriptionObject) {
							DescriptionObject descObj = (DescriptionObject) fondsNode
									.getUserObject();
							valid = !source.getParentPID().equals(
									descObj.getPid());
						} else if (fondsNode.getUserObject() instanceof SIP) {
							SIP sibling = (SIP) fondsNode.getUserObject();
							valid = !source.getParentPID().equals(
									sibling.getParentPID());
						} else {
							valid = false;
						}

					} else {
						valid = false;
					}
				} else {
					valid = false;
				}

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

	/**
	 * Get auto scroll insets
	 * 
	 * @return the insets
	 */
	public Insets getAutoscrollInsets() {
		return ((JViewport) getParent()).getInsets();
	}

	class FondsTreeCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = -3384681573197356288L;

		private final Icon fondsIcon;
		private final Icon subFondsIcon;
		private final Icon classIcon;
		private final Icon subClassIcon;
		private final Icon seriesIcon;
		private final Icon subSeriesIcon;
		private final Icon fileIcon;
		private final Icon itemIcon;

		public FondsTreeCellRenderer() {
			fondsIcon = Tools.createImageIcon("descriptionLevel/fonds.png");
			subFondsIcon = Tools
					.createImageIcon("descriptionLevel/subfonds.png");
			classIcon = Tools.createImageIcon("descriptionLevel/class.png");
			subClassIcon = Tools
					.createImageIcon("descriptionLevel/subclass.png");
			seriesIcon = Tools.createImageIcon("descriptionLevel/series.png");
			subSeriesIcon = Tools
					.createImageIcon("descriptionLevel/subseries.png");
			fileIcon = Tools.createImageIcon("descriptionLevel/file.png");
			itemIcon = Tools.createImageIcon("descriptionLevel/item.png");
			
			

		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			setIcon(getIcon(value));
			setText(getText(value));

			return this;
		}

		protected Icon getIcon(Object value) {
			Icon ret = null;
			FondsTreeNode node = (FondsTreeNode) value;
			if (node.getUserObject() instanceof DescriptionObject) {
				DescriptionObject obj = (DescriptionObject) node
						.getUserObject();
				if (DescriptionLevel.FONDS.equals(obj.getLevel())) {
					ret = fondsIcon;
				} else if (DescriptionLevel.SUBFONDS.equals(obj.getLevel())) {
					ret = subFondsIcon;
				} else if (DescriptionLevel.CLASS.equals(obj.getLevel())) {
					ret = classIcon;
				} else if (DescriptionLevel.SUBCLASS.equals(obj.getLevel())) {
					ret = subClassIcon;
				} else if (DescriptionLevel.SERIES.equals(obj.getLevel())) {
					ret = seriesIcon;
				} else if (DescriptionLevel.SUBSERIES.equals(obj.getLevel())) {
					ret = subSeriesIcon;
				} else if (DescriptionLevel.FILE.equals(obj.getLevel())) {
					ret = fileIcon;
				} else if (DescriptionLevel.ITEM.equals(obj.getLevel())) {
					ret = itemIcon;
				}
			} else if (node.getUserObject() instanceof SIP) {
				SIP sip = (SIP) node.getUserObject();
				ret = sip.getIcon();
			}
			return ret;
		}

		protected String getText(Object value) {
			String text = null;
			FondsTreeNode node = (FondsTreeNode) value;
			if (node.getUserObject() instanceof DescriptionObject) {
				DescriptionObject obj = (DescriptionObject) node
						.getUserObject();
				if (obj.getId() == null) {
					text = Messages.getString("FondsTree.sipdo.NEW");
				} else {
					text = obj.getId();
				}
			} else if (node.getUserObject() instanceof SIP) {
				SIP sip = (SIP) node.getUserObject();
				if (sip.getDescriptionObject().getId() == null) {
					text = Messages.getString("FondsTree.sip.NEW");
				} else {
					text = sip.getDescriptionObject().getId();
				}
			}
			return text;
		}
	}
}
