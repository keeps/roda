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

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ViewRepresentation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.process.IngestProcess;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
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
        BrowserService.Util.getInstance().retrieve(Report.class.getName(), jobReportId, new AsyncCallback<Report>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(Report jobReport) {
            ShowJobReport showJob = new ShowJobReport(jobReport);
            callback.onSuccess(showJob);
            JavascriptUtils.scrollToHeader();
          }
        });
      } else {
        Tools.newHistory(IngestProcess.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for show job permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {IngestProcess.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(ShowJob.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "report";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowJobReport> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private final Report jobReport;
  // private final Map<String, PluginInfo> pluginsInfo;

  @UiField
  Anchor job;
  @UiField
  Label aipLabel;
  @UiField
  Anchor aip;
  @UiField
  HTML aipState;

  @UiField
  Label objectLabel;
  @UiField
  Anchor objectId;
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
  Button buttonBack;

  public ShowJobReport(Report jobReport) {
    this.jobReport = jobReport;

    initWidget(uiBinder.createAndBindUi(this));

    job.setText(jobReport.getJobId());
    job.setHref(Tools.createHistoryHashLink(ShowJob.RESOLVER, jobReport.getJobId()));

    boolean hasSource = true;
    if (!jobReport.getSourceObjectOriginalId().isEmpty() || !jobReport.getSourceObjectId().isEmpty()) {
      objectId.setText(!"".equals(jobReport.getSourceObjectOriginalId()) ? jobReport.getSourceObjectOriginalId()
        : jobReport.getSourceObjectId());

      if (TransferredResource.class.getCanonicalName().equals(jobReport.getSourceObjectClass())) {
        objectId.setHref(Tools.createHistoryHashLink(IngestTransfer.RESOLVER, jobReport.getSourceObjectId()));
        objectLabel.setText(messages.showSIPExtended());

      } else if (AIP.class.getCanonicalName().equals(jobReport.getSourceObjectClass())) {
        objectId.setHref(Tools.createHistoryHashLink(Browse.RESOLVER, objectId.getText()));
        objectLabel.setText(messages.showAIPExtended());

      } else if (Representation.class.getCanonicalName().equals(jobReport.getSourceObjectClass())) {
        BrowserService.Util.getInstance().retrieve(IndexedRepresentation.class.getCanonicalName(), objectId.getText(),
          new AsyncCallback<IndexedRepresentation>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(IndexedRepresentation result) {
              if (result != null) {
                objectLabel.setText(messages.showRepresentationExtended());
                objectId.setHref(Tools.createHistoryHashLink(Browse.RESOLVER,
                  ViewRepresentation.RESOLVER.getHistoryToken(), result.getAipId(), result.getUUID()));
              }
            }
          });

      } else if (File.class.getCanonicalName().equals(jobReport.getSourceObjectClass())) {
        BrowserService.Util.getInstance().retrieve(IndexedFile.class.getCanonicalName(), objectId.getText(),
          new AsyncCallback<IndexedFile>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(IndexedFile result) {
              if (result != null) {
                objectLabel.setText(messages.showFileExtended());
                objectId
                  .setHref(Tools.createHistoryHashLink(Browse.RESOLVER, ViewRepresentation.RESOLVER.getHistoryToken(),
                    result.getAipId(), result.getRepresentationUUID(), result.getUUID()));
              }
            }
          });
      }
    } else {
      hasSource = false;
    }

    objectLabel.setVisible(hasSource);
    objectId.setVisible(hasSource);

    if (jobReport.getOutcomeObjectId() != null) {
      aip.setText(jobReport.getOutcomeObjectId());
      aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(jobReport.getOutcomeObjectState()));

      if (AIP.class.getCanonicalName().equals(jobReport.getOutcomeObjectClass())) {
        aip.setHref(Tools.createHistoryHashLink(Browse.RESOLVER, jobReport.getOutcomeObjectId()));
        aipLabel.setText(messages.showAIPExtended());

      } else if (Representation.class.getCanonicalName().equals(jobReport.getOutcomeObjectClass())) {
        BrowserService.Util.getInstance().retrieve(IndexedRepresentation.class.getCanonicalName(),
          jobReport.getOutcomeObjectId(), new AsyncCallback<IndexedRepresentation>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(IndexedRepresentation result) {
              if (result != null) {
                aipLabel.setText(messages.showRepresentationExtended());
                aip.setHref(Tools.createHistoryHashLink(Browse.RESOLVER, ViewRepresentation.RESOLVER.getHistoryToken(),
                  result.getAipId(), result.getUUID()));
              }
            }
          });

      } else if (File.class.getCanonicalName().equals(jobReport.getOutcomeObjectClass())) {
        BrowserService.Util.getInstance().retrieve(IndexedFile.class.getCanonicalName(), jobReport.getOutcomeObjectId(),
          new AsyncCallback<IndexedFile>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(IndexedFile result) {
              if (result != null) {
                aipLabel.setText(messages.showFileExtended());
                aip.setHref(Tools.createHistoryHashLink(Browse.RESOLVER, ViewRepresentation.RESOLVER.getHistoryToken(),
                  result.getAipId(), result.getRepresentationUUID(), result.getUUID()));
              }
            }
          });
      } else {
        aip.setVisible(false);
        aipState.setVisible(false);
        aipLabel.setVisible(false);
      }
    } else {
      // TODO show better message
      aip.setText("No AIP created");
      aipState.setText("");
    }

    DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT);
    dateCreated.setText(dateTimeFormat.format(jobReport.getDateCreated()));
    dateUpdated.setText(dateTimeFormat.format(jobReport.getDateUpdated()));
    duration.setText(Humanize.durationInDHMS(jobReport.getDateCreated(), jobReport.getDateUpdated()));
    status.setHTML(HtmlSnippetUtils.getPluginStateHTML(jobReport.getPluginState()));
    progress.setText(messages.showJobReportProgress(jobReport.getCompletionPercentage(), jobReport.getStepsCompleted(),
      jobReport.getTotalSteps()));

    for (Report reportItem : jobReport.getReports()) {
      FlowPanel panel = new FlowPanel();
      panel.setStyleName("panel");
      panel.addStyleName("panel-counter");
      reportItems.add(panel);

      FlowPanel panelHeading = new FlowPanel();
      panelHeading.setStyleName("panel-heading");
      Label panelTitle = new Label(reportItem.getTitle());
      panelTitle.setStyleName("panel-title");
      panelHeading.add(panelTitle);
      panel.add(panelHeading);

      FlowPanel panelBody = new FlowPanel();
      panelBody.addStyleName("panel-body");
      panel.add(panelBody);

      // FIXME
      Label attributeLabel = new Label(messages.reportAgent());
      attributeLabel.setStyleName("label");
      panelBody.add(attributeLabel);
      Label attributeValue = new Label(messages.pluginLabel(reportItem.getPlugin(), reportItem.getPluginVersion()));
      attributeValue.setStyleName("value");
      panelBody.add(attributeValue);

      attributeLabel = new Label(messages.reportStartDatetime());
      attributeLabel.setStyleName("label");
      panelBody.add(attributeLabel);
      attributeValue = new Label(dateTimeFormat.format(reportItem.getDateCreated()));
      attributeValue.setStyleName("value");
      panelBody.add(attributeValue);

      attributeLabel = new Label(messages.reportEndDatetime());
      attributeLabel.setStyleName("label");
      panelBody.add(attributeLabel);
      attributeValue = new Label(dateTimeFormat.format(reportItem.getDateUpdated()));
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
          attributeValue = new HTML(SafeHtmlUtils.fromTrustedString(reportItem.getPluginDetails()));
        } else {
          attributeValue = new Label(reportItem.getPluginDetails());
        }
        attributeValue.addStyleName("code-pre");
        panelBody.add(attributeValue);
      }
    }
  }

  @UiHandler("buttonBack")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(ShowJob.RESOLVER, jobReport.getJobId());
  }

}
