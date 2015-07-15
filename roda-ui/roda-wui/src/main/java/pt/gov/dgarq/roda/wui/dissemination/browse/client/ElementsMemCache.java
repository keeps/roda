/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.List;
import java.util.Vector;

import org.roda.index.filter.Filter;
import org.roda.index.sorter.Sorter;

import com.google.gwt.user.client.rpc.AsyncCallback;

import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;

/**
 * @author Luis Faria
 * 
 */
public abstract class ElementsMemCache {

	// class variables
	private static final int LOAD_BLOCK_SIZE = 30;

	// instance variables
	private ClientLogger logger = new ClientLogger(getClass().getName());

	private Filter filter;

	private Sorter sorter;

	private boolean loaded;

	private boolean loading;

	private final List<AsyncCallback<Integer>> loadListeners;

	private int count;

	private SimpleDescriptionObject[] memCache;

	private int elementsCachedNumber;

	private boolean working;

	private interface CacheListener {
		/**
		 * Do something when cache is idle
		 */
		public void onIdle();
	}

	private final List<CacheListener> cacheListeners;

	/**
	 * Create a new elements memory cache
	 * 
	 * @param filter
	 * @param sorter
	 */
	public ElementsMemCache(Filter filter, Sorter sorter) {
		this.filter = filter;
		this.sorter = sorter;
		loaded = false;
		loading = false;
		loadListeners = new Vector<AsyncCallback<Integer>>();
		memCache = null;
		elementsCachedNumber = 0;
		cacheListeners = new Vector<CacheListener>();
		working = false;
	}

	/**
	 * Ensure the cache is loaded/initialized
	 * 
	 * @param callback
	 *            handle the finish of loading/initialization. It return the
	 *            total elements count
	 */
	public void ensureLoaded(final AsyncCallback<Integer> callback) {
		if (!loaded && !loading) {
			loading = true;
			logger.debug("MemCache loading...");
			getCountImpl(new AsyncCallback<Integer>() {

				public void onFailure(Throwable caught) {
					logger.error("Error getting element count", caught);
					loading = false;
					callback.onFailure(caught);
					for (AsyncCallback<Integer> loadListener : loadListeners) {
						loadListener.onFailure(caught);
					}
				}

				public void onSuccess(Integer count) {
					ElementsMemCache.this.count = count;
					if (memCache == null) {
						memCache = new SimpleDescriptionObject[count];
					}
					loaded = true;
					loading = false;
					logger.debug("MemCache loaded, count=" + count);
					callback.onSuccess(count);
					for (AsyncCallback<Integer> loadListener : loadListeners) {
						loadListener.onSuccess(count);
					}
					loadListeners.clear();
				}

			});
		} else if (!loaded) {
			loadListeners.add(callback);
		} else {
			callback.onSuccess(count);
		}
	}

	/**
	 * Get total elements count
	 * 
	 * @param callback
	 */
	public void getCount(final AsyncCallback<Integer> callback) {
		ensureLoaded(new AsyncCallback<Integer>() {

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(Integer count) {
				callback.onSuccess(count);
			}

		});
	}

	/**
	 * Get the total elements count
	 * 
	 * @param callback
	 */
	protected abstract void getCountImpl(AsyncCallback<Integer> callback);

	/**
	 * Fetch elements from server
	 * 
	 * @param firstItemIndex
	 *            the index of the first item to fetch
	 * @param count
	 *            the maximum number of item to fetch
	 * @param callback
	 */
	protected abstract void getElements(int firstItemIndex, int count,
			AsyncCallback<SimpleDescriptionObject[]> callback);

	/**
	 * Get an element from cache. If element is not yet in cache, it will be
	 * fetched in block and then returned.
	 * 
	 * @param index
	 *            the element index
	 * @param callback
	 */
	public void getElement(final int index, final AsyncCallback<SimpleDescriptionObject> callback) {
		ensureLoaded(new AsyncCallback<Integer>() {

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(Integer count) {
				if (index >= count) {
					callback.onFailure(new IndexOutOfBoundsException(index + " >= " + count));
				} else {
					addCacheListener(index, callback);
				}
			}

		});
	}

	private void addCacheListener(final int index, final AsyncCallback<SimpleDescriptionObject> callback) {
		cacheListeners.add(new CacheListener() {

			public void onIdle() {
				cache(index, callback);
			}
		});
		if (!working) {
			work();
		}

	}

	private void work() {
		if (cacheListeners.isEmpty()) {
			working = false;
		} else {
			working = true;
			CacheListener listener = (CacheListener) cacheListeners.remove(0);
			listener.onIdle();

		}

	}

	private void cache(final int index, final AsyncCallback<SimpleDescriptionObject> callback) {
		if (index < 0) {
			throw new IndexOutOfBoundsException(index + " < " + 0);
		} else if (index > count) {
			throw new IndexOutOfBoundsException(index + " > " + count);
		} else if (!loaded) {
			ensureLoaded(new AsyncCallback<Integer>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(Integer result) {
					cache(index, callback);
				}

			});
		} else if (memCache[index] == null) {
			final int blockstart = (index / LOAD_BLOCK_SIZE) * LOAD_BLOCK_SIZE;
			final int remainder = memCache.length - blockstart;
			final int blocksize = (remainder < LOAD_BLOCK_SIZE) ? remainder : LOAD_BLOCK_SIZE;
			logger.debug("MemProxy cache fault, loading [" + blockstart + ", " + (blockstart + blocksize - 1) + "] for "
					+ index);

			getElements(blockstart, blocksize, new AsyncCallback<SimpleDescriptionObject[]>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
					work();
				}

				public void onSuccess(final SimpleDescriptionObject[] block) {
					ensureLoaded(new AsyncCallback<Integer>() {

						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}

						public void onSuccess(Integer result) {
							updateBlock(blockstart, block);
							callback.onSuccess(memCache[index]);
							work();
						}

					});

				}

			});
		} else {
			callback.onSuccess(memCache[index]);
			work();
		}
	}

	protected void updateBlock(int startIndex, SimpleDescriptionObject[] block) {
		for (int i = 0; i < block.length; i++) {
			memCache[i + startIndex] = block[i];
			elementsCachedNumber++;
		}
	}

	/**
	 * Get the number of cached elements
	 * 
	 * @return the number of cached elements
	 */
	public int getElementsCachedNumber() {
		return elementsCachedNumber;
	}

	/**
	 * Clear all cache
	 * 
	 */
	public void clear() {
		loaded = false;
		memCache = null;
		elementsCachedNumber = 0;
	}

	private int getItemIndex(String pid) {
		int ret = -1;
		if (memCache != null) {
			for (int i = 0; i < memCache.length; i++) {
				SimpleDescriptionObject sdo = memCache[i];
				if (pid.equals(sdo.getId())) {
					ret = i;
				}
			}
		}
		return ret;
	}

	public void clear(String pid, final AsyncCallback<SimpleDescriptionObject> item) {
		clear(getItemIndex(pid), item);
	}

	/**
	 * Clear the cache of a specified item
	 * 
	 * @param index
	 *            the index of the item
	 * @param item
	 *            the item re-cached
	 */
	public void clear(final int index, final AsyncCallback<SimpleDescriptionObject> item) {
		ensureLoaded(new AsyncCallback<Integer>() {

			public void onFailure(Throwable caught) {
				item.onFailure(caught);
			}

			public void onSuccess(Integer result) {
				memCache[index] = null;
				cache(index, item);
			}

		});

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
	 * Set current filter
	 * 
	 * @param filter
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
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
	 * Set current sorter
	 * 
	 * @param sorter
	 */
	public void setSorter(Sorter sorter) {
		this.sorter = sorter;
	}

}
