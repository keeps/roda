/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import org.roda.index.filter.Filter;
import org.roda.index.sorter.Sorter;
import org.roda.index.sublist.Sublist;
import org.roda.legacy.aip.metadata.descriptive.SimpleDescriptionObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public class CollectionsProxy extends ElementsMemCache {

	/**
	 * Create a new collections proxy
	 * 
	 * @param filter
	 *            Filter to use in the collections listing
	 * @param sorter
	 *            Sorter to use in collections listing
	 */
	public CollectionsProxy(Filter filter, Sorter sorter) {
		super(filter, sorter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pt.gov.dgarq.roda.office.dissemination.browse.client.ElementsMemProxy
	 * #getCount(com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	public void getCountImpl(AsyncCallback<Integer> callback) {
		BrowserService.Util.getInstance().getCollectionsCount(getFilter(), callback);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pt.gov.dgarq.roda.office.dissemination.browse.client.ElementsMemProxy
	 * #getElements(int, int, com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	public void getElements(int firstItemIndex, int count, AsyncCallback<SimpleDescriptionObject[]> callback) {
		BrowserService.Util.getInstance().getCollections(getFilter(), getSorter(), new Sublist(firstItemIndex, count),
				callback);

	}

	/**
	 * Clear proxy
	 * 
	 * @param pid
	 * @param callback
	 */
	public void clear(String pid, final AsyncCallback<SimpleDescriptionObject> callback) {
		clear(pid, callback);
	}

}
