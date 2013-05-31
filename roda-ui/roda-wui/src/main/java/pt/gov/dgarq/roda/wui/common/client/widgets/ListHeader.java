/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class ListHeader extends Composite implements SourcesClickEvents {

	private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT
			.create(CommonImageBundle.class);

	private final FocusPanel focus;
	private final HorizontalPanel layout;
	private final Label label;
	private final Image sortDirection;
	private final boolean defaultSortDirection;
	private Boolean ascending;
	private SortParameter[] sortParameters;

	/**
	 * Create a new list header
	 * 
	 * @param text
	 *            the text to show in header
	 * @param stylename
	 *            the style name to use
	 * @param sortParameters
	 *            the ascending sort parameters
	 * @param defaultSortDirection
	 *            the default sort direction, true for ascending, false for
	 *            descending, null if does not apply.
	 */
	public ListHeader(String text, String stylename,
			SortParameter[] sortParameters, boolean defaultSortDirection) {
		focus = new FocusPanel();
		layout = new HorizontalPanel();
		label = new Label(text);
		sortDirection = new Image();
		this.sortParameters = sortParameters;
		this.defaultSortDirection = defaultSortDirection;

		focus.setWidget(layout);
		if (text.length() > 0) {
			layout.add(label);
			layout.setCellWidth(label, "100%");
			layout.setCellVerticalAlignment(label, HasAlignment.ALIGN_MIDDLE);
		} else {
			layout.add(new HTML("&nbsp;"));
		}
		layout.add(sortDirection);

		setAscending(null);

		initWidget(getWidget());

		layout.setCellVerticalAlignment(sortDirection,
				HasAlignment.ALIGN_MIDDLE);

		focus.addStyleName("lazy-list-header");
		focus.addStyleName(stylename);
		layout.addStyleName("lazy-list-header-layout");
		label.addStyleName("lazy-list-header-label");
		sortDirection.setStylePrimaryName("lazy-list-header-direction");

	}

	/**
	 * Is sort direction ascending
	 * 
	 * @return true if ascending
	 */
	public Boolean isAscending() {
		return ascending;
	}

	/**
	 * Get default sort direction
	 * 
	 * @return true if it ascending is the default sort direction
	 */
	public boolean getDefaultSortDirection() {
		return defaultSortDirection;
	}

	/**
	 * Set sort direction
	 * 
	 * @param ascending
	 *            true if sort direction ascending, false if sort direction
	 *            descending, and null if sort direction not applied
	 */
	public void setAscending(Boolean ascending) {
		this.ascending = ascending;

		if (sortParameters.length == 0) {
			// no image
			sortDirection.setUrl("clear.cache.gif");
		} else if (ascending == null) {
			commonImageBundle.listSortDirection().applyTo(sortDirection);
			sortDirection.addStyleDependentName("unsorted");
		} else if (ascending) {
			commonImageBundle.listAscending().applyTo(sortDirection);
			sortDirection.removeStyleDependentName("unsorted");
		} else {
			commonImageBundle.listDescending().applyTo(sortDirection);
			sortDirection.removeStyleDependentName("unsorted");
		}
	}

	/**
	 * Get sort parameters
	 * 
	 * @return A list of {@link SortParameter}s
	 */
	public SortParameter[] getSortParameters() {
		SortParameter[] parameters;
		if (ascending == null) {
			parameters = new SortParameter[] {};
		} else {
			parameters = new SortParameter[sortParameters.length];
			for (int i = 0; i < sortParameters.length; i++) {
				parameters[i] = new SortParameter(sortParameters[i]);
				if (!ascending) {
					parameters[i].setDescending(!parameters[i].isDescending());
				}
			}
		}

		return parameters;
	}

	public void addClickListener(ClickListener listener) {
		if (sortParameters.length > 0) {
			focus.addClickListener(listener);
		}
	}

	public void removeClickListener(ClickListener listener) {
		if (sortParameters.length > 0) {
			focus.removeClickListener(listener);
		}
	}

	/**
	 * Get the widget
	 * 
	 * @return the list header widget
	 */
	public Widget getWidget() {
		return focus;
	}

	public String toString() {
		return label.getText();
	}
}
