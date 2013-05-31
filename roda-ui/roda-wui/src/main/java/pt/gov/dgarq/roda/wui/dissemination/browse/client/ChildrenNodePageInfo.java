/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.io.Serializable;
import java.util.List;

import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;

/**
 * @author Luis Faria
 *
 */
public class ChildrenNodePageInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int itemsBeforePageCount;

	private int itemsAfterPageCount;

	private List<SimpleDescriptionObject> page;

	private int pageNumber;

	public ChildrenNodePageInfo() {

	}

	public ChildrenNodePageInfo(int itemsBeforePageCount,
			int itemsAfterPageCount, List<SimpleDescriptionObject> page) {
		this.itemsBeforePageCount = itemsBeforePageCount;
		this.itemsAfterPageCount = itemsAfterPageCount;
		this.page = page;
	}


	public ChildrenNodePageInfo(int itemsBeforePageCount,
			int itemsAfterPageCount, List<SimpleDescriptionObject> page, int pageNumber) {
		this.itemsBeforePageCount = itemsBeforePageCount;
		this.itemsAfterPageCount = itemsAfterPageCount;
		this.page = page;
		this.pageNumber = pageNumber;
	}

	public int getItemsAfterPageCount() {
		return itemsAfterPageCount;
	}

	public void setItemsAfterPageCount(int itemsAfterPageCount) {
		this.itemsAfterPageCount = itemsAfterPageCount;
	}

	public int getItemsBeforePageCount() {
		return itemsBeforePageCount;
	}

	public void setItemsBeforePageCount(int itemsBeforePageCount) {
		this.itemsBeforePageCount = itemsBeforePageCount;
	}

	public List<SimpleDescriptionObject> getPage() {
		return page;
	}

	public void setPage(List<SimpleDescriptionObject> page) {
		this.page = page;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
}
