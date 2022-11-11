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
import java.util.List;

import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.process.IngestProcess;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Humanize.DHMSFormat;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class ShowJobReport extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String jobReportId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieve(IndexedReport.class.getName(), jobReportId, fieldsToReturn,
          new AsyncCallback<IndexedReport>() {

            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            @Override
            public void onSuccess(IndexedReport jobReport) {
              ShowJobReport showJob = new ShowJobReport(jobReport);
              callback.onSuccess(showJob);
            }
          });
      } else {
        HistoryUtils.newHistory(IngestProcess.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for show job permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {IngestProcess.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(ShowJob.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "report";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowJobReport> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // empty to get all report information
  private static final List<String> fieldsToReturn = new ArrayList<>();

  private final IndexedReport jobReport;

  @UiField
  Anchor job;
  @UiField
  Label outcomeObjectLabel;
  @UiField
  Anchor outcomeObject;
  @UiField
  HTML outcomeObjectState;

  @UiField
  Label sourceObjectLabel;
  @UiField
  Anchor sourceObject;
  @UiField
  Label dateCreated;
  @UiField
  Label dateUpdated;
  @UiField
  Label duration;
  @UiField
  HTML status;
  // FIXME 20160606 hsilva: added jobStateDetails
  @UiField
  Label progress;
  @UiField
  FlowPanel reportAttributes;
  @UiField
  FlowPanel reportItems;

  @UiField
  Button searchPrevious, searchNext, buttonBack;
  @UiField
  FocusPanel keyboardFocus;

  public ShowJobReport(IndexedReport jobReport) {
    this.jobReport = jobReport;

    initWidget(uiBinder.createAndBindUi(this));

    job.setText(jobReport.getJobName());
    job.setHref(HistoryUtils.createHistoryHashLink(ShowJob.RESOLVER, jobReport.getJobId()));
    outcomeObjectState.setVisible(false);

    boolean hasSource = !jobReport.getSourceObjectOriginalIds().isEmpty() || !jobReport.getSourceObjectId().isEmpty();

    if (hasSource) {
      String idText = !jobReport.getSourceObjectOriginalIds().isEmpty()
        ? " (" + StringUtils.prettyPrint(jobReport.getSourceObjectOriginalIds()) + ")"
        : "";

      if (StringUtils.isNotBlank(jobReport.getSourceObjectOriginalName())) {
        sourceObject.setText(jobReport.getSourceObjectOriginalName() + idText);
      } else if (StringUtils.isNotBlank(jobReport.getSourceObjectLabel())) {
        sourceObject.setText(jobReport.getSourceObjectLabel() + idText);
      } else if (StringUtils.isNotBlank(jobReport.getSourceObjectId())) {
        sourceObject.setText(jobReport.getSourceObjectId() + idText);
      } else {
        hasSource = false;
      }

      // sourceObject.setTitle(jobReport.getSourceObjectOriginalName());
      sourceObject.setHref(HistoryUtils.createHistoryHashLink(
        HistoryUtils.getHistoryUuidResolver(jobReport.getSourceObjectClass(), jobReport.getSourceObjectId())));
      sourceObjectLabel.setText(messages.jobReportSource(jobReport.getSourceObjectClass()));
    }

    sourceObjectLabel.setVisible(hasSource);
    sourceObject.setVisible(hasSource);

    boolean hasOutcome = StringUtils.isNotBlank(jobReport.getOutcomeObjectId())
      && !jobReport.getOutcomeObjectId().equals(jobReport.getSourceObjectId());

    if (hasOutcome) {
      if (jobReport.getOutcomeObjectLabel() != null) {
        outcomeObject.setText(jobReport.getOutcomeObjectLabel());
      } else if (jobReport.getOutcomeObjectId() != null) {
        outcomeObject.setText(jobReport.getOutcomeObjectId());
      } else {
        hasOutcome = false;
      }

      // outcomeObject.setTitle(jobReport.getSourceObjectOriginalName());
      outcomeObject.setHref(HistoryUtils.createHistoryHashLink(
        HistoryUtils.getHistoryUuidResolver(jobReport.getOutcomeObjectClass(), jobReport.getOutcomeObjectId())));
      outcomeObjectLabel.setText(messages.jobReportOutcome(jobReport.getOutcomeObjectClass()));
      outcomeObjectState.setHTML(HtmlSnippetUtils.getAIPStateHTML(jobReport.getOutcomeObjectState()));
    }

    outcomeObject.setVisible(hasOutcome);
    outcomeObjectState.setVisible(hasOutcome);
    outcomeObjectLabel.setVisible(hasOutcome);

    dateCreated.setText(Humanize.formatDateTime(jobReport.getDateCreated()));
    dateUpdated.setText(Humanize.formatDateTime(jobReport.getDateUpdated()));
    duration.setText(Humanize.durationInDHMS(jobReport.getDateCreated(), jobReport.getDateUpdated(), DHMSFormat.LONG));
    status.setHTML(HtmlSnippetUtils.getPluginStateHTML(jobReport.getPluginState()));
    progress.setText(messages.showJobReportProgress(jobReport.getCompletionPercentage(), jobReport.getStepsCompleted(),
      jobReport.getTotalSteps()));

    ListSelectionUtils.bindLayout(jobReport, searchPrevious, searchNext, keyboardFocus, true, false, false);

    for (Report reportItem : jobReport.getReports()) {
      FlowPanel panel = new FlowPanel();
      panel.setStyleName("panel");
      panel.addStyleName("panel-counter");
      reportItems.add(panel);

      FlowPanel panelHeading = new FlowPanel();
      panelHeading.setStyleName("panel-heading");
      Label panelTitle = new Label(reportItem.getTitle());

      HTML pluginMandatoryHTML = new HTML(HtmlSnippetUtils.getPluginMandatoryHTML(reportItem.getPluginIsMandatory()));
      pluginMandatoryHTML.addStyleName("small");

      panelTitle.setStyleName("panel-title");
      panelHeading.add(panelTitle);
      panelHeading.add(pluginMandatoryHTML);
      panel.add(panelHeading);

      FlowPanel panelBody = new FlowPanel();
      panelBody.addStyleName("panel-body");
      panel.add(panelBody);

      Label attributeLabel = new Label(messages.reportAgent());
      attributeLabel.setStyleName("label");
      panelBody.add(attributeLabel);

      String text;
      if (StringUtils.isNotBlank(reportItem.getPluginVersion())) {
        text = messages.pluginLabelWithVersion(reportItem.getPlugin(), reportItem.getPluginVersion());
      } else {
        text = messages.pluginLabel(reportItem.getPlugin());
      }

      Label attributeValue = new Label(text);
      attributeValue.setStyleName("value");
      panelBody.add(attributeValue);

      attributeLabel = new Label(messages.reportStartDatetime());
      attributeLabel.setStyleName("label");
      panelBody.add(attributeLabel);
      attributeValue = new Label(Humanize.formatDateTime(reportItem.getDateCreated()));
      attributeValue.setStyleName("value");
      panelBody.add(attributeValue);

      attributeLabel = new Label(messages.reportEndDatetime());
      attributeLabel.setStyleName("label");
      panelBody.add(attributeLabel);
      attributeValue = new Label(Humanize.formatDateTime(reportItem.getDateUpdated()));
      attributeValue.setStyleName("value");
      panelBody.add(attributeValue);

      attributeLabel = new Label(messages.reportOutcome());
      attributeLabel.setStyleName("label");
      panelBody.add(attributeLabel);
      HTML outcomeHTML = new HTML(HtmlSnippetUtils.getPluginStateHTML(reportItem.getPluginState()));
      panelBody.add(outcomeHTML);

      if (reportItem.getPluginDetails() != null && !"".equals(reportItem.getPluginDetails())) {
        attributeLabel = new Label(messages.reportOutcomeDetails());
        attributeLabel.setStyleName("label");
        panelBody.add(attributeLabel);
        if (reportItem.isHtmlPluginDetails()) {
          attributeValue = new HTML(SafeHtmlUtils.fromString(reportItem.getPluginDetails()));
        } else {
          attributeValue = new Label(reportItem.getPluginDetails());
        }
        attributeValue.addStyleName("code-pre");
        panelBody.add(attributeValue);
      }
    }

    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.removeFromParent();
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonBack")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowJob.RESOLVER, jobReport.getJobId());
  }

}
