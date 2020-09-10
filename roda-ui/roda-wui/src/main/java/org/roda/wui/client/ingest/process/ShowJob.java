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
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
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
import org.roda.wui.client.process.Process;
import org.roda.wui.common.client.HistoryResolver;
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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 1 && historyTokens.get(0).equals(ShowJobReport.RESOLVER.getHistoryToken())) {
        ShowJobReport.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() >= 1) {
        String jobId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieveJobBundle(jobId, new ArrayList<>(), new AsyncCallback<JobBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            if (caught instanceof NotFoundException) {
              Toast.showError(messages.notFoundError(), messages.jobNotFound());
              HistoryUtils.newHistory(Process.RESOLVER);
            } else {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }
          }

          @Override
          public void onSuccess(JobBundle jobBundle) {
            Map<String, PluginInfo> pluginsInfo = new HashMap<>();
            for (PluginInfo pluginInfo : jobBundle.getPluginsInfo()) {
              pluginsInfo.put(pluginInfo.getId(), pluginInfo);
            }

            List<FilterParameter> reportFilterParameters = new ArrayList<>();
            for (int i = 1; i < historyTokens.size() - 1; i += 2) {
              String key = historyTokens.get(i);
              String value = historyTokens.get(i + 1);
              reportFilterParameters.add(new SimpleFilterParameter(key, value));
            }

            ShowJob showJob = new ShowJob(jobBundle.getJob(), pluginsInfo, reportFilterParameters);
            callback.onSuccess(showJob);
          }

        });
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

  interface MyUiBinder extends UiBinder<Widget, ShowJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // empty to get all job information
  private static final List<String> fieldsToReturn = new ArrayList<>();

  private static final List<String> aipFieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_LEVEL,
    RodaConstants.AIP_TITLE);

  private Job job;
  private Map<String, PluginInfo> pluginsInfo;

  @UiField
  Label name;

  @UiField
  Label creator;

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
  Label stateDetailsLabel, stateDetailsValue;

  @UiField
  FlowPanel selectedListPanel;

  @UiField
  FlowPanel selectedList;

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

  // SIDEBAR

  @UiField
  SimplePanel actionsSidebar;

  @UiField
  FlowPanel sidebar;

  @UiField
  FlowPanel content;

  public ShowJob(Job job, Map<String, PluginInfo> pluginsInfo, List<FilterParameter> extraReportFilterParameters) {
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
      jobReportListBuilder = new ListBuilder<>(() -> new SimpleJobReportList(this.pluginsInfo, true, isJobRunning(), isJobComplex()),
        jobReportListBuilderOptions);
    }

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(jobReportListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    name.setText(job.getName());
    creator.setText(job.getUsername());
    dateStarted.setText(Humanize.formatDateTime(job.getStartDate()));
    update();

    SelectedItems<?> selected = job.getSourceObjects();
    selectedListPanel.setVisible(true);

    if (isIngest) {
      showIngestSourceObjects(selected);
    } else {
      showActionSourceObjects(selected);
    }

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
          BrowserService.Util.getInstance().retrieve(Job.class.getName(), job.getId(), fieldsToReturn,
            new NoAsyncCallback<Job>() {
              @Override
              public void onSuccess(Job updatedJob) {
                ShowJob.this.job = updatedJob;
                update();
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
            BrowserService.Util.getInstance().getExportLimit(new AsyncCallback<Integer>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(Integer result) {
                Toast.showInfo(messages.exportListTitle(), messages.exportListMessage(result));
                RestUtils.requestCSVExport(Job.class.getName(), filter, Sorter.NONE, new Sublist(0, result),
                  Facets.NONE, true, false, "job.csv");
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
          button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              BrowserService.Util.getInstance().getExportLimit(new AsyncCallback<Integer>() {

                @Override
                public void onFailure(Throwable caught) {
                  AsyncCallbackUtils.defaultFailureTreatment(caught);
                }

                @Override
                public void onSuccess(Integer result) {
                  Toast.showInfo(messages.exportListTitle(), messages.exportListMessage(result));
                  RestUtils.requestCSVExport(Job.class.getName(), filter, Sorter.NONE, new Sublist(0, result),
                    Facets.NONE, true, false, "job.csv");
                }
              });
            }
          });

          selectedList.add(button);
        }
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) selected).getFilter();
        HTML filterHTML = new HTML(SearchPreFilterUtils.getFilterHTML(filter, selected.getSelectedClass()));
        selectedList.add(filterHTML);
      } else if (selected instanceof SelectedItemsAll || selected instanceof SelectedItemsNone) {
        Label objectLabel = new Label();
        objectLabel.addStyleName("value");

        if (StringUtils.isBlank(selected.getSelectedClass())) {
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
    status.setHTML(HtmlSnippetUtils.getJobStateHtml(job));

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
      b.append(messages.showJobProgressPartialSuccessfulCount(job.getJobStats().getSourceObjectsProcessedWithPartialSuccess()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsProcessedWithFailure() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-danger'>"));
      b.append(messages.showJobProgressFailedCount(job.getJobStats().getSourceObjectsProcessedWithFailure()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }
    if (job.getJobStats().getSourceObjectsProcessedWithSkipped() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-default'>"));
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
    BrowserService.Util.getInstance().retrieveJobBundle(job.getId(), new ArrayList<>(), new AsyncCallback<JobBundle>() {
      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof NotFoundException) {
          Toast.showError(messages.notFoundError(), messages.jobNotFound());
          HistoryUtils.newHistory(Process.RESOLVER);
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }
      }

      @Override
      public void onSuccess(JobBundle jobBundle) {
        pluginsInfo = new HashMap<>();
        for (PluginInfo pluginInfo : jobBundle.getPluginsInfo()) {
          pluginsInfo.put(pluginInfo.getId(), pluginInfo);
        }
      }
    });
  }

  private Timer autoUpdateTimer = null;
  private int autoUpdateTimerPeriod = 0;

  private void scheduleUpdateStatus() {
    if (isJobRunning() || isJobRecent()) {
      if (autoUpdateTimer == null) {
        autoUpdateTimer = new Timer() {

          @Override
          public void run() {
            BrowserService.Util.getInstance().retrieve(Job.class.getName(), job.getId(), fieldsToReturn,
              new AsyncCallback<Job>() {

                @Override
                public void onFailure(Throwable caught) {
                  AsyncCallbackUtils.defaultFailureTreatment(caught);
                }

                @Override
                public void onSuccess(Job updatedJob) {
                  ShowJob.this.job = updatedJob;
                  update();
                  scheduleUpdateStatus();
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
      BrowserService.Util.getInstance().retrieve(IndexedAIP.class.getName(), value, aipFieldsToReturn,
        new AsyncCallback<IndexedAIP>() {

          @Override
          public void onFailure(Throwable caught) {
            if (caught instanceof NotFoundException) {
              Label itemTitle = new Label(value);
              itemTitle.addStyleName("itemText");
              aipPanel.clear();
              aipPanel.add(itemTitle);
            } else {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }
          }

          @Override
          public void onSuccess(IndexedAIP aip) {
            Label itemTitle = new Label();
            HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(aip.getLevel());
            itemIconHtmlPanel.addStyleName("itemIcon");
            itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
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

  private void addHelp(String description) {
    if (description != null && description.length() > 0) {
      Label pHelp = new Label(description);
      pluginOptions.add(pHelp);
      pHelp.addStyleName("form-help");
    }
  }
}
