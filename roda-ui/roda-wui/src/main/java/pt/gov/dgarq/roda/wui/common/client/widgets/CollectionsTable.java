package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;

public class CollectionsTable extends AsyncTableCell<SimpleDescriptionObject> {

	private static final int PAGE_SIZE = 20;

	private final ClientLogger logger = new ClientLogger(getClass().getName());

	private final Column<SimpleDescriptionObject, SafeHtml> levelColumn;
	private final TextColumn<SimpleDescriptionObject> titleColumn;
	private final Column<SimpleDescriptionObject, Date> dateInitialColumn;
	private final Column<SimpleDescriptionObject, Date> dateFinalColumn;

	private Filter filter;

	public CollectionsTable(Filter filter) {
		super();

		this.setFilter(filter);

		levelColumn = new Column<SimpleDescriptionObject, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(SimpleDescriptionObject sdo) {
				SafeHtml ret;
				if (sdo == null) {
					logger.error("Trying to display a NULL item");
					ret = null;
				} else {
					ret = DescriptionLevelUtils.getElementLevelIconSafeHtml(sdo.getLevel());
				}
				return ret;
			}
		};

		titleColumn = new TextColumn<SimpleDescriptionObject>() {

			@Override
			public String getValue(SimpleDescriptionObject sdo) {
				return sdo != null ? sdo.getTitle() + sdo.getId() : null;
			}
		};

		dateInitialColumn = new Column<SimpleDescriptionObject, Date>(
				new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM))) {
			@Override
			public Date getValue(SimpleDescriptionObject sdo) {
				return sdo != null ? sdo.getDateInitial() : null;
			}
		};

		dateFinalColumn = new Column<SimpleDescriptionObject, Date>(
				new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM))) {
			@Override
			public Date getValue(SimpleDescriptionObject sdo) {
				return sdo != null ? sdo.getDateFinal() : null;
			}
		};

		levelColumn.setSortable(true);
		titleColumn.setSortable(true);
		dateFinalColumn.setSortable(true);
		dateInitialColumn.setSortable(true);

		// TODO externalize strings into constants
		getDisplay().addColumn(levelColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-tag'></i>"));
		getDisplay().addColumn(titleColumn, "Title");
		getDisplay().addColumn(dateInitialColumn, "Date initial");
		getDisplay().addColumn(dateFinalColumn, "Date final");
		getDisplay().setColumnWidth(levelColumn, "35px");
		// getDisplay().setAutoHeaderRefreshDisabled(true);
		Label emptyInfo = new Label("No items to display.");
		getDisplay().setEmptyTableWidget(emptyInfo);
		getDisplay().setColumnWidth(titleColumn, "100%");

		addStyleName("my-collections-table");
		emptyInfo.addStyleName("my-collections-empty-info");

	}

	@Override
	protected void getData(int start, int length, ColumnSortList columnSortList,
			AsyncCallback<IndexResult<SimpleDescriptionObject>> callback) {
		Sorter sorter = new Sorter();
		for (int i = 0; i < columnSortList.size(); i++) {
			ColumnSortInfo columnSortInfo = columnSortList.get(i);
			String sortParameterKey;
			if (columnSortInfo.getColumn().equals(levelColumn)) {
				sortParameterKey = RodaConstants.SDO_LEVEL;
			} else if (columnSortInfo.getColumn().equals(titleColumn)) {
				sortParameterKey = RodaConstants.SDO_TITLE;
			} else if (columnSortInfo.getColumn().equals(dateInitialColumn)) {
				sortParameterKey = RodaConstants.SDO_DATE_INITIAL;
			} else if (columnSortInfo.getColumn().equals(dateFinalColumn)) {
				sortParameterKey = RodaConstants.SDO_DATE_FINAL;
			} else {
				sortParameterKey = null;
			}

			if (sortParameterKey != null) {
				sorter.add(new SortParameter(sortParameterKey, !columnSortInfo.isAscending()));
			} else {
				logger.warn("Selecting a sorter that is not mapped");
			}
		}

		sorter.add(new SortParameter(RodaConstants.SDO_TITLE, false));
		Sublist sublist = new Sublist(start, length);

		// Filter filter = new Filter();
		// if (parentId == null) {
		// filter.add(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
		// } else {
		// filter.add(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID,
		// parentId));
		// }

		BrowserService.Util.getInstance().findDescriptiveMetadata(getFilter(), sorter, sublist, callback);

	}

	@Override
	protected ProvidesKey<SimpleDescriptionObject> getKeyProvider() {
		return new ProvidesKey<SimpleDescriptionObject>() {

			@Override
			public Object getKey(SimpleDescriptionObject item) {
				return item.getId();
			}
		};
	}

	@Override
	protected int getInitialPageSize() {
		return PAGE_SIZE;
	}

	private void refresh() {
		getSelectionModel().clear();
		getDisplay().setVisibleRangeAndClearData(new Range(0, getInitialPageSize()), true);
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
		refresh();
	}

}
