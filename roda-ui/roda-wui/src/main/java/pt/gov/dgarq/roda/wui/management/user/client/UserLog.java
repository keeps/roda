/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.facet.SimpleFacetParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.FacetUtils;
import pt.gov.dgarq.roda.wui.common.client.widgets.LogEntryList;
import pt.gov.dgarq.roda.wui.management.client.Management;

/**
 * @author Luis Faria
 * 
 */
public class UserLog extends Composite {

	public static final HistoryResolver RESOLVER = new HistoryResolver() {

		@Override
		public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
			getInstance().resolve(historyTokens, callback);
		}

		@Override
		public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
			UserLogin.getInstance().checkRoles(new HistoryResolver[] { UserLog.RESOLVER }, false, callback);
		}

		public String getHistoryPath() {
			return Management.RESOLVER.getHistoryPath() + "." + getHistoryToken();
		}

		public String getHistoryToken() {
			return "log";
		}
	};

	private static UserLog instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return the instance
	 */
	public static UserLog getInstance() {
		if (instance == null) {
			instance = new UserLog(null);
		}
		return instance;
	}

	interface MyUiBinder extends UiBinder<Widget, UserLog> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final User user;

	@UiField
	FlowPanel inputUserFilterPanel;

	@UiField
	TextBox inputUserName;

	@UiField
	DateBox inputDateInitial;

	@UiField
	DateBox inputDateFinal;

	@UiField(provided = true)
	LogEntryList logList;

	@UiField(provided = true)
	FlowPanel facetComponents;

	@UiField(provided = true)
	FlowPanel facetMethods;

	@UiField(provided = true)
	FlowPanel facetUsers;

	/**
	 * Create a new user log
	 * 
	 * @param user
	 */
	public UserLog(User user) {
		this.user = user;

		Filter filter = null;
		Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.LOG_ACTION_COMPONENT),
				new SimpleFacetParameter(RodaConstants.LOG_ACTION_METHOD),
				new SimpleFacetParameter(RodaConstants.LOG_USERNAME));
		logList = new LogEntryList(filter, facets);
		facetComponents = new FlowPanel();
		facetMethods = new FlowPanel();
		facetUsers = new FlowPanel();

		Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
		facetPanels.put(RodaConstants.LOG_ACTION_COMPONENT, facetComponents);
		facetPanels.put(RodaConstants.LOG_ACTION_METHOD, facetMethods);
		facetPanels.put(RodaConstants.LOG_USERNAME, facetUsers);
		FacetUtils.bindFacets(logList, facetPanels);

		initWidget(uiBinder.createAndBindUi(this));
		
		inputUserFilterPanel.setVisible(user == null);
	}

	protected Filter getFilter() {
		List<FilterParameter> parameters = new Vector<FilterParameter>();
		if (user != null) {
			parameters.add(new SimpleFilterParameter("username", user.getName()));
		} else if (!inputUserName.getText().equals("")) {
			parameters.add(new SimpleFilterParameter("username", inputUserName.getText()));
		}
		// if (action != null) {
		// parameters.add(new SimpleFilterParameter("action", action));
		// }
		// if (dateInitial != null || dateFinal != null) {
		// parameters.add(new RangeFilterParameter("datetime", dateInitial,
		// dateFinal));
		// }

		return new Filter(parameters.toArray(new FilterParameter[parameters.size()]));
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			logList.refresh();
			callback.onSuccess(this);
		} else {
			History.newItem(RESOLVER.getHistoryPath());
			callback.onSuccess(null);
		}
	}

}
