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
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
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
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class InternalProcess extends Composite {

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
      return "internal";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static InternalProcess instance = null;

  interface MyUiBinder extends UiBinder<Widget, InternalProcess> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel internalProcessDescription;

  @UiField(provided = true)
  JobList jobList;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField(provided = true)
  FlowPanel facetsPanel;

  private InternalProcess() {

    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INTERNAL.toString()));

    jobList = new JobList("InternalProcess_jobs", filter, messages.jobList(), false);

    facetsPanel = new FlowPanel();
    FacetUtils.bindFacets(jobList, facetsPanel);

    initWidget(uiBinder.createAndBindUi(this));

    internalProcessDescription.add(new HTMLWidgetWrapper("InternalProcessDescription.html"));

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

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static InternalProcess getInstance() {
    if (instance == null) {
      instance = new InternalProcess();
    }

    return instance;
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
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INTERNAL.toString()));
    jobList.setFilter(filter);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
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
