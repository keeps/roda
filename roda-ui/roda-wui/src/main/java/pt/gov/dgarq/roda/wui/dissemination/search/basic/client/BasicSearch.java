/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.basic.client;

import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.dissemination.search.client.Search;
import pt.gov.dgarq.roda.wui.dissemination.search.client.SearchResultPanel;
import pt.gov.dgarq.roda.wui.dissemination.search.client.SearchService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.SearchConstants;

/**
 * @author Luis Faria
 * 
 */
public class BasicSearch extends DockPanel implements HistoryResolver {

	private static SearchConstants constants = (SearchConstants) GWT
			.create(SearchConstants.class);

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

	private SearchResultPanel searchResultPanel;

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
			this.searchInputButton = new WUIButton(constants
					.basicSearchButtonLabel(),
					WUIButton.Left.ROUND,
					WUIButton.Right.ARROW_FORWARD);
			this.searchInputLayout.add(searchInputLabel, DockPanel.NORTH);
			this.searchInputLayout.add(searchInputBox, DockPanel.CENTER);
			this.searchInputLayout.add(searchInputButton, DockPanel.EAST);

			this.add(searchInputLayout, NORTH);

			this.searchInputBox.addKeyboardListener(new KeyboardListener() {

				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
					if (keyCode == KEY_ENTER) {
						update();
					}
				}

				public void onKeyPress(Widget sender, char keyCode,
						int modifiers) {
				}

				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				}

			});

			this.searchInputButton.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					update();
				}

			});

			this.firstSearch = true;

			searchInputLayout.setCellVerticalAlignment(searchInputButton,
					DockPanel.ALIGN_MIDDLE);
			searchInputLabel.addStyleName("label");
			searchInputBox.addStyleName("box");
			searchInputLayout.addStyleName("layout");
			searchInputButton.addStyleName("button");
			this.addStyleName("wui-search-basic");
		}
	}

	public void update() {
		if (searchInputBox.getText().length() > 0) {
			if (firstSearch) {
				firstSearch = false;
				searchResultPanel = createSearchResult();
				this.add(searchResultPanel, CENTER);
			} else {
				this.remove(searchResultPanel);
				searchResultPanel = createSearchResult();
				this.add(searchResultPanel, CENTER);
			}
		} else {
			Window.alert(constants.basicSearchNoKeywords());
		}

	}

	private SearchResultPanel createSearchResult() {
		return new SearchResultPanel(BLOCK_SIZE, MAX_SIZE) {

			protected void getSearchResult(int startItem, int limit,
					AsyncCallback<SearchResult> callback) {
				SearchService.Util.getInstance().basicSearch(
						searchInputBox.getText(), startItem, limit,
						SNIPPETS_MAX, FIELD_MAX_LENGHT, callback);
			}

		};
	}

	public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
		UserLogin.getInstance().checkRole(this, callback);
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			init();
			callback.onSuccess(this);
		} else {
			History.newItem(getHistoryPath());
			callback.onSuccess(null);
		}
	}

	public String getHistoryPath() {
		return Search.getInstance().getHistoryPath() + "." + getHistoryToken();
	}

	public String getHistoryToken() {
		return "basic";
	}

}
