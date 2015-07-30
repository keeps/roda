package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.IngestListConstants;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.ingest.list.client.IngestListService;

public class SIPStateList extends AsyncTableCell<SIPState> {

	private static final int PAGE_SIZE = 20;

	private static IngestListConstants constants = GWT.create(IngestListConstants.class);

	private final ClientLogger logger = new ClientLogger(getClass().getName());

	private final TextColumn<SIPState> idColumn;
	private final TextColumn<SIPState> originalFilenameColumn;
	private final Column<SIPState, Date> submissionDateColumn;
	private final TextColumn<SIPState> currentStateColumn;
	private final TextColumn<SIPState> percentageColumn;
	private final TextColumn<SIPState> producerColumn;

	public SIPStateList() {
		super();

		idColumn = new TextColumn<SIPState>() {

			@Override
			public String getValue(SIPState sip) {
				return sip != null ? sip.getId() : null;
			}
		};

		originalFilenameColumn = new TextColumn<SIPState>() {

			@Override
			public String getValue(SIPState sip) {
				return sip != null ? sip.getOriginalFilename() : null;
			}
		};

		submissionDateColumn = new Column<SIPState, Date>(
				new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM))) {
			@Override
			public Date getValue(SIPState sip) {
				return sip != null ? sip.getDatetime() : null;
			}
		};
		currentStateColumn = new TextColumn<SIPState>() {

			@Override
			public String getValue(SIPState sip) {
				return sip != null ? sip.getState() : null;
			}
		};
		percentageColumn = new TextColumn<SIPState>() {

			@Override
			public String getValue(SIPState sip) {
				return sip != null ? sip.getCompletePercentage() + "%" : null;
			}
		};
		producerColumn = new TextColumn<SIPState>() {

			@Override
			public String getValue(SIPState sip) {
				return sip != null ? sip.getUsername() : null;
			}
		};

		idColumn.setSortable(true);
		originalFilenameColumn.setSortable(true);
		submissionDateColumn.setSortable(true);

		// TODO externalize strings into constants
		getDisplay().addColumn(idColumn, "Id");
		getDisplay().addColumn(originalFilenameColumn, constants.headerFilename());
		getDisplay().addColumn(submissionDateColumn, constants.headerStartDate());
		getDisplay().addColumn(currentStateColumn, constants.headerState());
		getDisplay().addColumn(percentageColumn, constants.headerPercentage());
		getDisplay().addColumn(producerColumn, constants.headerProducer());

		// getDisplay().setAutoHeaderRefreshDisabled(true);
		Label emptyInfo = new Label("No items to display");
		getDisplay().setEmptyTableWidget(emptyInfo);
		getDisplay().setColumnWidth(originalFilenameColumn, "100%");

		addStyleName("my-list-sipstate");
		emptyInfo.addStyleName("my-list-sipstate-empty-info");

	}

	@Override
	protected void getData(int start, int length, ColumnSortList columnSortList,
			AsyncCallback<IndexResult<SIPState>> callback) {

		Filter filter = getFilter();

		// calculate sorter
		Sorter sorter = new Sorter();
		for (int i = 0; i < columnSortList.size(); i++) {
			ColumnSortInfo columnSortInfo = columnSortList.get(i);
			String sortParameterKey;
			if (columnSortInfo.getColumn().equals(idColumn)) {
				sortParameterKey = "id"; // RodaConstants.SIP_ID;
			} else if (columnSortInfo.getColumn().equals(originalFilenameColumn)) {
				sortParameterKey = "originalFilename";
			} else if (columnSortInfo.getColumn().equals(submissionDateColumn)) {
				sortParameterKey = "datetime";
			} else if (columnSortInfo.getColumn().equals(currentStateColumn)) {
				sortParameterKey = "state";
			} else if (columnSortInfo.getColumn().equals(percentageColumn)) {
				sortParameterKey = "completePercentage";
			} else if (columnSortInfo.getColumn().equals(producerColumn)) {
				sortParameterKey = "username";
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

		GWT.log("Requesting ingest getSIPs");

		IngestListService.Util.getInstance().getSIPs(filter, sorter, sublist, callback);

	}

	@Override
	protected ProvidesKey<SIPState> getKeyProvider() {
		return new ProvidesKey<SIPState>() {

			@Override
			public Object getKey(SIPState item) {
				return item.getId();
			}
		};
	}

	@Override
	protected int getInitialPageSize() {
		return PAGE_SIZE;
	}

}
