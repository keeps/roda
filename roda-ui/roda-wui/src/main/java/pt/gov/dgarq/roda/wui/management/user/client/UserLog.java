/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.index.sorter.SortParameter;
import org.roda.legacy.old.adapter.ContentAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.DatePicker;
import pt.gov.dgarq.roda.wui.common.client.widgets.ElementPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList.ContentSource;
import pt.gov.dgarq.roda.wui.common.client.widgets.ListHeaderPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.management.client.Management;

/**
 * @author Luis Faria
 * 
 */
public class UserLog extends SimplePanel implements HistoryResolver {

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

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private static UserManagementMessages messages = (UserManagementMessages) GWT
			.create(UserManagementMessages.class);

	// private ClientLogger logger = new ClientLogger(getClass().getName());

	private final User user;

	private String action;

	private String dateInitial;

	private String dateFinal;

	private final VerticalPanel layout;

	private DockPanel filters_layout;

	private LazyVerticalList<LogEntry> lazyList;

	private boolean initialized;

	/**
	 * Create a new user log
	 * 
	 * @param user
	 */
	public UserLog(User user) {
		this.user = user;
		this.layout = new VerticalPanel();
		this.initialized = false;

		if (user != null) {
			init();
		}
	}

	private void init() {
		if (!initialized) {
			initialized = true;

			this.action = null;
			this.dateInitial = null;
			this.dateFinal = null;

			filters_layout = new DockPanel();
			createFiltersPanel();

			this.lazyList = new LazyVerticalList<LogEntry>(
					new ContentSource<LogEntry>() {

						public void getCount(Filter filter,
								AsyncCallback<Integer> callback) {
							UserManagementService.Util.getInstance()
									.getLogEntriesCount(filter, callback);
						}

						public ElementPanel<LogEntry> getElementPanel(
								LogEntry element) {
							return new LogEntryPanel(element, user == null);
						}

						public void getElements(ContentAdapter adapter,
								AsyncCallback<LogEntry[]> callback) {
							UserManagementService.Util.getInstance()
									.getLogEntries(adapter, callback);
						}

						public String getTotalMessage(int total) {
							return messages.userLogEntriesTotal(total);
						}

						public void setReportInfo(ContentAdapter adapter,
								String locale, AsyncCallback<Void> callback) {
							UserManagementService.Util.getInstance()
									.setUserLogReportInfo(adapter, locale,
											callback);

						}

					}, false, getFilter());

			lazyList.setScrollHeight("350px");

			addLogHeaders();

			layout.add(filters_layout);
			layout.add(lazyList.getWidget());

			this.setWidget(layout);

			this.setStylePrimaryName("wui-userlog");
			if(user!=null) {
				this.addStyleDependentName("user");
			}
			layout.addStyleName("userlog-layout");
			filters_layout.addStyleName("log-filters-layout");
			lazyList.getWidget().addStyleName("log-entries");
		}
	}

	private void createFiltersPanel() {

		HorizontalPanel pickers = new HorizontalPanel();

		VerticalPanel actionsLayout = new VerticalPanel();
		Label actionsLabel = new Label(constants.userlog_actions());
		final ListBox actions = new ListBox();

		actions.addItem(constants.userlog_allActions(), "ALL");
		for (int i = 0; i < LogEntry.ACTIONS.length; i++) {
			// TODO action i18n
			actions.addItem(LogEntry.ACTIONS[i].replace('.', ' '),
					LogEntry.ACTIONS[i]);
		}

		actions.setVisibleItemCount(1);

		actionsLayout.add(actionsLabel);
		actionsLayout.add(actions);

		HorizontalPanel dateFilterLayout = new HorizontalPanel();
		VerticalPanel initialDateLayout = new VerticalPanel();
		Label initialDateLabel = new Label(constants.userlog_initialDate());
		final DatePicker initialDatePicker = new DatePicker(true);
		initialDateLayout.add(initialDateLabel);
		initialDateLayout.add(initialDatePicker);

		VerticalPanel finalDateLayout = new VerticalPanel();
		Label finalDateLabel = new Label(constants.userlog_finalDate());
		final DatePicker finalDatePicker = new DatePicker(false);
		finalDateLayout.add(finalDateLabel);
		finalDateLayout.add(finalDatePicker);

		WUIButton setFilter = new WUIButton(constants.userlog_setFilter(),
				WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);

		setFilter.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {

				String selectedAction = actions.getValue(actions
						.getSelectedIndex());
				action = selectedAction.equals("ALL") ? null : selectedAction;

				if (initialDatePicker.isValid()) {
					dateInitial = initialDatePicker.getISODate()
							+ "T00:00:00.000";
				} else {
					dateInitial = null;
				}

				if (finalDatePicker.isValid()) {
					dateFinal = finalDatePicker.getISODate() + "T23:59:59.999";
				} else {
					dateFinal = null;
				}

				lazyList.setFilter(getFilter());
				lazyList.reset();

			}

		});

		dateFilterLayout.add(initialDateLayout);
		dateFilterLayout.add(finalDateLayout);

		pickers.add(actionsLayout);
		pickers.add(dateFilterLayout);

		filters_layout.add(pickers, DockPanel.CENTER);
		filters_layout.add(setFilter, DockPanel.SOUTH);

		actionsLayout.addStyleName("actions-layout");
		actionsLabel.addStyleName("actions-label");
		actions.addStyleName("actions-picker");
		initialDateLayout.addStyleName("date-initial-layout");
		initialDateLabel.addStyleName("date-initial-label");
		initialDatePicker.addStyleName("date-initial-picker");
		finalDateLayout.addStyleName("date-final-layout");
		finalDateLabel.addStyleName("date-final-label");
		finalDatePicker.addStyleName("date-final-picker");
		setFilter.addStyleName("filter-set");
	}

	private void addLogHeaders() {
		ListHeaderPanel lazyListHeader = lazyList.getHeader();
		lazyListHeader.addHeader(constants.actionReportLogDateTime(),
				"log-header-dateTime", new SortParameter[] {
						new SortParameter("datetime", false),
						new SortParameter("id", false) }, false);

		lazyListHeader.addHeader(constants.actionReportLogAction(),
				"log-header-action", new SortParameter[] {
						new SortParameter("action", false),
						new SortParameter("id", false) }, true);

		lazyListHeader.addHeader(constants.actionReportLogParameters(),
				"log-header-parameters", new SortParameter[] {}, true);

		if (user == null) {
			lazyListHeader.addHeader(constants.actionReportLogUser(),
					"log-header-user", new SortParameter[] {
							new SortParameter("username", false),
							new SortParameter("id", false) }, true);
		}

		lazyListHeader.setSelectedHeader(0);
		lazyListHeader.setFillerHeader(2);

	}

	protected Filter getFilter() {
		List<FilterParameter> parameters = new Vector<FilterParameter>();
		if (user != null) {
			parameters
					.add(new SimpleFilterParameter("username", user.getName()));
		}
		if (action != null) {
			parameters.add(new SimpleFilterParameter("action", action));
		}
		if (dateInitial != null || dateFinal != null) {
			parameters.add(new RangeFilterParameter("datetime", dateInitial,
					dateFinal));
		}

		return new Filter(parameters.toArray(new FilterParameter[parameters
				.size()]));
	}

	public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
		UserLogin.getInstance().checkRoles(
				new HistoryResolver[] { UserLog.getInstance() }, false,
				callback);
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
		return Management.getInstance().getHistoryPath() + "."
				+ getHistoryToken();
	}

	public String getHistoryToken() {
		return "log";
	}

	/**
	 * Log Entry Panel
	 */
	public class LogEntryPanel extends ElementPanel<LogEntry> {

		private final HorizontalPanel rowLayout;
		private final Label dateTime;
		private final Label action;
		private final Label parameters;
		private final Label user;

		/**
		 * Create a new log entry panel
		 * 
		 * @param logEntry
		 */
		public LogEntryPanel(LogEntry logEntry, boolean showUser) {
			super(logEntry);

			rowLayout = new HorizontalPanel();
			setWidget(rowLayout);

			dateTime = new Label();
			action = new Label();
			parameters = new Label();
			

			user = new Label();

			rowLayout.add(dateTime);
			rowLayout.add(action);
			rowLayout.add(parameters);
			if(showUser) {
			rowLayout.add(user);
			}

			rowLayout.setCellWidth(parameters, "100%");

			this.addStyleName("log-entry");
			rowLayout.addStyleName("log-entries-row");
			dateTime.addStyleName("dateTime");
			action.addStyleName("action");
			parameters.addStyleName("parameters");
			user.addStyleName("user");

			update(logEntry);
		}

		protected void update(LogEntry logEntry) {
			dateTime.setText(logEntry.getDatetime());
			action.setText(logEntry.getAction().replace('.', ' '));
			String parametersText = "";
			for (int j = 0; j < logEntry.getParameters().length; j++) {
				String name = logEntry.getParameters()[j].getName();
				String value = logEntry.getParameters()[j].getValue();
				parametersText += messages.logParameter(name, value) + "\n";
			}
			parameters.setText(parametersText);
			user.setText(logEntry.getUsername());

		}

	}
}
