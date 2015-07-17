package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.ImageCell;
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
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTreeVerticalScrollPanel;

public class CollectionsDataGrid extends AsyncDataGrid<SimpleDescriptionObject> {

	private static final int PAGE_SIZE = 25;

	public CollectionsDataGrid() {
		super();

		Column<SimpleDescriptionObject, String> levelColumn = new Column<SimpleDescriptionObject, String>(
				new ImageCell()) {
			@Override
			public String getValue(SimpleDescriptionObject sdo) {
				return DescriptionLevelUtils.getElementLevelIconPath(sdo.getLevel());
			}
		};

		TextColumn<SimpleDescriptionObject> titleColumn = new TextColumn<SimpleDescriptionObject>() {

			@Override
			public String getValue(SimpleDescriptionObject sdo) {
				return sdo.getTitle();
			}
		};

		Column<SimpleDescriptionObject, Date> dateInitialColumn = new Column<SimpleDescriptionObject, Date>(
				new DateCell()) {
			@Override
			public Date getValue(SimpleDescriptionObject sdo) {
				return sdo.getDateInitial();
			}
		};

		Column<SimpleDescriptionObject, Date> dateFinalColumn = new Column<SimpleDescriptionObject, Date>(
				new DateCell()) {
			@Override
			public Date getValue(SimpleDescriptionObject sdo) {
				return sdo.getDateFinal();
			}
		};

		getDisplay().addColumn(levelColumn);
		getDisplay().addColumn(titleColumn, "Title");
		getDisplay().addColumn(dateInitialColumn, "Date initial");
		getDisplay().addColumn(dateFinalColumn, "Date final");
		getDisplay().setColumnWidth(levelColumn, "35px");
		getDisplay().setAutoHeaderRefreshDisabled(true);
		getDisplay().setEmptyTableWidget(new Label("Table is empty"));

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
