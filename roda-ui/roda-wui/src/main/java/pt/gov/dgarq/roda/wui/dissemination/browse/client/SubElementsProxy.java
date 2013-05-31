/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public class SubElementsProxy extends ElementsMemCache {

	// private GWTLogger logger = new GWTLogger(GWT.getTypeName(this));

	private SimpleDescriptionObject sdo;

	/**
	 * Get sub elements proxy
	 * 
	 * @param sdo
	 * @param filter
	 * @param sorter
	 */
	public SubElementsProxy(SimpleDescriptionObject sdo, Filter filter,
			Sorter sorter) {
		super(filter, sorter);
		this.sdo = sdo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pt.gov.dgarq.roda.office.dissemination.browse.client.ElementsMemProxy
	 * #getCount(com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	public void getCountImpl(AsyncCallback<Integer> callback) {
		BrowserService.Util.getInstance().getSubElementsCount(sdo.getPid(),
				getFilter(), callback);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pt.gov.dgarq.roda.office.dissemination.browse.client.ElementsMemProxy
	 * #getElements(int, int, com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	public void getElements(int firstItemIndex, int count,
			AsyncCallback<SimpleDescriptionObject[]> callback) {
		ContentAdapter adapter = new ContentAdapter();
		adapter.setFilter(getFilter());
		adapter.setSorter(getSorter());
		adapter.setSublist(new Sublist(firstItemIndex, count));
		BrowserService.Util.getInstance().getSubElements(sdo.getPid(), adapter,
				callback);

	}

	/**
	 * Clear proxy
	 * 
	 * @param pid
	 * @param callback
	 */
	public void clear(String pid,
			final AsyncCallback<SimpleDescriptionObject> callback) {
		BrowserService.Util.getInstance().getItemIndex(sdo.getPid(), pid,
				getFilter(), getSorter(), new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(Integer index) {
						clear(index, callback);

					}

				});
	}

	/**
	 * Set simple description object
	 * 
	 * @param sdo
	 */
	public void setSDO(SimpleDescriptionObject sdo) {
		this.sdo = sdo;
	}

}
