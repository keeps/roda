package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.Date;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ProvidesKey;

import config.i18n.client.CommonConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelInfo;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTreeVerticalScrollPanel;
import pt.gov.dgarq.roda.wui.main.client.Main;

public class CollectionsDataGrid extends AsyncDataGrid<SimpleDescriptionObject> {

	public CollectionsDataGrid() {
		super();

		Column<SimpleDescriptionObject, String> levelColumn = new Column<SimpleDescriptionObject, String>(
				new ImageCell()) {
			@Override
			public String getValue(SimpleDescriptionObject sdo) {
				// return resources.getImageResource();
				return getElementLevelIcon(sdo.getLevel());
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

	}

	/**
	 * Get description level icon
	 * 
	 * @param level
	 * @return the icon message
	 */
	public String getElementLevelIcon(String level) {
		String ret;
		final DescriptionLevelInfo levelInfo = Main.getDescriptionLevel(level);
		if (levelInfo != null) {
			ret = GWT.getModuleBaseURL() + "description_levels/" + levelInfo.getCategory().getCategory() + ".png";

		} else {
			ret = GWT.getModuleBaseURL() + "description_levels/default.png";
		}
		return ret;
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

}
