/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class ListHeaderPanel extends Composite {

	/**
	 * Interface to listen to list header panel events
	 * 
	 * @author Luis Faria
	 * 
	 */
	public interface ListHeaderListener {
		/**
		 * Set content adapter sorter and reset
		 * 
		 * @param sorter
		 */
		public void setSorter(Sorter sorter);
	}

	private ListHeaderListener listener;
	private final HorizontalPanel layout;
	private List<ListHeader> headers;

	/**
	 * Create a new List header panel
	 * 
	 * @param listener
	 */
	public ListHeaderPanel(ListHeaderListener listener) {
		this.listener = listener;
		layout = new HorizontalPanel();
		headers = new ArrayList<ListHeader>();

		initWidget(layout);

		this.addStyleName("wui-list-header");
	}

	/**
	 * Add a list header
	 * 
	 * @param headerLabel
	 *            the header label
	 * @param headerStyle
	 *            the header style name
	 * @param ascendingParameters
	 *            sorting parameters considering ascending header order
	 * @param defaultSortDirection
	 *            sort direction to be choose on first click
	 */
	public void addHeader(String headerLabel, String headerStyle, SortParameter[] ascendingParameters,
			boolean defaultSortDirection) {
		ListHeader header = new ListHeader(headerLabel, headerStyle, ascendingParameters, defaultSortDirection);
		headers.add(header);
		final int headerIndex = headers.indexOf(header);
		header.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				setSelectedHeader(headerIndex);
			}

		});

		layout.add(header);

	}

	/**
	 * Set currently selected header
	 * 
	 * @param header
	 */
	protected void setSelectedHeader(ListHeader header) {
		for (ListHeader h : headers) {
			if (h != header) {
				h.setAscending(null);
			}
		}
		if (header.isAscending() == null) {
			header.setAscending(header.getDefaultSortDirection());
		} else {
			header.setAscending(!header.isAscending());
		}

		Sorter sorter = new Sorter(header.getSortParameters());
		listener.setSorter(sorter);
	}

	/**
	 * Set currently selected header
	 * 
	 * @param headerIndex
	 */
	public void setSelectedHeader(int headerIndex) {
		setSelectedHeader(headers.get(headerIndex));
	}

	/**
	 * Set which header will stretch to fill the width
	 * 
	 * @param headerIndex
	 */
	public void setFillerHeader(int headerIndex) {
		layout.setCellWidth(layout.getWidget(headerIndex), "100%");
	}
}
