/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;

import com.google.gwt.user.client.rpc.AsyncCallback;

import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

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
	public SubElementsProxy(SimpleDescriptionObject sdo, Filter filter, Sorter sorter) {
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
		BrowserService.Util.getInstance().getSubElementsCount(sdo.getId(), getFilter(), callback);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pt.gov.dgarq.roda.office.dissemination.browse.client.ElementsMemProxy
	 * #getElements(int, int, com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	public void getElements(int firstItemIndex, int count, AsyncCallback<SimpleDescriptionObject[]> callback) {
		BrowserService.Util.getInstance().getSubElements(sdo.getId(), getFilter(), getSorter(),
				new Sublist(firstItemIndex, count), callback);

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
