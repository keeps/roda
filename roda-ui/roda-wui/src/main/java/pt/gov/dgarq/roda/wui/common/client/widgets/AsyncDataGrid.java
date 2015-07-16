package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.io.Serializable;

import org.apache.hadoop.crypto.key.KeyProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

import pt.gov.dgarq.roda.core.data.v2.IndexResult;

public abstract class AsyncDataGrid<T extends Serializable> extends DockLayoutPanel {

	private final AsyncDataProvider<T> dataProvider;
	private final DataGrid<T> display;

	public AsyncDataGrid() {
		super(Unit.PX);

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
						// TODO treat failure
						GWT.log("Error getting data", caught);
					}

					@Override
					public void onSuccess(IndexResult<T> result) {
						GWT.log("Got result: " + result);
						if (result != null) {
							updateRowData((int) result.getOffset(), result.getResults());
							updateRowCount((int) result.getTotalCount(), true);
						} else {
							// TODO treat this option
						}
					}
				});
			}
		};

		display = new DataGrid<>(getKeyProvider());
		dataProvider.addDataDisplay(display);

		SimplePager pager = new SimplePager();
		pager.setDisplay(display);

		addNorth(pager, 30);
		add(display);

		// TODO add support for sorter
		// TODO add support for selection
	}

	protected abstract ProvidesKey<T> getKeyProvider();

	protected abstract void getData(int start, int length, AsyncCallback<IndexResult<T>> callback);

	protected DataGrid<T> getDisplay() {
		return display;
	}

}
