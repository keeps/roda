/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.basic.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.SearchConstants;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.CollectionsTable;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.dissemination.search.client.Search;

/**
 * @author Luis Faria
 * 
 */
public class BasicSearch extends DockPanel {

	public static final HistoryResolver RESOLVER = new HistoryResolver() {

		@Override
		public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
			getInstance().resolve(historyTokens, callback);
		}

		@Override
		public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
			UserLogin.getInstance().checkRole(this, callback);
		}

		@Override
		public String getHistoryPath() {
			return Search.getInstance().getHistoryPath() + "." + getHistoryToken();
		}

		@Override
		public String getHistoryToken() {
			return "basic";
		}
	};

	private static SearchConstants constants = (SearchConstants) GWT.create(SearchConstants.class);

	private static BasicSearch instance = null;

	private static final int BLOCK_SIZE = 30;

	private static final int MAX_SIZE = 3000;

	private static final int SNIPPETS_MAX = 5;

	private static final int FIELD_MAX_LENGHT = 50;

	public static BasicSearch getInstance() {
		if (instance == null) {
			instance = new BasicSearch();
		}
		return instance;
	}

	private boolean initialized;

	private DockPanel searchInputLayout;

	private Label searchInputLabel;

	private TextBox searchInputBox;

	private WUIButton searchInputButton;

	private CollectionsTable searchResultPanel = null;

	private boolean firstSearch;

	private BasicSearch() {
		initialized = false;
	}

	private void init() {
		if (!initialized) {
			initialized = true;
			this.searchInputLabel = new Label(constants.basicSearchInputLabel());
			this.searchInputBox = new TextBox();
			this.searchInputLayout = new DockPanel();
			this.searchInputButton = new WUIButton(constants.basicSearchButtonLabel(), WUIButton.Left.ROUND,
					WUIButton.Right.ARROW_FORWARD);
			this.searchInputLayout.add(searchInputLabel, DockPanel.NORTH);
			this.searchInputLayout.add(searchInputBox, DockPanel.CENTER);
			this.searchInputLayout.add(searchInputButton, DockPanel.EAST);

			this.add(searchInputLayout, NORTH);

			this.searchInputBox.addKeyDownHandler(new KeyDownHandler() {

				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						update();
					}
				}
			});

			this.searchInputButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					update();
				}
			});

			this.firstSearch = true;

			searchInputLayout.setCellVerticalAlignment(searchInputButton, DockPanel.ALIGN_MIDDLE);
			searchInputLabel.addStyleName("label");
			searchInputBox.addStyleName("box");
			searchInputLayout.addStyleName("layout");
			searchInputButton.addStyleName("button");
			this.addStyleName("wui-search-basic");
		}
	}

	public void update() {
		searchResultPanel = getSearchResultPanel();
		searchResultPanel.setFilter(
				new Filter(new BasicSearchFilterParameter(RodaConstants.SDO__ALL, searchInputBox.getText())));
		this.add(searchResultPanel, CENTER);
	}

	private CollectionsTable getSearchResultPanel() {
		if (searchResultPanel == null) {
			searchResultPanel = new CollectionsTable(new Filter());
		}
		return searchResultPanel;
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			init();
			callback.onSuccess(this);
		} else {
			History.newItem(RESOLVER.getHistoryPath());
			callback.onSuccess(null);
		}
	}

}
