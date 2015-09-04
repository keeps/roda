package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.Browse;
import pt.gov.dgarq.roda.wui.management.user.client.UserManagementService;

public class LogEntryList extends AsyncTableCell<LogEntry> {

	private static final int PAGE_SIZE = 20;

	private final ClientLogger logger = new ClientLogger(getClass().getName());

	private final Column<LogEntry, Date> dateColumn;
	private final TextColumn<LogEntry> actionComponentColumn;
	private final TextColumn<LogEntry> actionMethodColumn;
	private final Column<LogEntry, String> relatedObjectColumn;
	private final TextColumn<LogEntry> usernameColumn;
	private final TextColumn<LogEntry> durationColumn;
	private final TextColumn<LogEntry> addressColumn;

	public LogEntryList() {
		this(null, null);
	}

	public LogEntryList(Filter filter, Facets facets) {
		super(filter, facets,"LOGS");

		dateColumn = new Column<LogEntry, Date>(new DateCell(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS"))) {
			@Override
			public Date getValue(LogEntry logEntry) {
				return logEntry != null ? logEntry.getDatetime() : null;
			}
		};

		actionComponentColumn = new TextColumn<LogEntry>() {

			@Override
			public String getValue(LogEntry logEntry) {
				return logEntry != null ? logEntry.getActionComponent() : null;
			}
		};

		actionMethodColumn = new TextColumn<LogEntry>() {

			@Override
			public String getValue(LogEntry logEntry) {
				return logEntry != null ? logEntry.getActionMethod() : null;
			}
		};

		relatedObjectColumn = new Column<LogEntry, String>(new ClickableTextCell()) {

			@Override
			public String getValue(LogEntry logEntry) {
				return logEntry != null ? logEntry.getRelatedObjectID() : null;
			}
		};

		relatedObjectColumn.setFieldUpdater(new FieldUpdater<LogEntry, String>() {

			@Override
			public void update(int index, LogEntry logEntry, String value) {
				if (logEntry != null && logEntry.getRelatedObjectID() != null) {
					History.newItem(Browse.getViewItemHistoryToken(logEntry.getRelatedObjectID()));
				}
			}
		});

		usernameColumn = new TextColumn<LogEntry>() {

			@Override
			public String getValue(LogEntry logEntry) {
				return logEntry != null ? logEntry.getUsername() : null;
			}
		};

		durationColumn = new TextColumn<LogEntry>() {

			@Override
			public String getValue(LogEntry logEntry) {
				// FIXME
				return logEntry != null ? logEntry.getDuration() + "ms" : null;
			}
		};

		addressColumn = new TextColumn<LogEntry>() {

			@Override
			public String getValue(LogEntry logEntry) {
				return logEntry != null ? logEntry.getAddress() : null;
			}
		};

		dateColumn.setSortable(true);
		actionComponentColumn.setSortable(true);
		actionMethodColumn.setSortable(true);
		relatedObjectColumn.setSortable(true);
		usernameColumn.setSortable(true);
		durationColumn.setSortable(true);
		addressColumn.setSortable(true);

		// TODO externalize strings into constants

		// getDisplay().addColumn(idColumn, "Id");
		getDisplay().addColumn(dateColumn, "Date and time");
		getDisplay().addColumn(actionComponentColumn, "Component");
		getDisplay().addColumn(actionMethodColumn, "Method");
		getDisplay().addColumn(relatedObjectColumn, "Related object");
		getDisplay().addColumn(usernameColumn, "User");
		getDisplay().addColumn(durationColumn, "Duration");
		getDisplay().addColumn(addressColumn, "Address");

		Label emptyInfo = new Label("No items to display");
		getDisplay().setEmptyTableWidget(emptyInfo);

		addStyleName("my-collections-table");
		emptyInfo.addStyleName("my-collections-empty-info");
		relatedObjectColumn.setCellStyleNames("my-collections-table-cell-link");

	}

	@Override
	protected void getData(int start, int length, ColumnSortList columnSortList,
			AsyncCallback<IndexResult<LogEntry>> callback) {

		Filter filter = getFilter();

		// calculate sorter
		Sorter sorter = new Sorter();
		for (int i = 0; i < columnSortList.size(); i++) {
			ColumnSortInfo columnSortInfo = columnSortList.get(i);
			String sortParameterKey;
			if (columnSortInfo.getColumn().equals(dateColumn)) {
				sortParameterKey = RodaConstants.LOG_DATETIME;
			} else if (columnSortInfo.getColumn().equals(actionComponentColumn)) {
				sortParameterKey = RodaConstants.LOG_ACTION_COMPONENT;
			} else if (columnSortInfo.getColumn().equals(actionMethodColumn)) {
				sortParameterKey = RodaConstants.LOG_ACTION_METHOD;
			} else if (columnSortInfo.getColumn().equals(usernameColumn)) {
				sortParameterKey = RodaConstants.LOG_USERNAME;
			} else if (columnSortInfo.getColumn().equals(durationColumn)) {
				sortParameterKey = RodaConstants.LOG_DURATION;
			} else if (columnSortInfo.getColumn().equals(addressColumn)) {
				sortParameterKey = RodaConstants.LOG_ADDRESS;
			} else {
				sortParameterKey = null;
			}

			if (sortParameterKey != null) {
				sorter.add(new SortParameter(sortParameterKey, !columnSortInfo.isAscending()));
			} else {
				logger.warn("Selecting a sorter that is not mapped");
			}
		}

		// define sublist
		Sublist sublist = new Sublist(start, length);

		UserManagementService.Util.getInstance().findLogEntries(filter, sorter, sublist, getFacets(), callback);

	}

	@Override
	protected ProvidesKey<LogEntry> getKeyProvider() {
		return new ProvidesKey<LogEntry>() {

			@Override
			public Object getKey(LogEntry item) {
				return item.getId();
			}
		};
	}

	@Override
	protected int getInitialPageSize() {
		return PAGE_SIZE;
	}

}
