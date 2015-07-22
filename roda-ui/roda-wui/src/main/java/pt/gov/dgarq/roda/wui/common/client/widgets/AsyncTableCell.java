package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.io.Serializable;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.PageSizePager;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;

import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;

public abstract class AsyncTableCell<T extends Serializable> extends FlowPanel {

	private final AsyncDataProvider<T> dataProvider;
	private final SingleSelectionModel<T> selectionModel;

	private final SimplePager resultsPager;
	private final PageSizePager pageSizePager;
	private final CellTable<T> display;

	private final ClientLogger logger = new ClientLogger(getClass().getName());

	public AsyncTableCell() {
		super();

		this.dataProvider = new AsyncDataProvider<T>() {

			@Override
			protected void onRangeChanged(HasData<T> display) {
				// Get the new range.
				final Range range = display.getVisibleRange();

				// Query the data asynchronously.

				final int start = range.getStart();
				int length = range.getLength();
				getData(start, length, new AsyncCallback<IndexResult<T>>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.error("Error getting data", caught);
					}

					@Override
					public void onSuccess(IndexResult<T> result) {
						if (result != null) {
							int rowCount = (int) result.getTotalCount();
							updateRowData((int) result.getOffset(), result.getResults());
							updateRowCount(rowCount, true);
							resultsPager.setVisible(rowCount > 0);
							pageSizePager.setVisible(rowCount > 0);
						} else {
							// TODO treat this option
						}
					}
				});
			}
		};

		display = new CellTable<>(getInitialPageSize(), getKeyProvider());

		dataProvider.addDataDisplay(display);

		resultsPager = new SimplePager(TextLocation.RIGHT, false, true);
		resultsPager.setDisplay(display);

		pageSizePager = new PageSizePager(getInitialPageSize());
		pageSizePager.setDisplay(display);

		add(resultsPager);
		add(display);
		add(pageSizePager);

		selectionModel = new SingleSelectionModel<>(getKeyProvider());
		display.setSelectionModel(selectionModel);
		// TODO add support for sorter

		addStyleName("my-asyncdatagrid");
		resultsPager.addStyleName("my-asyncdatagrid-pager-results");
		pageSizePager.addStyleName("my-asyncdatagrid-pager-pagesize");
	}

	protected abstract int getInitialPageSize();

	protected abstract ProvidesKey<T> getKeyProvider();

	protected abstract void getData(int start, int length, AsyncCallback<IndexResult<T>> callback);

	protected CellTable<T> getDisplay() {
		return display;
	}

	public SingleSelectionModel<T> getSelectionModel() {
		return selectionModel;
	}

}
