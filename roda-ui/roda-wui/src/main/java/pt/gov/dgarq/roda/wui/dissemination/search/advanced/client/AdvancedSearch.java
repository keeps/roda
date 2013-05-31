/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.advanced.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.data.SearchParameter;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.AdvancedSearchConstants;

/**
 * @author Luis Faria
 * 
 */
public class AdvancedSearch extends VerticalPanel implements HistoryResolver {

	private static AdvancedSearch instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return the instance
	 */
	public static AdvancedSearch getInstance() {
		if (instance == null) {
			instance = new AdvancedSearch();
		}
		return instance;
	}

	private static AdvancedSearchConstants constants = (AdvancedSearchConstants) GWT
			.create(AdvancedSearchConstants.class);

	private static final int BLOCK_SIZE = 30;

	private static final int MAX_SIZE = 3000;

	private static final int SNIPPETS_MAX = 5;

	private static final int FIELD_MAX_LENGHT = 50;

	private boolean initialized;

	private Label levelChoosingLabel;

	private ElementLevelChooser elementLevelChooser;

	private Label dateIntervalLabel;

	private DateIntervalPicker dateIntervalPicker;

	private Label addKeywordLabel;

	private VerticalPanel addKeywordLayout;

	private KeywordPicker keywordPicker;

	private VerticalPanel addedKeywordItems;

	private WUIButton search;

	private SearchResultPanel searchResultPanel;

	private boolean firstSearch;

	private AdvancedSearch() {
		initialized = false;

	}

	/**
	 * Initialize advanced search
	 */
	public void init() {
		if (!initialized) {
			initialized = true;
			levelChoosingLabel = new Label(constants.chooseLevelLabel());
			elementLevelChooser = new ElementLevelChooser();

			dateIntervalLabel = new Label(constants.chooseDateIntervalLabel());
			dateIntervalPicker = new DateIntervalPicker();

			addKeywordLabel = new Label(constants.addKeywordsLabel());

			addKeywordLayout = new VerticalPanel();

			addedKeywordItems = new VerticalPanel();
			addedKeywordItems.setVisible(false);

			keywordPicker = new KeywordPicker() {

				protected void onKeywordAdd(KeywordParameter keywordParameter) {
					addedKeywordItems.setVisible(true);
					addedKeywordItems.add(new KeywordItem(keywordParameter) {

						protected void onKeywordRemove() {
							addedKeywordItems.remove(this);
							if (addedKeywordItems.getWidgetCount() == 0) {
								addedKeywordItems.setVisible(false);
							}
						}

					});
					keywordPicker.clear();

				}

			};

			addKeywordLayout.add(keywordPicker);
			addKeywordLayout.add(addedKeywordItems);

			search = new WUIButton(constants.search(), WUIButton.Left.ROUND,
					WUIButton.Right.ARROW_FORWARD);

			search.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					update();
				}

			});

			add(levelChoosingLabel);
			add(elementLevelChooser);
			add(dateIntervalLabel);
			add(dateIntervalPicker);
			add(addKeywordLabel);
			add(addKeywordLayout);
			add(search);

			this.firstSearch = true;

			this.addStyleName("wui-search-advanced");
			levelChoosingLabel.addStyleName("section-label");
			dateIntervalLabel.addStyleName("section-label");
			addKeywordLabel.addStyleName("section-label");

			elementLevelChooser.addStyleName("section-content");
			dateIntervalPicker.addStyleName("section-content");
			addKeywordLayout.addStyleName("section-content");
			addedKeywordItems.addStyleName("addedKeywords");

			search.addStyleName("searchButton");
		}
	}

	/**
	 * Update search results
	 */
	public void update() {
		if (addedKeywordItems.getWidgetCount() > 0) {
			List<SearchParameter> searchParameterList = new ArrayList<SearchParameter>();
			searchParameterList.addAll(Arrays.asList(elementLevelChooser
					.getSearchParameters()));
			searchParameterList.addAll(Arrays.asList(dateIntervalPicker
					.getSearchParameters()));

			for (Widget widget : addedKeywordItems) {
				KeywordItem addedKeyword = (KeywordItem) widget;
				if (addedKeyword.isChecked()) {
					searchParameterList.add(addedKeyword.getKeywordParameter()
							.getSearchParameter());
				}
			}
			if (searchParameterList.size() > 0) {
				SearchParameter[] searchParameters = (SearchParameter[]) searchParameterList
						.toArray(new SearchParameter[] {});

				if (firstSearch) {
					firstSearch = false;
					searchResultPanel = createSearchResult(searchParameters);
					this.add(searchResultPanel);
				} else {
					this.remove(searchResultPanel);
					searchResultPanel = createSearchResult(searchParameters);
					this.add(searchResultPanel);
				}
			} else {
				Window.alert(constants.searchingButNoKeywordsAlert());
			}
		} else {
			if (keywordPicker.onKeywordAdd()) {
				update();
			}

		}

	}

	private SearchResultPanel createSearchResult(
			final SearchParameter[] searchParameters) {
		return new SearchResultPanel(BLOCK_SIZE, MAX_SIZE) {

			protected void getSearchResult(int startItem, int limit,
					AsyncCallback<SearchResult> callback) {

				SearchService.Util.getInstance().advancedSearch(
						searchParameters, startItem, limit, SNIPPETS_MAX,
						FIELD_MAX_LENGHT, callback);

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
		return "advanced";
	}
}
