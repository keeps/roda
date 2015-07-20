package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTreeVerticalScrollPanel;

public class CollectionsTable extends AsyncTableCell<SimpleDescriptionObject> {

	private static final int PAGE_SIZE = 25;

	private final ClientLogger logger = new ClientLogger(getClass().getName());

	public CollectionsTable(Unit unit) {
		this();
	}

	public CollectionsTable() {
		super();

		Column<SimpleDescriptionObject, String> levelColumn = new Column<SimpleDescriptionObject, String>(
				new ImageCell()) {
			@Override
			public String getValue(SimpleDescriptionObject sdo) {
				String ret;
				if (sdo == null) {
					logger.error("Trying to display a NULL collection");
					ret = null;
				} else {
					ret = DescriptionLevelUtils.getElementLevelIconPath(sdo.getLevel());
				}
				return ret;
			}
		};

		TextColumn<SimpleDescriptionObject> titleColumn = new TextColumn<SimpleDescriptionObject>() {

			@Override
			public String getValue(SimpleDescriptionObject sdo) {
				return sdo != null ? sdo.getTitle() : null;
			}
		};

		Column<SimpleDescriptionObject, Date> dateInitialColumn = new Column<SimpleDescriptionObject, Date>(
				new DateCell()) {
			@Override
			public Date getValue(SimpleDescriptionObject sdo) {
				return sdo != null ? sdo.getDateInitial() : null;
			}
		};

		Column<SimpleDescriptionObject, Date> dateFinalColumn = new Column<SimpleDescriptionObject, Date>(
				new DateCell()) {
			@Override
			public Date getValue(SimpleDescriptionObject sdo) {
				return sdo != null ? sdo.getDateFinal() : null;
			}
		};

		// TODO externalize strings into constants
		getDisplay().addColumn(levelColumn);
		getDisplay().addColumn(titleColumn, "Title");
		getDisplay().addColumn(dateInitialColumn, "Date initial");
		getDisplay().addColumn(dateFinalColumn, "Date final");
		getDisplay().setColumnWidth(levelColumn, "35px");
		getDisplay().setAutoHeaderRefreshDisabled(true);
		getDisplay().setEmptyTableWidget(new Label("Table is empty"));
		getDisplay().setColumnWidth(titleColumn, "100%");

		addStyleName("my-collections-table");

	}

	@Override
	protected void getData(int start, int length, AsyncCallback<IndexResult<SimpleDescriptionObject>> callback) {
		Filter filter = CollectionsTreeVerticalScrollPanel.DEFAULT_FILTER;
		Sorter sorter = CollectionsTreeVerticalScrollPanel.DEFAULT_SORTER;
		Sublist sublist = new Sublist(start, length);
		BrowserService.Util.getInstance().findCollections(filter, sorter, sublist, callback);
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
	protected int getPageSize() {
		return PAGE_SIZE;
	}

}
