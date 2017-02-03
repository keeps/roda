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
package org.roda.wui.client.process;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.JobList;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.Management;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class ActionProcess extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "process";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static ActionProcess instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static ActionProcess getInstance() {
    if (instance == null) {
      instance = new ActionProcess();
    }

    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, ActionProcess> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel ingestProcessDescription;

  @UiField(provided = true)
  JobList jobList;

  @UiField(provided = true)
  FlowPanel stateFacets;

  @UiField(provided = true)
  FlowPanel producerFacets;

  @UiField(provided = true)
  FlowPanel jobTypeFacets;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField
  Button newJob;

  private ActionProcess() {

    Filter filter = new Filter(
      new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INGEST.toString()));

    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.JOB_STATE),
      new SimpleFacetParameter(RodaConstants.JOB_USERNAME), new SimpleFacetParameter(RodaConstants.JOB_PLUGIN_TYPE));

    jobList = new JobList(filter, facets, messages.jobList(), false);
    producerFacets = new FlowPanel();
    stateFacets = new FlowPanel();
    jobTypeFacets = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.JOB_STATE, stateFacets);
    facetPanels.put(RodaConstants.JOB_USERNAME, producerFacets);
    facetPanels.put(RodaConstants.JOB_PLUGIN_TYPE, jobTypeFacets);
    FacetUtils.bindFacets(jobList, facetPanels);

    initWidget(uiBinder.createAndBindUi(this));

    ingestProcessDescription.add(new HTMLWidgetWrapper("IngestProcessDescription.html"));

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    ValueChangeHandler<Date> valueChangeHandler = new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        updateDateFilter();
      }

    };

    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());

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

    jobList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Job job = jobList.getSelectionModel().getSelectedObject();
        if (job != null) {
          HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
        }
      }
    });

    jobList.autoUpdate(10000);

  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.JOB_START_DATE, dateInitial,
      dateFinal, RodaConstants.DateGranularity.DAY);

    Filter filter = new Filter(filterParameter);
    filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INGEST.toString()));
    jobList.setFilter(filter);
  }

  @UiHandler("newJob")
  void handleNewJobAction(ClickEvent e) {
    HistoryUtils.newHistory(CreateDefaultJob.RESOLVER);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      jobList.refresh();
      callback.onSuccess(this);
    } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(ShowJob.RESOLVER.getHistoryToken())) {
      ShowJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

}
