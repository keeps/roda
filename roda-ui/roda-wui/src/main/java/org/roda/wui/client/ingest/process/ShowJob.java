package org.roda.wui.client.ingest.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoRequest;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.common.JobActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.labels.Header;
import org.roda.wui.client.common.lists.IngestJobReportList;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.process.Process;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ShowJob extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 1 && historyTokens.get(1).equals(ShowJobReport.RESOLVER.getHistoryToken())) {
        ShowJobReport.RESOLVER.resolve(historyTokens, callback);
      } else if (!historyTokens.isEmpty()) {
        String jobId = historyTokens.get(0);
        Services services = new Services("Get job plugin info", "get");
        services.jobsResource(s -> s.findByUuid(jobId, LocaleInfo.getCurrentLocale().getLocaleName())).thenCompose(
          retrievedJob -> services.jobsResource(s -> s.getJobPluginInfo(new PluginInfoRequest(retrievedJob)))
            .whenComplete((pluginInfoList, error) -> {
              if (error != null) {
                if (error.getCause() instanceof NotFoundException) {
                  Toast.showError(messages.notFoundError(), messages.jobNotFound());
                  HistoryUtils.newHistory(Process.RESOLVER);
                } else {
                  AsyncCallbackUtils.defaultFailureTreatment(error.getCause());
                }
              } else {
                Map<String, PluginInfo> pluginsInfoMap = new HashMap<>();
                for (PluginInfo pluginInfo : pluginInfoList) {
                  pluginsInfoMap.put(pluginInfo.getId(), pluginInfo);
                }
                List<FilterParameter> reportFilterParameters = new ArrayList<>();
                for (int i = 1; i < historyTokens.size() - 1; i += 2) {
                  String key = historyTokens.get(i);
                  String value = historyTokens.get(i + 1);
                  reportFilterParameters.add(new SimpleFilterParameter(key, value));
                }
                ShowJob showJob = new ShowJob(retrievedJob, pluginsInfoMap, reportFilterParameters);
                callback.onSuccess(showJob);
              }
            }));
      } else {
        HistoryUtils.newHistory(Process.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Process.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Process.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "job";
    }
  };
  // Timer Constants
  private static final int PERIOD_MILLIS = 10000;
  private static final int PERIOD_MILLIS_FAST = 2000;
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  NavigationToolbar<IndexedJob> navigationToolbar;

  @UiField
  JobActionsToolbar actionsToolbar;

  @UiField
  TitlePanel title;

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel informationPanel;

  @UiField
  FlowPanel pluginParametersPanel;

  @UiField
  Header header;

  @UiField
  FlowPanel lowerContent;

  // State Variables
  private IndexedJob job;
  private Map<String, PluginInfo> pluginsInfo;
  private Timer autoUpdateTimer = null;
  private int autoUpdateTimerPeriod = 0;

  private SelectedItems<?> cachedSourceObjects = null;
  private boolean sourceObjectsFetched = false;

  public ShowJob(IndexedJob job, Map<String, PluginInfo> pluginsInfo,
                 List<FilterParameter> extraReportFilterParameters) {
    this.job = job;
    this.pluginsInfo = pluginsInfo;
    boolean isIngest = job.getPluginType().equals(PluginType.INGEST);

    initWidget(uiBinder.createAndBindUi(this));

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    header.setHeaderText(messages.jobProcessed());
    header.setLevel(5);

    if (isJobInFinalState()) {
      navigationToolbar.withObject(job);
    } else {
      navigationToolbar.withoutButtons();
    }

    navigationToolbar.build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getJobBreadcrumbs(job));

    title.setIconClass("IndexedJob");
    title.setText(job.getName());
    title.addStyleName("mb-16");

    actionsToolbar.setObjectAndBuild(job, null, null);
    actionsToolbar.setLabel(messages.processTitle());

    // Initial UI Population
    updateUi(isIngest);

    addParameters();

    // Determine timer period
    if (isJobRecent()) {
      autoUpdateTimerPeriod = PERIOD_MILLIS_FAST;
    } else if (isJobRunning()) {
      autoUpdateTimerPeriod = PERIOD_MILLIS;
    }

    buildLowerContent(extraReportFilterParameters, isIngest);

    scheduleUpdateStatus(isIngest);
  }

  private void addParameters() {
    PluginInfo pluginInfo = pluginsInfo.get(job.getPlugin());
    if (pluginInfo != null && !pluginInfo.getParameters().isEmpty()) {
      pluginParametersPanel.add(
        new JobParametersPanel(job, pluginsInfo, pluginInfo.getParameters(), messages.pluginLabelWithVersion(pluginInfo.getName(), pluginInfo.getVersion())));
    }
  }

  private void updateUi(final boolean isIngest) {
    // Refresh the content panel
    informationPanel.clear();
    informationPanel.addStyleName("mb-16");
    informationPanel.add(new JobDetailsPanel(job, isIngest, isJobInFinalState(), cachedSourceObjects,
      sourceObjectsFetched, fetchedData -> {
        this.cachedSourceObjects = fetchedData;
        this.sourceObjectsFetched = true;
      }));

    actionsToolbar.setObjectAndBuild(job, null, null);
  }

  private void scheduleUpdateStatus(final boolean isIngest) {
    if (isJobRunning() || isJobRecent()) {
      if (autoUpdateTimer == null) {
        autoUpdateTimer = new Timer() {
          @Override
          public void run() {
            Services services = new Services("Update job status", "update");
            services.jobsResource(s -> s.findByUuid(job.getId(), LocaleInfo.getCurrentLocale().getLocaleName()))
              .whenComplete((updatedJob, error) -> {
                if (updatedJob != null) {
                  ShowJob.this.job = updatedJob;
                  updateUi(isIngest);
                  scheduleUpdateStatus(isIngest);
                } else if (error != null) {
                  AsyncCallbackUtils.defaultFailureTreatment(error);
                }
              });
          }
        };
      }
      autoUpdateTimer.schedule(autoUpdateTimerPeriod);
    }
  }

  private boolean isJobRecent() {
    return job != null && ((new Date()).getTime()) - job.getStartDate().getTime() < (30 * 1000);
  }

  private boolean isJobRunning() {
    boolean recentlyEnded = job.getEndDate() != null
      && ((new Date()).getTime()) - job.getEndDate().getTime() < (30 * 1000);
    return job != null && (!job.isInFinalState() || recentlyEnded);
  }

  private boolean isJobComplex() {
    String value = job.getPluginParameters().get(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS);
    int totalSteps = 1;
    if (value != null) {
      try {
        totalSteps = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        // return default value
      }
    }

    return totalSteps > 1;
  }

  private boolean isJobInFinalState() {
    return job != null && job.isInFinalState();
  }

  private void buildLowerContent(List<FilterParameter> extraReportFilterParameters, boolean isIngest) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getUUID()));
    filter.add(extraReportFilterParameters);

    AsyncTableCellOptions<IndexedReport> jobReportListBuilderOptions = new AsyncTableCellOptions<>(IndexedReport.class,
      "ShowJob_reports");
    jobReportListBuilderOptions.withRedirectOnSingleResult(!extraReportFilterParameters.isEmpty());
    jobReportListBuilderOptions.withFilter(filter);
    jobReportListBuilderOptions.withSummary(messages.reportList());
    jobReportListBuilderOptions.bindOpener();
    jobReportListBuilderOptions.withSearchPlaceholder(messages.jobProcessedSearchPlaceHolder());
    if (isJobRecent()) {
      autoUpdateTimerPeriod = PERIOD_MILLIS_FAST;
      jobReportListBuilderOptions.withAutoUpdate(autoUpdateTimerPeriod);
    } else if (isJobRunning()) {
      autoUpdateTimerPeriod = PERIOD_MILLIS;
      jobReportListBuilderOptions.withAutoUpdate(autoUpdateTimerPeriod);
    }

    ListBuilder<IndexedReport> jobReportListBuilder;
    if (isIngest) {
      jobReportListBuilder = new ListBuilder<>(() -> new IngestJobReportList(true, isJobRunning(), isJobComplex()),
        jobReportListBuilderOptions);
    } else {
      jobReportListBuilder = new ListBuilder<>(
        () -> new SimpleJobReportList(this.pluginsInfo, true, isJobRunning(), isJobComplex()),
        jobReportListBuilderOptions);
    }

    lowerContent.add(new SearchWrapper(false).createListAndSearchPanel(jobReportListBuilder));
  }

  @Override
  protected void onDetach() {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
    super.onDetach();
  }

  @Override
  protected void onLoad() {
    if (autoUpdateTimer != null && !autoUpdateTimer.isRunning() && autoUpdateTimerPeriod > 0) {
      autoUpdateTimer.scheduleRepeating(autoUpdateTimerPeriod);
    }
    super.onLoad();
  }

  interface MyUiBinder extends UiBinder<Widget, ShowJob> {
  }
}