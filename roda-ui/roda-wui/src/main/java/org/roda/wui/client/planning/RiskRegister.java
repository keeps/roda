/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.planning;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {RiskRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "riskregister";
    }
  };

  private static RiskRegister instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static RiskRegister getInstance() {
    if (instance == null) {
      instance = new RiskRegister();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, RiskRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  FlowPanel riskRegisterDescription;


  // @UiField(provided = true)
  // LogEntryList logList;

  @UiField(provided = true)
  FlowPanel facetCategories;

  @UiField(provided = true)
  FlowPanel facetSeverities;
  
  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonEdit;
  
  @UiField
  Button buttonRemove;
  
  @UiField
  Button startProcess;
  
  /**
   * Create a risk register page
   *
   * @param user
   */
  public RiskRegister() {
    // Filter filter = null;
    // Facets facets = new Facets(new
    // SimpleFacetParameter(RodaConstants.LOG_ACTION_COMPONENT),
    // new SimpleFacetParameter(RodaConstants.LOG_ACTION_METHOD), new
    // SimpleFacetParameter(RodaConstants.LOG_USERNAME));

    // logList = new LogEntryList(filter, facets, "Logs", false);

    facetCategories = new FlowPanel();
    facetSeverities = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.LOG_ACTION_COMPONENT, facetCategories);
    facetPanels.put(RodaConstants.LOG_ACTION_METHOD, facetSeverities);
    // FacetUtils.bindFacets(logList, facetPanels);

    // logList.getSelectionModel().addSelectionChangeHandler(new
    // SelectionChangeEvent.Handler() {
    //
    // @Override
    // public void onSelectionChange(SelectionChangeEvent event) {
    // LogEntry selected = logList.getSelectionModel().getSelectedObject();
    // if (selected != null) {
    // Tools.newHistory(ShowLogEntry.RESOLVER, selected.getId());
    // }
    // }
    // });

    initWidget(uiBinder.createAndBindUi(this));

    riskRegisterDescription.add(new HTMLWidgetWrapper("RiskRegisterDescription.html"));
    
    buttonEdit.setEnabled(false);
    buttonRemove.setEnabled(false);

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    ValueChangeHandler<Date> valueChangeHandler = new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        updateDateFilter();
      }

    };

    inputDateInitial.setFormat(dateFormat);
    inputDateInitial.getDatePicker().setYearArrowsVisible(true);
    inputDateInitial.setFireNullValues(true);
    inputDateInitial.addValueChangeHandler(valueChangeHandler);

    inputDateFinal.setFormat(dateFormat);
    inputDateFinal.getDatePicker().setYearArrowsVisible(true);
    inputDateFinal.setFireNullValues(true);
    inputDateFinal.addValueChangeHandler(valueChangeHandler);

    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());
  }

  private void updateDateFilter() {
    // Date dateInitial = inputDateInitial.getDatePicker().getValue();
    // Date dateFinal = inputDateFinal.getDatePicker().getValue();
    //
    // DateRangeFilterParameter filterParameter = new
    // DateRangeFilterParameter(RodaConstants.LOG_DATETIME, dateInitial,
    // dateFinal, RodaConstants.DateGranularity.DAY);
    //
    // logList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      // logList.refresh();
      callback.onSuccess(this);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  protected void updateVisibles() {
    // TODO selection control
    buttonEdit.setEnabled(true);
    buttonRemove.setEnabled(true);
  }
}
