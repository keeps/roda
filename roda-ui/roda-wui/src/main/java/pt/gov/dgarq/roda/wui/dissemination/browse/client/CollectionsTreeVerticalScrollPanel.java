/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import org.roda.index.sorter.Sorter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;

/**
 * @author Luis Faria
 * 
 */
public class CollectionsTreeVerticalScrollPanel extends ElementVerticalScrollPanel implements SourcesClickEvents {

	private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

	/**
	 * Default scroll block size
	 */
	public static int DEFAULT_SCROLL_BLOCK_SIZE = 10;

	/**
	 * Default scroll maximum size
	 */
	public static int DEFAULT_SCROLL_MAX_SIZE = 1000;

	/**
	 * Default tree limit
	 */
	public static int DEFAULT_TREE_LIMIT = 30;

	/**
	 * Default filter
	 */
	public static final Filter DEFAULT_FILTER = null;

	/**
	 * Default sorter
	 */
	public static final Sorter DEFAULT_SORTER = null;

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final int treeLimit;

	private CollectionsProxy cache;

	private final Vector<CollectionsTree> trees;

	private boolean showInfo;

	private CollectionsTreeItem selectedItem;

	private final List<ClickListener> listeners;

	/**
	 * Create a new collection tree vertical scroll panel
	 * 
	 * @param showInfo
	 *            where to show extended info or not
	 */
	public CollectionsTreeVerticalScrollPanel(boolean showInfo) {
		this(DEFAULT_FILTER, DEFAULT_SORTER, DEFAULT_SCROLL_BLOCK_SIZE, DEFAULT_SCROLL_MAX_SIZE, DEFAULT_TREE_LIMIT,
				showInfo);
	}

	/**
	 * Create a new collection tree vertical scroll panel
	 * 
	 * @param filter
	 *            Collections Tree filter, which filters every Description
	 *            Object in tree
	 * @param sorter
	 *            Collections Tree sorter, which sorts every level of the tree
	 * 
	 * @param showInfo
	 *            where to show extended info or not
	 */
	public CollectionsTreeVerticalScrollPanel(Filter filter, Sorter sorter, boolean showInfo) {
		this(filter, sorter, DEFAULT_SCROLL_BLOCK_SIZE, DEFAULT_SCROLL_MAX_SIZE, DEFAULT_TREE_LIMIT, showInfo);
	}

	/**
	 * Create a new collection tree vertical scroll panel
	 * 
	 * @param filter
	 *            Collections Tree filter
	 * @param sorter
	 *            Collections Tree sorter
	 * 
	 * @param scrollBlockSize
	 *            the scroll fetch block size
	 * @param scrollMaxSize
	 *            the scroll maximum size
	 * @param treeLimit
	 *            the tree maximum number of children to show
	 * @param showInfo
	 *            where to show extended info
	 */
	public CollectionsTreeVerticalScrollPanel(Filter filter, Sorter sorter, int scrollBlockSize, int scrollMaxSize,
			int treeLimit, boolean showInfo) {
		this(filter, sorter, new CollectionsProxy(filter, sorter), scrollBlockSize, scrollMaxSize, treeLimit, showInfo);
	}

	private CollectionsTreeVerticalScrollPanel(Filter filter, Sorter sorter, CollectionsProxy proxy,
			int scrollBlockSize, int scrollMaxSize, int treeLimit, boolean showInfo) {
		super(filter, sorter, proxy, scrollBlockSize, scrollMaxSize);
		this.cache = proxy;
		this.treeLimit = treeLimit;
		this.trees = new Vector<CollectionsTree>();
		this.showInfo = showInfo;
		this.selectedItem = null;
		this.listeners = new Vector<ClickListener>();
	}

	protected Widget createWidget(SimpleDescriptionObject sdo, int position) {
		CollectionsTree tree = new CollectionsTree(sdo, getFilter(), getSorter(), treeLimit, showInfo);
		trees.insertElementAt(tree, position);
		tree.addTreeListener(new TreeListener() {

			public void onTreeItemSelected(TreeItem item) {
				// click focus shortcut
				if (item instanceof CollectionsTreeItem) {
					if (selectedItem != item) {
						if (selectedItem != null) {
							selectedItem.setSelected(false);
							logger.debug("Deselecting item " + selectedItem.getPid());
						}
						selectedItem = (CollectionsTreeItem) item;
						logger.debug("Selecting item " + selectedItem.getPid() + " (shortcut)");
					} else {
						logger.debug("same element selected");
					}
					onClick(selectedItem.getWidget());
				}
			}

			public void onTreeItemStateChanged(TreeItem item) {
			}

		});
		return tree;
	}

	protected void removeWidget(Widget tree) {
		trees.remove(tree);
	}

	/**
	 * Get the total number of collections
	 * 
	 * @param callback
	 */
	public void getCount(AsyncCallback<Integer> callback) {
		cache.getCount(callback);
	}

	/**
	 * Show object extended information
	 * 
	 * @param showInfo
	 */
	public void setShowInfo(boolean showInfo) {
		if (this.showInfo != showInfo) {
			this.showInfo = showInfo;
			for (CollectionsTree tree : trees) {
				tree.setShowInfo(showInfo);
			}
			if (showInfo) {
				removeStyleDependentName("noInfo");
			} else {
				addStyleDependentName("noInfo");
			}
		}
	}

	/**
	 * Ensure a collection is loaded in the tree vertical panel
	 * 
	 * @param collectionPID
	 *            the collection PID
	 * @param callback
	 */
	public void ensureLoaded(final String collectionPID, final AsyncCallback<CollectionsTree> callback) {

		// TODO fix this
		// BrowserService.Util.getInstance().getCollectionIndex(collectionPID,
		// getFilter(), getSorter(),
		// new AsyncCallback<Integer>() {
		//
		// public void onFailure(Throwable caught) {
		// callback.onFailure(caught);
		// }
		//
		// public void onSuccess(final Integer index) {
		// logger.debug("tree at index " + index);
		// if (index >= 0) {
		// getScroll().ensureLoaded(index, getBlockSize(), new
		// AsyncCallback<Integer>() {
		//
		// public void onFailure(Throwable caught) {
		// callback.onFailure(caught);
		// }
		//
		// public void onSuccess(Integer result) {
		// CollectionsTree tree = (CollectionsTree) trees
		// .get(index - getScroll().getWindowOffset());
		// // getScroll().ensureVisible(tree);
		// callback.onSuccess(tree);
		// }
		//
		// });
		// } else {
		// logger.error("Index of collection " + collectionPID + " not found!");
		// }
		//
		// }
		//
		// });

	}

	/**
	 * Focus on a path of objects, expanding tree and selected the last.
	 * 
	 * @param path
	 */
	public void focusOn(final String[] path) {
		logger.debug("focusing on " + Tools.toString(path));
		ensureLoaded(path[0], new AsyncCallback<CollectionsTree>() {

			public void onFailure(Throwable caught) {
				logger.error("Error ensuring loading of tree " + path[0], caught);
			}

			public void onSuccess(CollectionsTree tree) {
				if (tree != null) {
					if (tree.getRoot().getId().equals(path[0])) {
						tree.focusOn(Tools.tail(path), new AsyncCallback<CollectionsTreeItem>() {

							public void onFailure(Throwable caught) {
								logger.error("Error focusing on " + Tools.toString(path), caught);
							}

							public void onSuccess(CollectionsTreeItem item) {
								if (selectedItem != item) {
									if (selectedItem != null) {
										selectedItem.setSelected(false);
										logger.debug("Deselecting item " + selectedItem.getPid());
									}
									selectedItem = (CollectionsTreeItem) item;
									getScroll().ensureVisible(selectedItem);
									logger.debug("Selecting item " + selectedItem.getPid());
								}
							}

						});
					} else {
						// Tree is considered inconsistent
						// Deep reload the tree and retry focus
						clear(new AsyncCallback<Integer>() {

							public void onFailure(Throwable caught) {
								logger.error("Error clearing tree after focus " + "giving consistency error", caught);
							}

							public void onSuccess(Integer result) {
								focusOn(path);
							}

						});
					}
				}
			}

		});
	}

	/**
	 * Select an object. If the object is already selected nothing is done.
	 * 
	 * @param pid
	 *            The object PID or null to remove focus from current selected
	 *            object.
	 */
	public void setSelected(final String pid) {
		if (pid == null) {
			selectedItem.setSelected(false);
			selectedItem = null;
		} else if (selectedItem != null && selectedItem.getPid().equals(pid)) {
			// getScroll().ensureVisible(selectedItem);
		} else {
			logger.debug("focusing on " + pid);
			BrowserService.Util.getInstance().getAncestors(pid, new AsyncCallback<String[]>() {

				public void onFailure(Throwable caught) {
					if (caught instanceof NoSuchRODAObjectException) {
						Window.alert(messages.noSuchRODAObject(pid));
						Browse.getInstance().view(null);
					} else {
						logger.error("could not get ancestors of " + pid, caught);
					}
				}

				public void onSuccess(String[] path) {
					if (path != null && path.length > 0) {
						focusOn(path);
					} else {
						logger.warn("path of " + pid + " was null or empty");
					}

				}

			});
		}
	}

	/**
	 * Update an object information and/or hierarchy
	 * 
	 * @param pid
	 *            the object pid
	 * @param info
	 *            true to update object information
	 * @param hierarchy
	 *            true to update object hierarchy
	 * @param callback
	 *            Handle the updated collections tree item
	 */
	public void update(final String pid, final boolean info, final boolean hierarchy,
			final AsyncCallback<CollectionsTreeItem> callback) {
		if (pid == null) {
			logger.debug("clearing tree vertical panel");
			clear(new AsyncCallback<Integer>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(Integer result) {
					callback.onSuccess(null);
				}

			});
		} else {
			BrowserService.Util.getInstance().getAncestors(pid, new AsyncCallback<String[]>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(String[] path) {
					if (path != null) {
						if (path.length == 1) {
							logger.debug("updating tree root " + path[0]);
							updateTreeRoot(path[0], info, hierarchy, callback);
						} else {
							logger.debug("updating tree recursively");
							updateTreeRecursively(path, info, hierarchy, callback);
						}
					} else {
						logger.debug("WARNING: ancestor where null of " + pid);
					}

				}

			});
		}
	}

	private void updateTreeRoot(final String rootPID, final boolean info, final boolean hierarchy,
			final AsyncCallback<CollectionsTreeItem> callback) {
		cache.clear(rootPID, new AsyncCallback<SimpleDescriptionObject>() {

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(final SimpleDescriptionObject sdo) {
				ensureLoaded(rootPID, new AsyncCallback<CollectionsTree>() {

					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(CollectionsTree tree) {
						if (tree != null) {
							logger.debug("Setting root sdo");
							tree.getRootItem().setSDO(sdo);
							if (info) {
								logger.debug("Updating root info");
								tree.getRootItem().updateInfo();
							}
							if (hierarchy) {
								logger.debug("Updating root hierarchy");
								tree.getRootItem().updateHierarchy();
							}
							callback.onSuccess(tree.getRootItem());
						} else {
							callback.onFailure(new RuntimeException(
									"Couldn't " + "update tree root because ensureLoaded " + "didn't return a tree"));
						}

					}
				});
			}

		});
	}

	private void updateTreeRecursively(final String[] path, final boolean info, final boolean hierarchy,
			final AsyncCallback<CollectionsTreeItem> callback) {
		ensureLoaded(path[0], new AsyncCallback<CollectionsTree>() {

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(CollectionsTree tree) {
				if (tree != null) {
					logger.debug("calling tree recursive update");
					tree.update(Tools.tail(path), info, hierarchy, callback);
				}
			}
		});

	}

	public void clear(final AsyncCallback<Integer> callback) {
		if (trees != null) {
			trees.clear();
		}
		super.clear(callback);
	}

	protected void onClick(Widget sender) {
		for (ClickListener listener : listeners) {
			listener.onClick(sender);
		}
	}

	/**
	 * Get the current selected item
	 * 
	 * @return The selected item or null if none selected
	 */
	public CollectionsTreeItem getSelected() {
		return selectedItem;
	}

	public void addClickListener(ClickListener listener) {
		listeners.add(listener);
	}

	public void removeClickListener(ClickListener listener) {
		listeners.remove(listener);
	}

}
