/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import org.roda.index.filter.Filter;
import org.roda.index.sorter.Sorter;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;

import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTreeItem.JumpCollectionsTreeItem;

/**
 * @author Luis Faria
 * 
 */
public class CollectionsTree extends Tree {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private SimpleDescriptionObject root;

	private Filter filter;

	private Sorter sorter;

	private int childrenVisibleCount;

	private CollectionsTreeItem rootItem;

	private boolean showInfo;

	/**
	 * Create a new collections tree
	 * 
	 * @param root
	 *            the collection root description object
	 * @param filter
	 *            the filter to use in the description object listing
	 * @param sorter
	 *            the sorter to use in the description object listing
	 * @param childrenVisibleCount
	 *            the maximum number of children to show under a node
	 * @param showInfo
	 *            where to show extended info
	 */
	public CollectionsTree(SimpleDescriptionObject root, Filter filter, Sorter sorter, int childrenVisibleCount,
			boolean showInfo) {
		this.filter = filter;
		this.sorter = sorter;
		this.childrenVisibleCount = childrenVisibleCount;
		this.showInfo = showInfo;

		this.addTreeListener(new TreeListener() {

			public void onTreeItemSelected(TreeItem item) {
				if (item instanceof JumpCollectionsTreeItem) {
					((JumpCollectionsTreeItem) item).jump();
				}
			}

			public void onTreeItemStateChanged(TreeItem item) {
				CollectionsTreeItem fondsItem = (CollectionsTreeItem) item;
				if (fondsItem.getState()) {
					fondsItem.init();
					// fondsItem.setSliderVisible(true);
				} else {
					// fondsItem.setSliderVisible(false);
				}
			}

		});

		if (root == null) {
			logger.error("Tryed to create a CollectionsTree with null root");
		} else {
			logger.debug("Setting root " + root.getId());
			setRoot(root);
		}

		this.addStyleName("collection-tree");
	}

	/**
	 * Get tree root
	 * 
	 * @return the simple description object of this tree root
	 */
	public SimpleDescriptionObject getRoot() {
		return root;
	}

	/**
	 * Set tree root
	 * 
	 * @param root
	 */
	public void setRoot(SimpleDescriptionObject root) {
		if (this.root != root) {
			this.root = root;
			this.rootItem = new CollectionsTreeItem(root, getFilter(), getSorter(), childrenVisibleCount, showInfo);
			this.removeItems();
			this.addItem(rootItem);
		}
	}

	/**
	 * Add an item
	 * 
	 * @param item
	 */
	public void addItem(CollectionsTreeItem item) {
		super.addItem(item);
		Element itemContainer = DOM.getParent(DOM.getParent(item.getWidget().getElement()));
		DOM.setElementProperty(itemContainer, "width", "100%");
	}

	/**
	 * Get current children visible count
	 * 
	 * @return the number of (maximum) visible items under this node tree
	 */
	public int getChildrenVisibleCount() {
		return childrenVisibleCount;
	}

	/**
	 * Set current (maximum) visible items under this node tree
	 * 
	 * @param childrenVisibleCount
	 */
	public void setChildrenVisibleCount(int childrenVisibleCount) {
		this.childrenVisibleCount = childrenVisibleCount;
		rootItem.setChildrenVisibleCount(childrenVisibleCount);
	}

	/**
	 * Set if extended information should be shown
	 * 
	 * @param showInfo
	 */
	public void setShowInfo(boolean showInfo) {
		if (this.showInfo != showInfo) {
			this.showInfo = showInfo;
			rootItem.setShowInfo(showInfo);
		}

	}

	/**
	 * Focus on a path
	 * 
	 * @param path
	 * @param callback
	 */
	public void focusOn(String[] path, AsyncCallback<CollectionsTreeItem> callback) {
		rootItem.focusOn(path, callback);
	}

	public void setFocus(boolean inFocus) {
		logger.debug("CollectionsTree set focus " + inFocus + " intercepted");
		// XXX this is a workarround to prevent focus jumping to top in FireFox
	}

	/**
	 * Update the tree
	 * 
	 * @param path
	 *            the tree node path to update
	 * @param info
	 *            if tree node information should be updated
	 * @param hierarchy
	 *            if the tree node hierarchy should be updated
	 * @param callback
	 *            callback after the update with the updated tree item
	 */
	public void update(String[] path, boolean info, boolean hierarchy, AsyncCallback<CollectionsTreeItem> callback) {
		rootItem.update(path, info, hierarchy, callback);
	}

	/**
	 * Get root item
	 * 
	 * @return the collection tree root item
	 */
	public CollectionsTreeItem getRootItem() {
		return rootItem;
	}

	/**
	 * Get current filter
	 * 
	 * @return {@link Filter}
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * Set filter
	 * 
	 * @param filter
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
		getRootItem().setFilter(filter);
	}

	/**
	 * Get current sorter
	 * 
	 * @return {@link Sorter}
	 */
	public Sorter getSorter() {
		return sorter;
	}

	/**
	 * Set sorter
	 * 
	 * @param sorter
	 */
	public void setSorter(Sorter sorter) {
		this.sorter = sorter;
		getRootItem().setSorter(sorter);
	}

}
