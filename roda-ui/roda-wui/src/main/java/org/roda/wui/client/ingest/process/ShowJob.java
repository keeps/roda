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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.IngestJobReportList;
import org.roda.wui.client.common.lists.SimpleJobReportList;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.process.ActionProcess;
import org.roda.wui.client.process.IngestProcess;
import org.roda.wui.client.process.Process;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class ShowJob extends Composite {

  private static final int PERIOD_MILLIS = 10000;

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String jobId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieveJobBundle(jobId, new AsyncCallback<JobBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            if (caught instanceof NotFoundException) {
              Toast.showError("Not found", "The job you requested was not found");
            } else {
              Toast.showError(caught.getClass().getName(), caught.getMessage());
            }

            Tools.newHistory(Process.RESOLVER);
          }

          @Override
          public void onSuccess(JobBundle jobBundle) {
            Map<String, PluginInfo> pluginsInfo = new HashMap<String, PluginInfo>();
            for (PluginInfo pluginInfo : jobBundle.getPluginsInfo()) {
              pluginsInfo.put(pluginInfo.getId(), pluginInfo);
            }

            ShowJob showJob = new ShowJob(jobBundle.getJob(), pluginsInfo);
            callback.onSuccess(showJob);
          }
        });
      } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(ShowJobReport.RESOLVER.getHistoryToken())) {
        ShowJobReport.RESOLVER.resolve(Tools.tail(historyTokens), callback);
      } else {
        Tools.newHistory(Process.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for show job permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Process.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Process.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "job";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private Job job;
  private final Map<String, PluginInfo> pluginsInfo;

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
  Label plugin;

  @UiField
  FlowPanel pluginPanel;

  @UiField
  FlowPanel pluginOptions;

  @UiField
  Label reportsLabel;

  @UiField(provided = true)
  IngestJobReportList jobReports;

  @UiField(provided = true)
  SimpleJobReportList simpleJobReports;

  @UiField
  Button buttonAppraisal, buttonBack;

  private final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT);

  public ShowJob(Job job, Map<String, PluginInfo> pluginsInfo) {
    this.job = job;
    this.pluginsInfo = pluginsInfo;
    boolean isIngest = false;

    // TODO get better name for job report list
    if (job.getPluginType().equals(PluginType.INGEST)) {
      jobReports = new IngestJobReportList(
        new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getId())), null, "Job report list",
        pluginsInfo, false);
      simpleJobReports = new SimpleJobReportList();
      isIngest = true;
    } else {
      simpleJobReports = new SimpleJobReportList(
        new Filter(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getId())), null, "Job report list",
        pluginsInfo, false);
      jobReports = new IngestJobReportList();
    }

    initWidget(uiBinder.createAndBindUi(this));
    simpleJobReports.setVisible(!isIngest);
    jobReports.setVisible(isIngest);

    name.setText(job.getName());
    creator.setText(job.getUsername());
    dateStarted.setText(dateTimeFormat.format(job.getStartDate()));
    update();

    if (isIngest) {
      if (isJobRunning()) {
        jobReports.autoUpdate(PERIOD_MILLIS);
      }

      jobReports.getSelectionModel().addSelectionChangeHandler(new Handler() {

        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
          Report jobReport = jobReports.getSelectionModel().getSelectedObject();
          GWT.log("new history: " + ShowJobReport.RESOLVER.getHistoryPath() + "/" + jobReport.getId());
          Tools.newHistory(ShowJobReport.RESOLVER, jobReport.getId());
        }
      });
    } else {
      if (isJobRunning()) {
        simpleJobReports.autoUpdate(PERIOD_MILLIS);
      }

      simpleJobReports.getSelectionModel().addSelectionChangeHandler(new Handler() {

        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
          Report jobReport = simpleJobReports.getSelectionModel().getSelectedObject();
          Tools.newHistory(ShowJobReport.RESOLVER, jobReport.getId());
        }
      });
    }

    PluginInfo pluginInfo = pluginsInfo.get(job.getPlugin());
    plugin.setText(messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()));

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
        // TODO log a warning
        createStringLayout(parameter);
      }
    }
  }

  private boolean isJobRunning() {
    return job != null && !JOB_STATE.COMPLETED.equals(job.getState())
      && !JOB_STATE.FAILED_DURING_CREATION.equals(job.getState())
      && !JOB_STATE.FAILED_TO_COMPLETE.equals(job.getState());
  }

  private void update() {
    // set end date
    dateEndedLabel.setVisible(job.getEndDate() != null);
    dateEnded.setVisible(job.getEndDate() != null);
    if (job.getEndDate() != null) {
      dateEnded.setText(dateTimeFormat.format(job.getEndDate()));
    }

    // set duration
    duration.setText(Humanize.durationInDHMS(job.getStartDate(), job.getEndDate()));

    // set state
    status.setHTML(HtmlSnippetUtils.getJobStateHtml(job));

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
    if (job.getJobStats().getSourceObjectsProcessedWithFailure() > 0) {
      b.append(SafeHtmlUtils.fromSafeConstant("&nbsp;<span class='label-danger'>"));
      b.append(messages.showJobProgressFailedCount(job.getJobStats().getSourceObjectsProcessedWithFailure()));
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

    buttonAppraisal.setText("Appraisal (" + job.getJobStats().getOutcomeObjectsWithManualIntervention() + ")");

    // set button visibility
    // buttonStop.setVisible(isJobRunning());
    buttonAppraisal.setVisible(job.getJobStats().getOutcomeObjectsWithManualIntervention() > 0);

    scheduleUpdateStatus();
  }

  private Timer autoUpdateTimer = null;

  private void scheduleUpdateStatus() {
    JOB_STATE state = job.getState();
    if (!JOB_STATE.COMPLETED.equals(state) && !JOB_STATE.FAILED_DURING_CREATION.equals(state)
      && !JOB_STATE.FAILED_TO_COMPLETE.equals(state)) {
      if (autoUpdateTimer == null) {
        autoUpdateTimer = new Timer() {

          @Override
          public void run() {
            BrowserService.Util.getInstance().retrieve(Job.class.getName(), job.getId(), new AsyncCallback<Job>() {

              @Override
              public void onFailure(Throwable caught) {
                Toast.showError(caught.getClass().getName(), caught.getMessage());
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
      autoUpdateTimer.schedule(PERIOD_MILLIS);
    }
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
    if (autoUpdateTimer != null && !autoUpdateTimer.isRunning() && isJobRunning()) {
      autoUpdateTimer.scheduleRepeating(PERIOD_MILLIS);
    }
    super.onLoad();
  }

  private void createSelectAipLayout(PluginParameter parameter) {
    Label parameterName = new Label(parameter.getName());
    final FlowPanel aipPanel = new FlowPanel();
    final String value = job.getPluginParameters().get(parameter.getId());

    if (value != null && !value.isEmpty()) {
      BrowserService.Util.getInstance().retrieve(IndexedAIP.class.getName(), value, new AsyncCallback<IndexedAIP>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof NotFoundException) {
            Label itemTitle = new Label(value);
            itemTitle.addStyleName("itemText");
            aipPanel.clear();
            aipPanel.add(itemTitle);
          } else {
            Toast.showError(caught.getClass().getName(), caught.getMessage());
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
    checkBox.setValue("true".equals(value));
    checkBox.setEnabled(false);

    pluginOptions.add(checkBox);
    addHelp(parameter.getDescription());

    checkBox.addStyleName("form-checkbox");
  }

  private void createStringLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
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
    if (value != null && value.length() > 0) {
      Label pluginLabel = new Label(parameter.getName());
      PluginInfo sipToAipPlugin = pluginsInfo.get(value);
      // Label pluginValue = new
      // Label(messages.pluginLabel(sipToAipPlugin.getName(),
      // sipToAipPlugin.getVersion()));

      RadioButton pluginValue = new RadioButton(parameter.getId(),
        messages.pluginLabel(sipToAipPlugin.getName(), sipToAipPlugin.getVersion()));
      pluginValue.setValue(true);
      pluginValue.setEnabled(false);

      pluginOptions.add(pluginLabel);
      addHelp(parameter.getDescription());
      pluginOptions.add(pluginValue);
      addHelp(sipToAipPlugin.getDescription());

      pluginLabel.addStyleName("label");
      pluginValue.addStyleName("form-radiobutton");

      // TODO show SIP_TO_AIP plugin description
    }
  }

  private void addHelp(String description) {
    if (description != null && description.length() > 0) {
      Label pHelp = new Label(description);

      pluginOptions.add(pHelp);

      pHelp.addStyleName("form-help");
    }
  }

  @UiHandler("buttonAppraisal")
  void buttonAppraisalHandler(ClickEvent e) {
    Tools.newHistory(IngestAppraisal.RESOLVER, RodaConstants.INGEST_JOB_ID, job.getId());
  }

  @UiHandler("buttonBack")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    if (job.getPluginType().equals(PluginType.INGEST)) {
      Tools.newHistory(IngestProcess.RESOLVER);
    } else {
      Tools.newHistory(ActionProcess.RESOLVER);
    }
  }

}
