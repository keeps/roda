/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.advanced.client;

import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.search.DateRangeSearchParameter;
import pt.gov.dgarq.roda.core.data.search.EadcSearchFields;
import pt.gov.dgarq.roda.wui.common.client.widgets.DatePicker;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.AdvancedSearchConstants;

/**
 * @author Luis Faria
 * 
 */
public class DateIntervalPicker extends DockPanel {

  private static AdvancedSearchConstants constants = (AdvancedSearchConstants) GWT
    .create(AdvancedSearchConstants.class);

  private final VerticalPanel optionsLayout;

  private final RadioButton anyDateInterval;

  private final RadioButton chooseDateInterval;

  private final HorizontalPanel centralLayout;

  private final Label fromDateLabel;

  private final DatePicker startDatePicker;

  private final Label toDateLabel;

  private final DatePicker endDatePicker;

  /**
   * Create new date interval picker
   */
  public DateIntervalPicker() {
    optionsLayout = new VerticalPanel();
    anyDateInterval = new RadioButton("date-interval-option", constants.anyDateInterval());
    chooseDateInterval = new RadioButton("date-interval-option", constants.chooseDateInterval());
    optionsLayout.add(anyDateInterval);
    optionsLayout.add(chooseDateInterval);

    centralLayout = new HorizontalPanel();
    this.fromDateLabel = new Label(constants.from());
    this.startDatePicker = new DatePicker(true);
    this.toDateLabel = new Label(constants.to());
    this.endDatePicker = new DatePicker(false);

    centralLayout.add(fromDateLabel);
    centralLayout.add(startDatePicker);
    centralLayout.add(toDateLabel);
    centralLayout.add(endDatePicker);

    this.add(optionsLayout, WEST);
    this.add(centralLayout, CENTER);

    anyDateInterval.setChecked(true);
    centralLayout.setVisible(false);

    ClickListener optionListener = new ClickListener() {

      public void onClick(Widget sender) {
        centralLayout.setVisible(chooseDateInterval.isChecked());
      }

    };
    anyDateInterval.addClickListener(optionListener);
    chooseDateInterval.addClickListener(optionListener);

    addStyleName("wui-dateIntervalPicker");
    optionsLayout.addStyleName("dateInterval-option-layout");
    centralLayout.addStyleName("dateInterval-choose-layout");
    fromDateLabel.addStyleName("label-from");
    toDateLabel.addStyleName("label-to");
  }

  /**
   * Get start ISO date
   * 
   * @return the date in ISO 8601
   */
  public String getStartISODate() {
    return startDatePicker.getISODate();
  }

  /**
   * Get end ISO date
   * 
   * @return the date in ISO 8601
   */
  public String getEndISODate() {
    return endDatePicker.getISODate();
  }

  /**
   * Is the date interval valid
   * 
   * @return true if valid
   */
  public boolean isValid() {
    boolean ret = true;

    if (startDatePicker.isValid() && endDatePicker.isValid()) {
      ret = startDatePicker.getDate().compareTo(endDatePicker.getDate()) >= 0;
    }

    return ret;
  }

  /**
   * Get search parameters
   * 
   * @return the search parameters
   */
  public SearchParameter[] getSearchParameters() {
    SearchParameter[] parameters;
    if (anyDateInterval.isChecked()) {
      parameters = new SearchParameter[] {};
    } else {
      String startDate = startDatePicker.isValid() ? startDatePicker.getISODate() : null;
      String endDate = endDatePicker.isValid() ? endDatePicker.getISODate() : null;
      if (startDate == null && endDate == null) {
        parameters = new SearchParameter[] {};
      } else {
        SearchParameter parameter = new DateRangeSearchParameter(EadcSearchFields.UNITDATE, startDate, endDate);
        parameters = new SearchParameter[] {parameter};
      }
    }

    return parameters;
  }

}
