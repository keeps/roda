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
package org.roda.wui.client.management;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.FacetParameter.SORT;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.LogEntryList;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class UserLog extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {UserLog.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "log";
    }
  };

  private static UserLog instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static UserLog getInstance() {
    if (instance == null) {
      instance = new UserLog();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, UserLog> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel userLogDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField(provided = true)
  LogEntryList logList;

  @UiField(provided = true)
  FlowPanel facetComponents;

  @UiField(provided = true)
  FlowPanel facetMethods;

  @UiField(provided = true)
  FlowPanel facetUsers;

  @UiField(provided = true)
  FlowPanel facetStates;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(LogEntry.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(LogEntry.class.getName());

  /**
   * Create a new user log
   *
   * @param user
   */
  public UserLog() {
    FacetParameter fp1 = new SimpleFacetParameter(RodaConstants.LOG_ACTION_COMPONENT);
    fp1.setSort(SORT.COUNT);
    FacetParameter fp2 = new SimpleFacetParameter(RodaConstants.LOG_ACTION_METHOD);
    fp2.setSort(SORT.COUNT);
    FacetParameter fp3 = new SimpleFacetParameter(RodaConstants.LOG_USERNAME);
    fp3.setSort(SORT.COUNT);
    FacetParameter fp4 = new SimpleFacetParameter(RodaConstants.LOG_STATE);
    fp4.setSort(SORT.COUNT);
    Facets facets = new Facets(fp1, fp2, fp3, fp4);
    logList = new LogEntryList(Filter.NULL, facets, messages.logsTitle(), false);

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true, messages.userLogSearchPlaceHolder(), false, false,
      false);
    searchPanel.setList(logList);

    facetComponents = new FlowPanel();
    facetMethods = new FlowPanel();
    facetUsers = new FlowPanel();
    facetStates = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<>();
    facetPanels.put(RodaConstants.LOG_ACTION_COMPONENT, facetComponents);
    facetPanels.put(RodaConstants.LOG_ACTION_METHOD, facetMethods);
    facetPanels.put(RodaConstants.LOG_USERNAME, facetUsers);
    facetPanels.put(RodaConstants.LOG_STATE, facetStates);

    FacetUtils.bindFacets(logList, facetPanels);

    logList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        LogEntry selected = logList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
          HistoryUtils.newHistory(ShowLogEntry.RESOLVER, selected.getId());
        }
      }
    });

    initWidget(uiBinder.createAndBindUi(this));

    userLogDescription.add(new HTMLWidgetWrapper("UserLogDescription.html"));

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
    inputDateInitial.setTitle(messages.dateIntervalLabelInitial());

    inputDateFinal.setFormat(dateFormat);
    inputDateFinal.getDatePicker().setYearArrowsVisible(true);
    inputDateFinal.setFireNullValues(true);
    inputDateFinal.addValueChangeHandler(valueChangeHandler);
    inputDateFinal.setTitle(messages.dateIntervalLabelFinal());

    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.LOG_DATETIME, dateInitial,
      dateFinal, RodaConstants.DateGranularity.DAY);

    logList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      logList.setFilter(Filter.ALL);
      logList.refresh();
      callback.onSuccess(this);
    } else if (historyTokens.size() > 1 && ShowLogEntry.RESOLVER.getHistoryToken().equals(historyTokens.get(0))) {
      ShowLogEntry.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1) {
      final String aipId = historyTokens.get(0);
      logList.setFilter(new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, aipId)));
      logList.refresh();
      callback.onSuccess(this);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

}
