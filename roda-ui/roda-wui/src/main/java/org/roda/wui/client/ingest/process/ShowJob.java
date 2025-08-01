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
package org.roda.wui.client.ingest.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoRequest;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.JobActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.IngestJobReportList;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchPreFilterUtils;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.management.distributed.ShowDistributedInstance;
import org.roda.wui.client.process.Process;
import org.roda.wui.client.services.ConfigurationRestService;
import org.roda.wui.client.services.DistributedInstancesRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Humanize.DHMSFormat;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria public SimpleJob
 */
public class ShowJob extends Composite {

  private static final int PERIOD_MILLIS = 10000;
  private static final int PERIOD_MILLIS_FAST = 2000;
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
      // TODO check for show job permission
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
  // empty to get all job information
  private static final List<String> fieldsToReturn = new ArrayList<>();
  private static final List<String> aipFieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_LEVEL,
    RodaConstants.AIP_TITLE);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  FlowPanel instancePanel;
  @UiField
  Label instanceLabel;
  @UiField
  Label name;
  @UiField
  Label creator;
  @UiField
  HTML jobPriority;
  @UiField
  HTML jobParallelism;
  @UiField
  Label dateStarted;
  @UiField
  Label dateEndedLabel, dateEnded;
  @UiField
  Label duration;
  @UiField
  HTML progress;
  @UiField
  HTML status;
  @UiField
  Label scheduleInfoLabel;
  @UiField
  Label scheduleInfo;
  @UiField
  Label stateDetailsLabel, stateDetailsValue;
  @UiField
  FlowPanel selectedListPanel;
  @UiField
  FlowPanel selectedList;
  @UiField
  FlowPanel attachmentsPanel;
  @UiField
  FlowPanel attachmentsList;
  @UiField
  Label plugin;
  @UiField
  FlowPanel pluginPanel;
  @UiField
  FlowPanel pluginOptions;
  @UiField
  FlowPanel reportListPanel;
  @UiField
  Label reportsLabel;
  @UiField(provided = true)
  SearchWrapper searchWrapper;
  @UiField
  SimplePanel actionsSidebar;
  @UiField
  FlowPanel sidebar;
  @UiField
  FlowPanel content;

  // SIDEBAR
  private IndexedJob job;
  private Map<String, PluginInfo> pluginsInfo;
  private Timer autoUpdateTimer = null;
  private int autoUpdateTimerPeriod = 0;

  public ShowJob(IndexedJob job, Map<String, PluginInfo> pluginsInfo,
    List<FilterParameter> extraReportFilterParameters) {
    this.job = job;
    this.pluginsInfo = pluginsInfo;
    boolean isIngest = job.getPluginType().equals(PluginType.INGEST);

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

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(jobReportListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    if (job.getInstanceId() != null) {
      String distributedMode = ConfigurationManager.getStringWithDefault(
        RodaConstants.DEFAULT_DISTRIBUTED_MODE_TYPE.name(), RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY);
      if (distributedMode.equals(RodaConstants.DistributedModeType.CENTRAL.name())) {
        Services services = new Services("Retrieve distributed instance", "retrieve");
        services.distributedInstanceResource(s -> s.getDistributedInstance(job.getInstanceId()))
          .whenComplete((distributedInstance, throwable) -> {
            if (throwable != null) {
              services.distributedInstanceResource(DistributedInstancesRestService::getLocalInstance)
                .whenComplete((localInstance, throwable1) -> {
                  Label instanceNameLabel = new Label(localInstance.getName());
                  instanceNameLabel.addStyleName("value");
                  instancePanel.add(instanceNameLabel);
                });
            } else {
              Anchor anchor = new Anchor();
              anchor.setHref(HistoryUtils.createHistoryHashLink(ShowDistributedInstance.RESOLVER, job.getInstanceId()));
              anchor.setText(distributedInstance.getName());
              anchor.addStyleName("btn-link");
              instancePanel.add(anchor);
            }
          });
      } else {
        instancePanel.setVisible(false);
      }
    } else {
      instancePanel.setVisible(false);
    }

    name.setText(job.getName());
    creator.setText(job.getUsername());

    jobPriority.setHTML(HtmlSnippetUtils.getJobPriorityHtml(job.getPriority(), true));
    jobParallelism.setHTML(HtmlSnippetUtils.getJobParallelismTypeHtml(job.getParallelism(), true));

    dateStarted.setText(Humanize.formatDateTime(job.getStartDate()));
    update();

    Services services = new Services("get job from model", "get");
    services.jobsResource(s -> s.getJobFromModel(job.getId())).whenComplete((modelJob, error) -> {
      if (modelJob != null) {
        SelectedItems<?> selected = modelJob.getSourceObjects();
        if (isIngest) {
          showIngestSourceObjects(selected);
        } else {
          showActionSourceObjects(selected);
        }
      } else if (error != null) {
        selectedListPanel.setVisible(false);
        AsyncCallbackUtils.defaultFailureTreatment(error);
      }
    });

    showAttachments(job.getAttachmentsList());

    PluginInfo pluginInfo = pluginsInfo.get(job.getPlugin());
    if (pluginInfo != null) {
      plugin.setText(messages.pluginLabelWithVersion(pluginInfo.getName(), pluginInfo.getVersion()));

      if (pluginInfo.getParameters().isEmpty()) {
        pluginPanel.setVisible(false);
        pluginOptions.setVisible(false);
      } else {
        pluginPanel.setVisible(true);
        pluginOptions.setVisible(true);
      }

      for (PluginParameter parameter : pluginInfo.getParameters()) {
        if (PluginParameterType.BOOLEAN.equals(parameter.getType())) {
          createBooleanLayout(parameter);
        } else if (PluginParameterType.STRING.equals(parameter.getType())) {
          createStringLayout(parameter);
        } else if (PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
          createPluginSipToAipLayout(parameter);
        } else if (PluginParameterType.AIP_ID.equals(parameter.getType())) {
          createSelectAipLayout(parameter);
        } else if (PluginParameterType.CONVERSION_PROFILE.equals(parameter.getType())) {
          createConvertProfileLayout(parameter);
        } else if (PluginParameterType.CONVERSION.equals(parameter.getType())) {
          createConversionLayout(parameter);
        } else {
          createStringLayout(parameter);
        }
      }
    } else {
      plugin.setText(job.getPlugin());
      pluginPanel.setVisible(false);
      pluginOptions.setVisible(false);
    }
  }

  private void refreshSidebar() {
    actionsSidebar.setWidget(new ActionableWidgetBuilder<>(JobActions.get(ShowJob.RESOLVER))
      .withActionCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
        @Override
        public void onSuccess(Actionable.ActionImpact result) {
          Services services = new Services("refresh sidebar", "refresh");
          services.jobsResource(s -> s.findByUuid(job.getId(), LocaleInfo.getCurrentLocale().getLocaleName()))
            .whenComplete((updatedJob, error) -> {
              if (updatedJob != null) {
                ShowJob.this.job = updatedJob;
                update();
              } else if (error != null) {
                AsyncCallbackUtils.defaultFailureTreatment(error);
              }
            });
        }
      }).withWidgetCreatedHandler(buttonCount -> {
        // hide sidebar if we don't have actions
        SidebarUtils.toggleSidebar(content, sidebar, buttonCount > 0);
      }).buildListWithObjects(new ActionableObject<>(job)));
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

    JavascriptUtils.stickSidebar();
    super.onLoad();
  }

  private boolean isJobRecent() {
    // currentTime - jobStartTime < 30 seconds
    return job != null && ((new Date()).getTime()) - job.getStartDate().getTime() < (30 * 1000);
  }

  private boolean isJobRunning() {
    boolean recentlyEnded = job.getEndDate() != null
      && ((new Date()).getTime()) - job.getEndDate().getTime() < (30 * 1000);
    return job != null && (!job.isInFinalState() || recentlyEnded);
  }

  private boolean isJobInFinalState() {
    return job != null && job.isInFinalState();
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

  private void showIngestSourceObjects(final SelectedItems<?> selected) {
    if (selected != null) {
      selectedList.clear();
      selectedListPanel.setVisible(true);

      if (ClientSelectedItemsUtils.isEmpty(selected) && isJobInFinalState()) {
        Label noSourceLabel = new Label(messages.noItemsToDisplay(messages.someOfAObject(selected.getSelectedClass())));
        selectedListPanel.add(noSourceLabel);
      } else if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();
        final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, job.getUUID()));
        InlineHTML filterHTML = new InlineHTML(
          messages.sourceObjectList(ids.size(), messages.someOfAObject(selected.getSelectedClass())));
        selectedList.add(filterHTML);

        Button button = new Button(messages.downloadButton());
        button.addStyleName("btn btn-separator-left btn-link");
        button.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Services services = new Services("Retrieve export limit", "retrieve");
            services.configurationsResource(ConfigurationRestService::retrieveExportLimit)
              .whenComplete((longResponse, throwable) -> {
                if (throwable != null) {
                  AsyncCallbackUtils.defaultFailureTreatment(throwable);
                } else {
                  Toast.showInfo(messages.exportListTitle(),
                    messages.exportListMessage(longResponse.getResult().intValue()));
                  RestUtils.requestCSVExport(IndexedJob.class, filter, Sorter.NONE,
                    new Sublist(0, longResponse.getResult().intValue()), Facets.NONE, true, false, "job.csv");
                }
              });
          }
        });

        selectedList.add(button);
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
        HTML filterHTML = new HTML(SearchPreFilterUtils.getFilterHTML(filter, selected.getSelectedClass()));
        selectedList.add(filterHTML);
      } else if (selected instanceof SelectedItemsAll) {
        Label filterLabel = new Label(messages.allOfAObject(selected.getSelectedClass()));
        filterLabel.addStyleName("value");
        selectedList.add(filterLabel);
      } else {
        selectedListPanel.setVisible(false);
      }
    }
  }

  private void showActionSourceObjects(final SelectedItems<?> selected) {
    if (selected != null) {
      selectedList.clear();

      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();

        if (ids.isEmpty()) {
          Label noSourceLabel = new Label(
            messages.noItemsToDisplay(messages.someOfAObject(selected.getSelectedClass())));
          selectedListPanel.add(noSourceLabel);
        } else {
          final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, job.getUUID()));
          InlineHTML filterHTML = new InlineHTML(
            messages.sourceObjectList(ids.size(), messages.someOfAObject(selected.getSelectedClass())));
          selectedList.add(filterHTML);

          Button button = new Button(messages.downloadButton());
          button.addStyleName("btn btn-separator-left btn-link");
          button.addClickHandler(event -> {
            Services services = new Services("Retrieve export limit", "retrieve");
            services.configurationsResource(ConfigurationRestService::retrieveExportLimit)
              .whenComplete((longResponse, throwable) -> {
                if (throwable != null) {
                  AsyncCallbackUtils.defaultFailureTreatment(throwable);
                } else {
                  Toast.showInfo(messages.exportListTitle(),
                    messages.exportListMessage(longResponse.getResult().intValue()));
                  RestUtils.requestCSVExport(IndexedJob.class, filter, Sorter.NONE,
                    new Sublist(0, longResponse.getResult().intValue()), Facets.NONE, true, false, "job.csv");
                }
              });
          });

          selectedList.add(button);
        }
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
        HTML filterHTML = new HTML(SearchPreFilterUtils.getFilterHTML(filter, selected.getSelectedClass()));
        selectedList.add(filterHTML);
      } else if (selected instanceof SelectedItemsAll) {

        Label objectLabel = new Label();
        objectLabel.addStyleName("value");

        if (!StringUtils.isBlank(selected.getSelectedClass())) {
          objectLabel.setText(messages.noItemsToDisplay(messages.someOfAObject(selected.getSelectedClass())));
        } else if (AIP.class.getName().equals(selected.getSelectedClass())
          || IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          objectLabel.setText(messages.allIntellectualEntities());
        } else if (Representation.class.getName().equals(selected.getSelectedClass())
          || IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          objectLabel.setText(messages.allRepresentations());
        } else if (File.class.getName().equals(selected.getSelectedClass())
          || IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          objectLabel.setText(messages.allFiles());
        } else {
          objectLabel.setText(messages.allOfAObject(selected.getSelectedClass()));
        }

        selectedList.add(objectLabel);
      } else {
        selectedListPanel.setVisible(false);
      }
    }
  }

  private void showAttachments(List<String> attachments) {
    if (attachments.isEmpty()) {
      attachmentsPanel.setVisible(false);
      attachmentsList.setVisible(false);
    } else {
      attachmentsPanel.setVisible(true);
      attachmentsList.setVisible(true);
      attachmentsList.clear();

      for (String attachment : attachments) {
        FlowPanel attachmentItem = new FlowPanel();
        InlineHTML attachmentId = new InlineHTML(attachment);

        Button button = new Button(messages.downloadButton());
        button.addStyleName("btn btn-separator-left btn-link");
        button.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            SafeUri downloadUri = RestUtils.createJobAttachmentDownloadUri(job.getId(), attachment);
            Window.Location.assign(downloadUri.asString());
          }
        });

        attachmentItem.add(attachmentId);
        attachmentItem.add(button);
        this.attachmentsList.add(attachmentItem);
      }
    }
  }

  private void update() {
    // set end date
    dateEndedLabel.setVisible(job.getEndDate() != null);
    dateEnded.setVisible(job.getEndDate() != null);
    if (job.getEndDate() != null) {
      dateEnded.setText(Humanize.formatDateTime(job.getEndDate()));
    }

    // set duration
    duration.setText(Humanize.durationInDHMS(job.getStartDate(), job.getEndDate(), DHMSFormat.LONG));

    // set state
    status.setHTML(HtmlSnippetUtils.getJobStateHtml(job.getState(), job.getJobStats()));

    scheduleInfoLabel.setVisible(false);
    scheduleInfo.setVisible(false);

    String distributedMode = ConfigurationManager.getStringWithDefault(
      RodaConstants.DEFAULT_DISTRIBUTED_MODE_TYPE.name(), RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY);

    if (distributedMode.equals(RodaConstants.DistributedModeType.LOCAL.name())
      && Job.JOB_STATE.SCHEDULED.equals(job.getState())) {
      Services services = new Services("Retrieve job schedule info", "retrieve");
      services.configurationsResource(s -> s.retrieveCronValue(LocaleInfo.getCurrentLocale().getLocaleName()))
        .whenComplete((stringResponse, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            String description = stringResponse.getValue();
            if (StringUtils.isNotBlank(description)) {
              scheduleInfoLabel.setVisible(true);
              scheduleInfo.setVisible(true);
              scheduleInfo.setText(description);
            }
          }
        });
    }

    // set state details
    boolean hasStateDetails = StringUtils.isNotBlank(job.getStateDetails());
    stateDetailsLabel.setVisible(hasStateDetails);
    stateDetailsValue.setVisible(hasStateDetails);
    if (hasStateDetails) {
      stateDetailsValue.setText(job.getStateDetails());
    }

    // set counters
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-default'>"));
    b.append(messages.showJobProgressCompletionPercentage(job.getJobStats().getCompletionPercentage()));
    b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    if (job.getJobStats().getSourceObjectsCount() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-default'>"));
      b.append(messages.showJobProgressTotalCount(job.getJobStats().getSourceObjectsCount()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsProcessedWithSuccess() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-success'>"));
      b.append(messages.showJobProgressSuccessfulCount(job.getJobStats().getSourceObjectsProcessedWithSuccess()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsProcessedWithPartialSuccess() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-warning'>"));
      b.append(messages
        .showJobProgressPartialSuccessfulCount(job.getJobStats().getSourceObjectsProcessedWithPartialSuccess()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsProcessedWithFailure() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-danger'>"));
      b.append(messages.showJobProgressFailedCount(job.getJobStats().getSourceObjectsProcessedWithFailure()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsProcessedWithSkipped() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-warning'>"));
      b.append(messages.showJobProgressSkippedCount(job.getJobStats().getSourceObjectsProcessedWithSkipped()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsBeingProcessed() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-info'>"));
      b.append(messages.showJobProgressProcessingCount(job.getJobStats().getSourceObjectsBeingProcessed()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsWaitingToBeProcessed() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-warning'>"));
      b.append(messages.showJobProgressWaitingCount(job.getJobStats().getSourceObjectsWaitingToBeProcessed()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }

    progress.setHTML(b.toSafeHtml());

    refreshSidebar();

    scheduleUpdateStatus();

    refreshJobPluginInfo();
  }

  private void refreshJobPluginInfo() {
    Services services = new Services("Refresh job plugin info", "update");
    services.jobsResource(s -> s.findByUuid(job.getId(), LocaleInfo.getCurrentLocale().getLocaleName()))
      .thenCompose(updateJob -> services.jobsResource(s -> s.getJobPluginInfo(new PluginInfoRequest(updateJob))))
      .whenComplete((pluginsInfoList, error) -> {
        if (pluginsInfoList != null) {
          pluginsInfo = new HashMap<>();
          for (PluginInfo pluginInfo : pluginsInfoList) {
            pluginsInfo.put(pluginInfo.getId(), pluginInfo);
          }
        } else if (error != null) {
          if (error instanceof NotFoundException) {
            Toast.showError(messages.notFoundError(), messages.jobNotFound());
            HistoryUtils.newHistory(Process.RESOLVER);
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(error);
          }
        }
      });
  }

  private void scheduleUpdateStatus() {
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
                  update();
                  scheduleUpdateStatus();
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

  private void createSelectAipLayout(PluginParameter parameter) {
    Label parameterName = new Label(parameter.getName());
    final FlowPanel aipPanel = new FlowPanel();
    final String value = job.getPluginParameters().containsKey(parameter.getId())
      ? job.getPluginParameters().get(parameter.getId())
      : parameter.getDefaultValue();

    if (value != null && !value.isEmpty()) {
      Services services = new Services("Retrieve AIP", "get");
      services.aipResource(s -> s.findByUuid(value, LocaleInfo.getCurrentLocale().getLocaleName()))
        .whenComplete((indexedAIP, throwable) -> {
          if (throwable != null) {
            if (throwable.getCause() instanceof NotFoundException) {
              Label itemTitle = new Label(value);
              itemTitle.addStyleName("itemText");
              aipPanel.clear();
              aipPanel.add(itemTitle);
            } else {
              AsyncCallbackUtils.defaultFailureTreatment(throwable.getCause());
            }
          } else {
            Label itemTitle = new Label();
            HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(indexedAIP.getLevel());
            itemIconHtmlPanel.addStyleName("itemIcon");
            itemTitle.setText(indexedAIP.getTitle() != null ? indexedAIP.getTitle() : indexedAIP.getId());
            itemTitle.addStyleName("itemText");

            aipPanel.clear();
            aipPanel.add(itemIconHtmlPanel);
            aipPanel.add(itemTitle);
          }
        });
    } else {
      HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
      aipPanel.clear();
      aipPanel.add(itemIconHtmlPanel);
    }

    pluginOptions.add(parameterName);
    pluginOptions.add(aipPanel);

    parameterName.addStyleName("form-label itemLabel");
    aipPanel.addStyleName("itemPanel itemPanelShow");
  }

  private void createBooleanLayout(PluginParameter parameter) {
    CheckBox checkBox = new CheckBox(parameter.getName());
    String value = job.getPluginParameters().get(parameter.getId());
    if (value == null) {
      value = parameter.getDefaultValue();
    }
    checkBox.setValue("true".equals(value));
    checkBox.setEnabled(false);

    pluginOptions.add(checkBox);
    addHelp(parameter.getDescription());

    checkBox.addStyleName("form-checkbox");
  }

  private void createStringLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
    if (value == null) {
      value = parameter.getDefaultValue();
    }
    if (value != null && value.length() > 0) {
      Label parameterLabel = new Label(parameter.getName());
      Label parameterValue = new Label(value);
      pluginOptions.add(parameterLabel);
      pluginOptions.add(parameterValue);

      parameterLabel.addStyleName("label");

      addHelp(parameter.getDescription());
    }
  }

  private void createPluginSipToAipLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());

    if (value == null) {
      value = parameter.getDefaultValue();
    }

    if (StringUtils.isNotBlank(value)) {
      Label pluginLabel = new Label(parameter.getName());
      PluginInfo sipToAipPlugin = pluginsInfo.get(value);
      RadioButton pluginValue;

      pluginOptions.add(pluginLabel);
      addHelp(parameter.getDescription());

      if (sipToAipPlugin != null) {
        pluginValue = new RadioButton(parameter.getId(),
          messages.pluginLabelWithVersion(sipToAipPlugin.getName(), sipToAipPlugin.getVersion()));

        pluginValue.setValue(true);
        pluginValue.setEnabled(false);
        pluginOptions.add(pluginValue);
        addHelp(sipToAipPlugin.getDescription());

      } else {
        pluginValue = new RadioButton(parameter.getId(), value);
        pluginValue.setValue(true);
        pluginValue.setEnabled(false);
        pluginOptions.add(pluginValue);
      }

      pluginLabel.addStyleName("label");
      pluginValue.addStyleName("form-radiobutton");
    }
  }

  private void createConvertProfileLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
    if (value == null) {
      value = parameter.getDefaultValue();
      Label parameterLabel = new Label(parameter.getName());
      Label parameterValue = new Label(value);
      pluginOptions.add(parameterLabel);
      pluginOptions.add(parameterValue);
      parameterLabel.addStyleName("label");

      addHelp(parameter.getDescription());
    } else {
      Label parameterLabel = new Label(parameter.getName());
      Label parameterValue = new Label(value);
      pluginOptions.add(parameterLabel);
      pluginOptions.add(parameterValue);
      parameterLabel.addStyleName("label");

      addHelp(parameter.getDescription());

      String profileOptions = job.getPluginParameters().get("parameter.option." + value);
      String content = profileOptions.substring(1, profileOptions.length() - 1);
      String[] profileOptionsArray = content.split(", ");

      for (String profileOption : profileOptionsArray) {
        String profileOptionName = profileOption.split("\\.")[1];
        Label profileParameterLabel = new Label(profileOptionName);
        Label profileParameterValue = new Label(job.getPluginParameters().get(profileOption));
        pluginOptions.add(profileParameterLabel);
        pluginOptions.add(profileParameterValue);
        profileParameterLabel.addStyleName("label");
      }
    }
  }

  private void createConversionLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
    Map<String, String> map = new HashMap<>();
    String[] keyValuePairs = value.split(";");
    for (String pair : keyValuePairs) {
      String[] parts = pair.split("=");
      if (parts.length == 2) {
        map.put(parts[0].trim(), parts[1].trim());
      }
    }

    String profile = job.getPluginParameters().get(RodaConstants.PLUGIN_PARAMS_CONVERSION_PROFILE);
    Label profileLabel = new Label("Conversion Profile");
    Label profileValueLabel = new Label(profile);
    profileLabel.addStyleName("label");
    pluginOptions.add(profileLabel);
    pluginOptions.add(profileValueLabel);

    String profileOptions = job.getPluginParameters().get("parameter.option." + profile);
    String content = profileOptions.substring(1, profileOptions.length() - 1);
    String[] profileOptionsArray = content.split(", ");

    for (String optionName : profileOptionsArray) {
      String optionNameCleaned = optionName.split("\\.")[1].replace("_", " ");
      String optionNameCapitalized = optionNameCleaned.substring(0, 1).toUpperCase() + optionNameCleaned.substring(1);
      Label optionLabel = new Label(optionNameCapitalized);
      optionLabel.addStyleName("label");
      String optionValue = job.getPluginParameters().get(optionName);
      if (optionValue.isEmpty()) {
        break;
      }
      Label optionValueLabel = new Label(optionValue);
      pluginOptions.add(optionLabel);
      pluginOptions.add(optionValueLabel);
    }

    if (map.get("type").equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_DISSEMINATION)) {
      String title = map.get("title");
      String description = map.get("description");

      Label titleLabel = new Label("Dissemination title");
      Label titleValue = new Label(title);
      pluginOptions.add(titleLabel);
      pluginOptions.add(titleValue);
      titleLabel.addStyleName("label");

      Label descriptionLabel = new Label("Dissemination description");
      Label descriptionTitle = new Label(description);
      pluginOptions.add(descriptionLabel);
      pluginOptions.add(descriptionTitle);
      descriptionLabel.addStyleName("label");
    } else {
      String representationType = map.get("value");
      Label parameterLabel = new Label("Representation type");
      Label parameterValue = new Label(representationType);

      pluginOptions.add(parameterLabel);
      pluginOptions.add(parameterValue);
      parameterLabel.addStyleName("label");
    }
  }

  private void addHelp(String description) {
    if (description != null && !description.isEmpty()) {
      Label pHelp = new Label(description);
      pluginOptions.add(pHelp);
      pHelp.addStyleName("form-help");
    }
  }

  interface MyUiBinder extends UiBinder<Widget, ShowJob> {
  }
}
