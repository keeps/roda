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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.SearchConstants;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.AIPList;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.Browse;

/**
 * @author Luis Faria
 * 
 */
public class BasicSearch extends Composite {

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
			return getHistoryToken();
		}

		@Override
		public String getHistoryToken() {
			return "search";
		}
	};

	private static SearchConstants constants = (SearchConstants) GWT.create(SearchConstants.class);

	private static BasicSearch instance = null;

	public static BasicSearch getInstance() {
		if (instance == null) {
			instance = new BasicSearch();
		}
		return instance;
	}

	interface MyUiBinder extends UiBinder<Widget, BasicSearch> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	@UiField
	Label searchInputLabel;

	@UiField
	TextBox searchInputBox;

	@UiField
	FocusPanel searchInputButton;

	@UiField(provided = true)
	AIPList searchResultPanel;

	// ADVANCED SEARCH
	@UiField
	DisclosurePanel advancedSearchDisclosure;

	@UiField
	TextBox advancedSearchInputTitle;

	@UiField
	DateBox advancedSearchInputDateInitial;
	@UiField
	DateBox advancedSearchInputDateFinal;

	private BasicSearch() {
		searchResultPanel = new AIPList();
		initWidget(uiBinder.createAndBindUi(this));

		searchInputLabel.setText(constants.basicSearchInputLabel());
		// searchInputButton.setText(constants.basicSearchButtonLabel());

		searchResultPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				SimpleDescriptionObject sdo = searchResultPanel.getSelectionModel().getSelectedObject();
				if (sdo != null) {
					view(sdo.getId());
				}
			}
		});

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

		DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM));
		advancedSearchInputDateInitial.setFormat(dateFormat);
		advancedSearchInputDateFinal.setFormat(dateFormat);
		advancedSearchInputDateInitial.getDatePicker().setYearArrowsVisible(true);
		advancedSearchInputDateFinal.getDatePicker().setYearArrowsVisible(true);

	}

	protected void view(String id) {
		String path = Browse.RESOLVER.getHistoryPath() + "." + id;
		History.newItem(path);
	}

	public void update() {
		String query = searchInputBox.getText();

		if ("".equals(query)) {
			searchResultPanel.setVisible(false);
		} else {
			searchResultPanel.setFilter(new Filter(new BasicSearchFilterParameter(RodaConstants.SDO__ALL, query)));
			searchResultPanel.setVisible(true);
		}

	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			callback.onSuccess(this);
		} else {
			History.newItem(RESOLVER.getHistoryPath());
			callback.onSuccess(null);
		}
	}

}
