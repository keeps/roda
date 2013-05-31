/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.client;

import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.data.SearchResultObject;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyScroll;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyScroll.Loader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import config.i18n.client.SearchConstants;
import config.i18n.client.SearchMessages;

/**
 * @author Luis Faria
 * 
 */
public abstract class SearchResultPanel extends DockPanel {

	private static SearchConstants constants = (SearchConstants) GWT
			.create(SearchConstants.class);

	private static SearchMessages messages = (SearchMessages) GWT
			.create(SearchMessages.class);

	// private ClientLogger logger = new ClientLogger(getClass().getName());

	private final int blockSize;

	private final int maxSize;

	private final Label total;

	private final LazyScroll scroll;

	private final VerticalPanel layout;

	/**
	 * Create a new search result panel
	 * 
	 * @param blockSize
	 * @param maxSize
	 */
	public SearchResultPanel(int blockSize, int maxSize) {
		this.blockSize = blockSize;
		this.maxSize = maxSize;
		this.total = new Label(constants.searching());
		this.layout = new VerticalPanel();
		this.scroll = new LazyScroll(layout, blockSize, maxSize, new Loader() {

			public void load(final int offset, final int widgetOffset,
					int limit, final AsyncCallback<Integer> loaded) {
				getSearchResult(offset, limit,
						new AsyncCallback<SearchResult>() {

							public void onFailure(Throwable caught) {
								loaded.onFailure(caught);
							}

							public void onSuccess(SearchResult searchResult) {
								updateLayout(searchResult, offset
										- widgetOffset);
								loaded.onSuccess(new Integer(searchResult
										.getResultCount()));
							}

						});

			}

			public void remove(int offset, int widgetOffset, int limit,
					AsyncCallback<Integer> removed) {
				int first = offset - widgetOffset;
				int last = (first + limit < layout.getWidgetCount()) ? first
						+ limit : layout.getWidgetCount();
				for (int i = first; i < last; i++) {
					layout.remove(i);
				}
				removed.onSuccess(new Integer(last - first));

			}

			public void update(int widgetOffset, int count,
					AsyncCallback<Integer> updatedOffset) {
				updatedOffset.onSuccess(new Integer(widgetOffset + count));
			}

		});
		add(total, NORTH);

		add(scroll, CENTER);
		// scroll.init();

		setCellHorizontalAlignment(total, ALIGN_RIGHT);

		this.scroll.addStyleName("scrollAutoLoader");
		this.layout.addStyleName("wui-search-results-layout");
		this.total.addStyleName("wui-search-results-total");
		this.addStyleName("wui-search-results");

	}

	/**
	 * Method used to fetch a SearchResult
	 * 
	 * @param startItem
	 *            index of the first item to show
	 * @param limit
	 *            number of items to show
	 * @param callback
	 *            the callback to return the SearchResult
	 */
	protected abstract void getSearchResult(int startItem, int limit,
			AsyncCallback<SearchResult> callback);

	private SearchResultObjectPanel selected = null;

	protected void updateLayout(SearchResult searchResult, int offset) {
		total.setText(messages.totalResultsMessage(searchResult.getHitTotal()));
		SearchResultObject[] searchResultObjects = searchResult
				.getSearchResultObjects();
		for (int i = 0; i < searchResultObjects.length; i++) {
			final SearchResultObjectPanel objPanel = new SearchResultObjectPanel(
					searchResultObjects[i]);

			objPanel.addEventHandler(new DisclosureHandler() {

				public void onClose(DisclosureEvent event) {
					if (selected == objPanel) {
						selected = null;
					}
				}

				public void onOpen(DisclosureEvent event) {
					if (selected != null && selected != objPanel) {
						selected.setOpen(false);
					}
					selected = objPanel;
				}

			});

			layout.insert(objPanel, i + offset);

		}

	}

}
