/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.NoSuchElementException;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

import config.i18n.client.BrowseConstants;
import config.i18n.client.BrowseMessages;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.images.BrowseImageBundle;

/**
 * @author Luis Faria
 * 
 */
public class CollectionsTreeItem extends TreeItem {

	private static BrowseConstants constants = (BrowseConstants) GWT.create(BrowseConstants.class);

	private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

	private static BrowseImageBundle browseImageBundle = (BrowseImageBundle) GWT.create(BrowseImageBundle.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private boolean initialized;

	private boolean initialising;

	private int childrenCount = -1;

	private int childrenVisibleCount;

	private int currentOffset;

	private SimpleDescriptionObject sdo;

	private final SubElementsProxy memCache;

	private TreeItemPanel itemPanel;

	private final TreeItem dummy;

	private final JumpCollectionsTreeItem previousItem;

	private final JumpCollectionsTreeItem nextItem;

	private boolean showInfo;

	private int waitingToLoad;

	private Filter filter;
	private Sorter sorter;

	/**
	 * Create a new collection tree item
	 * 
	 * @param sdo
	 *            the simple description object of this item
	 * @param filter
	 * @param sorter
	 * @param childrenVisibleCount
	 *            the maximum number of children to show
	 * @param showInfo
	 *            where to show extended info
	 */
	public CollectionsTreeItem(SimpleDescriptionObject sdo, Filter filter, Sorter sorter, int childrenVisibleCount,
			boolean showInfo) {
		this.childrenVisibleCount = childrenVisibleCount;
		this.sdo = sdo;
		this.filter = filter;
		this.sorter = sorter;
		this.memCache = new SubElementsProxy(sdo, filter, sorter);
		this.showInfo = showInfo;
		this.itemPanel = new TreeItemPanel(sdo, childrenVisibleCount, showInfo);
		this.setWidget(itemPanel);

		waitingToLoad = 0;

		itemPanel.addSliderEventListener(new SliderEventListener() {

			public void onSliderMove(int value, int size) {
				setCurrentOffset(value);
			}

		});

		this.currentOffset = 0;
		this.initialized = false;
		this.initialising = false;

		dummy = new TreeItem();
		dummy.setText(constants.treeLoading());

		logger.debug(sdo.getId() + " has " + sdo.getSubElementsCount() + " children");

		getChildrenCount(new AsyncCallback<Integer>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting children count", caught);
			}

			public void onSuccess(Integer count) {
				if (count > 0) {
					initialized = false;
					addItem(dummy);
				} else {
					initialized = true;

				}

			}

		});

		previousItem = new JumpCollectionsTreeItem(false);
		nextItem = new JumpCollectionsTreeItem(true);

		previousItem.setVisible(false);
		nextItem.setVisible(false);

		dummy.addStyleName("TreeItem-dummy");

	}

	private void getChildrenCount(final AsyncCallback<Integer> callback) {
		if (childrenCount < 0) {
			if (filter == null || filter.getParameters().length == 0) {
				childrenCount = sdo.getSubElementsCount();
				callback.onSuccess(childrenCount);

			} else {
				BrowserService.Util.getInstance().getSubElementsCount(sdo.getId(), filter,
						new AsyncCallback<Integer>() {

							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}

							public void onSuccess(Integer result) {
								childrenCount = result;
								callback.onSuccess(childrenCount);
							}

						});
			}
		} else {
			callback.onSuccess(childrenCount);
		}
	}

	private void ensureLoaded(AsyncCallback<Integer> callback) {
		memCache.ensureLoaded(callback);
	}

	/**
	 * Initialize the collection tree item
	 */
	public void init() {
		if (!initialized && !initialising) {
			initialized = true;
			initialising = true;
			logger.debug("Init calls load(" + currentOffset + ")");
			removeItems();
			load(currentOffset, null);
		}
	}

	/**
	 * Get current children offset
	 * 
	 * @return current offset
	 */
	public int getCurrentOffset() {
		return currentOffset;
	}

	/**
	 * Set current children offset
	 * 
	 * @param currentOffset
	 */
	public void setCurrentOffset(int currentOffset) {
		if (this.currentOffset != currentOffset) {
			logger.debug("setCurrentOffset calls load(" + currentOffset + ")");
			load(currentOffset, null);
		}
	}

	/**
	 * Get current visible children count
	 * 
	 * @return current visible children count
	 */
	public int getChildrenVisibleCount() {
		return childrenVisibleCount;
	}

	/**
	 * Set current visible children count
	 * 
	 * @param childrenVisibleCount
	 */
	public void setChildrenVisibleCount(int childrenVisibleCount) {
		if (this.childrenVisibleCount != childrenVisibleCount) {
			this.childrenVisibleCount = childrenVisibleCount;
			logger.debug("setChildrenVisibleCount calls load(" + currentOffset + ")");
			load(getCurrentOffset(), null);
		}

	}

	/**
	 * Load children
	 * 
	 * @param offset
	 *            the children offset
	 * @param callback
	 *            Handle the finish of load, returning the final offset, which
	 *            can be different from the requested offset in case the offset
	 *            is out of bounds
	 */
	protected void load(final int offset, AsyncCallback<Integer> callback) {
		itemPanel.setWaitImageVisible(true);

		final AsyncCallback<Integer> async = callback != null ? callback : new AsyncCallback<Integer>() {

			public void onFailure(Throwable caught) {
				logger.error("Error loading " + offset, caught);
			}

			public void onSuccess(Integer result) {
				// nothing to do
			}

		};

		final int last = (sdo.getSubElementsCount() < offset + childrenVisibleCount) ? sdo.getSubElementsCount()
				: offset + childrenVisibleCount;
		int newOffset = (last - childrenVisibleCount >= 0) ? last - childrenVisibleCount : 0;

		if (currentOffset != newOffset || initialising) {
			currentOffset = newOffset;

			previousItem.updateText();
			nextItem.updateText();

			previousItem.setVisible(currentOffset > 0);

			nextItem.setVisible(sdo.getSubElementsCount() > childrenVisibleCount
					&& currentOffset < sdo.getSubElementsCount() - childrenVisibleCount);

			logger.debug("Adding items [" + currentOffset + ", " + (last - 1) + "]");

			ensureLoaded(new AsyncCallback<Integer>() {

				public void onFailure(Throwable caught) {
					logger.error("Error loading FondsTreeItem", caught);
					itemPanel.setWaitImageVisible(false);
					async.onFailure(caught);
				}

				public void onSuccess(Integer count) {
					waitingToLoad += last - currentOffset;
					logger.debug("Waiting to load " + waitingToLoad);
					if (currentOffset == last) {
						itemPanel.setWaitImageVisible(false);
						async.onSuccess(currentOffset);
					} else {
						for (int i = currentOffset; i < last; i++) {
							final int index = i;
							memCache.getElement(index, new AsyncCallback<SimpleDescriptionObject>() {

								public void onFailure(Throwable caught) {
									logger.error("Error getting child " + index + " of " + sdo.getId(), caught);
									dummy.setText("error!");
									itemPanel.setWaitImageVisible(false);
									async.onFailure(caught);
								}

								public void onSuccess(SimpleDescriptionObject sdo) {
									if (initialising) {
										initialising = false;
										removeItems();
										addItem(previousItem);
									}
									CollectionsTreeItem item = new CollectionsTreeItem(sdo, filter, sorter,
											childrenVisibleCount, showInfo);
									addCollectionItem(item);
									if (--waitingToLoad <= 0) {
										itemPanel.setWaitImageVisible(false);
										async.onSuccess(currentOffset);
									}
								}

							});
						}
					}
				}
			});
		} else {
			itemPanel.setWaitImageVisible(false);
			async.onSuccess(currentOffset);
		}
	}

	protected void addCollectionItem(CollectionsTreeItem item) {

		int excepient = getChildCount() - childrenVisibleCount - 1;
		excepient = (excepient >= 0) ? excepient : 0;
		for (int i = 0; i < excepient; i++) {
			removeItem(getChild(1));
		}
		removeItem(nextItem);
		addItem(item);
		addItem(nextItem);

		Element itemContainer = DOM.getParent(DOM.getParent(item.getWidget().getElement()));
		DOM.setElementProperty(itemContainer, "width", "100%");
	}

	/**
	 * Set if slider is visible
	 * 
	 * @param sliderVisible
	 * @deprecated this method is not yet implemented
	 */
	@Deprecated
	public void setSliderVisible(boolean sliderVisible) {
		// this.itemPanel.setSliderVisible(sliderVisible);
	}

	class JumpCollectionsTreeItem extends TreeItem {

		private final boolean next;

		private final HorizontalPanel layout;

		private final Image icon;

		private final Label text;

		/**
		 * Create a new jump collections tree item, which allows children to be
		 * browsed
		 * 
		 * @param next
		 */
		public JumpCollectionsTreeItem(boolean next) {
			this.next = next;
			layout = new HorizontalPanel();
			icon = next ? browseImageBundle.collectionsTreeNextResults().createImage()
					: browseImageBundle.collectionsTreePreviousResults().createImage();
			text = new Label();

			layout.add(icon);
			layout.add(text);
			layout.setCellWidth(icon, "16px");
			layout.setCellWidth(text, "100%");
			layout.setCellHorizontalAlignment(text, HorizontalPanel.ALIGN_LEFT);
			layout.setCellVerticalAlignment(icon, HorizontalPanel.ALIGN_MIDDLE);
			layout.setCellVerticalAlignment(text, HorizontalPanel.ALIGN_MIDDLE);

			this.setWidget(layout);

			layout.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
			layout.addStyleName("jumpitem-layout");
			text.addStyleName("jumpitem-text");
			icon.addStyleName("jumpitem-icon");

			this.setStylePrimaryName("jumpitem");
			this.addStyleDependentName(next ? "next" : "previous");
		}

		protected void updateText() {
			if (next) {
				int last = Math.min(currentOffset + 2 * childrenVisibleCount, sdo.getSubElementsCount());
				text.setText(messages.nextItems(last - childrenVisibleCount + 1, last, sdo.getSubElementsCount()));
			} else {
				int first = currentOffset - childrenVisibleCount < 0 ? 0 : currentOffset - childrenVisibleCount;
				text.setText(messages.previousItems(first + 1, first + childrenVisibleCount));
			}
		}

		/**
		 * Jump to the next/previous children
		 */
		public void jump() {
			if (next) {
				load(Math.min(currentOffset + childrenVisibleCount, sdo.getSubElementsCount() - childrenVisibleCount),
						null);

			} else {

				load(currentOffset - childrenVisibleCount < 0 ? 0 : currentOffset - childrenVisibleCount, null);
			}
		}

		/**
		 * Is next or previous jump type
		 * 
		 * @return true if it jumps to the next children, false if it jumps to
		 *         the previous
		 */
		public boolean isNext() {
			return next;
		}

	}

	/**
	 * Show extended info of the next/previous children
	 * 
	 * @param showInfo
	 */
	public void setShowInfo(boolean showInfo) {
		if (this.showInfo != showInfo) {
			this.showInfo = showInfo;
			this.itemPanel.setShowInfo(showInfo);
			for (int i = 0; i < this.getChildCount(); i++) {
				TreeItem child = getChild(i);
				if (child instanceof CollectionsTreeItem) {
					((CollectionsTreeItem) child).setShowInfo(showInfo);
				}
			}
		}

	}

	/**
	 * Recursively focus on a path
	 * 
	 * @param path
	 *            the path to focus on
	 * @param callback
	 *            returns the CollectionsTreeItem witch the path points to
	 */
	public void focusOn(final String[] path, final AsyncCallback<CollectionsTreeItem> callback) {
		if (path.length == 0) {
			if (!this.isSelected()) {
				logger.debug("item " + sdo.getId() + " selecting itself");
				this.setSelected(true);
			}
			callback.onSuccess(this);
		} else {
			focusOn(path[0], new AsyncCallback<CollectionsTreeItem>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(CollectionsTreeItem focusItem) {
					focusItem.focusOn(Tools.tail(path), callback);
				}

			});

		}

	}

	/**
	 * Focus on a child pid, it ensures the child is loaded
	 * 
	 * @param pid
	 *            the pid of the child
	 * @param callback
	 *            handle the child's CollectionsTreeItem
	 */
	public void focusOn(final String pid, final AsyncCallback<CollectionsTreeItem> callback) {
		if (!initialized) {
			initialising = true;
		}
		// TODO fix this
//		BrowserService.Util.getInstance().getItemIndex(sdo.getId(), pid, getFilter(), getSorter(),
//				new AsyncCallback<Integer>() {
//
//					public void onFailure(Throwable caught) {
//						initialising = false;
//						callback.onFailure(caught);
//					}
//
//					public void onSuccess(Integer itemIndex) {
//						final int index = itemIndex.intValue();
//						if (index >= 0) {
//
//							final int newOffset = (index / childrenVisibleCount) * childrenVisibleCount;
//							load(newOffset, new AsyncCallback<Integer>() {
//
//								public void onFailure(Throwable caught) {
//									initialising = false;
//									callback.onFailure(caught);
//								}
//
//								public void onSuccess(Integer offset) {
//									initialized = true;
//									initialising = false;
//									CollectionsTreeItem.this.setState(true);
//									CollectionsTreeItem focusItem = (CollectionsTreeItem) getChild(
//											index - currentOffset + 1);
//									if (focusItem != null) {
//										callback.onSuccess(focusItem);
//									} else {
//										callback.onFailure(
//												new IndexOutOfBoundsException("Get child " + (index - currentOffset + 1)
//														+ " when child count is " + getChildCount()));
//									}
//								}
//
//							});
//						} else {
//							initialising = false;
//							callback.onFailure(new NoSuchElementException(
//									"Index of " + pid + " under " + sdo.getId() + " not found!"));
//						}
//					}
//				});

	}

	/**
	 * Get simple description object PID
	 * 
	 * @return object PID
	 */
	public String getPid() {
		return sdo.getId();
	}

	/**
	 * Update collection tree item
	 * 
	 * @param path
	 *            the collection tree path to update
	 * @param info
	 *            update info
	 * @param hierarchy
	 *            update hierarchy
	 * @param callback
	 */
	public void update(final String[] path, final boolean info, final boolean hierarchy,
			final AsyncCallback<CollectionsTreeItem> callback) {
		if (path.length == 1) {
			logger.debug("found the father of the element to update," + " refreshing cache");
			// TODO fix this
			// BrowserService.Util.getInstance().getItemIndex(sdo.getId(),
			// path[0], getFilter(), getSorter(),
			// new AsyncCallback<Integer>() {
			//
			// public void onFailure(Throwable caught) {
			// callback.onFailure(caught);
			// }
			//
			// public void onSuccess(Integer itemIndex) {
			// update(path[0], itemIndex, info, hierarchy, callback);
			// }
			//
			// });

		} else {
			focusOn(path[0], new AsyncCallback<CollectionsTreeItem>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(CollectionsTreeItem item) {
					item.update(Tools.tail(path), info, hierarchy, callback);
				}

			});
		}
	}

	private void update(final String pid, final int index, final boolean info, final boolean hierarchy,
			final AsyncCallback<CollectionsTreeItem> callback) {
		memCache.clear(index, new AsyncCallback<SimpleDescriptionObject>() {

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(final SimpleDescriptionObject child) {
				focusOn(pid, new AsyncCallback<CollectionsTreeItem>() {

					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(CollectionsTreeItem item) {
						logger.debug("Updating item " + item.getPid());
						item.setSDO(child);
						if (info) {
							item.updateInfo();
						}
						if (hierarchy) {
							item.updateHierarchy();
						}
						callback.onSuccess(item);
					}

				});

			}

		});
	}

	/**
	 * Get simple description object
	 * 
	 * @return get Simple Description Object
	 */
	public SimpleDescriptionObject getSDO() {
		return sdo;
	}

	protected void setSDO(SimpleDescriptionObject sdo) {
		this.sdo = sdo;
		this.memCache.setSDO(sdo);
	}

	/**
	 * Update collection tree item info
	 */
	public void updateInfo() {
		this.itemPanel = new TreeItemPanel(sdo, childrenVisibleCount, showInfo);
		this.setWidget(itemPanel);
	}

	/**
	 * Update collection tree item hierarchy
	 */
	public void updateHierarchy() {
		logger.debug("Updating hierarchy of " + sdo.getId());
		memCache.clear();
		initialized = false;
		init();
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
	}

	/**
	 * Get sorter
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
	}

}
