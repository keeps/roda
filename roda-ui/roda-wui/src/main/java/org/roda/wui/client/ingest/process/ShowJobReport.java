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

import org.roda.core.data.Attribute;
import org.roda.core.data.ReportItem;
import org.roda.core.data.v2.JobReport;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class ShowJobReport extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      GWT.log("show job report: " + historyTokens);
      if (historyTokens.size() == 1) {
        String jobReportId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieveJobReport(jobReportId, new AsyncCallback<JobReport>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(JobReport jobReport) {
            ShowJobReport showJob = new ShowJobReport(jobReport);
            callback.onSuccess(showJob);
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

  // private static final BrowseMessages messages =
  // GWT.create(BrowseMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private final JobReport jobReport;
  // private final Map<String, PluginInfo> pluginsInfo;

  @UiField
  Label id;
  @UiField
  Label job;
  @UiField
  Label aip;
  @UiField
  Label objectId;
  @UiField
  Label dateCreated;
  @UiField
  Label dateUpdated;
  @UiField
  FlowPanel reportAttributes;
  @UiField
  FlowPanel reportItems;

  @UiField
  Button buttonBack;

  public ShowJobReport(JobReport jobReport) {
    this.jobReport = jobReport;

    initWidget(uiBinder.createAndBindUi(this));

    id.setText(jobReport.getId());
    // TODO make the Job id a link
    job.setText(jobReport.getJobId());
    // TODO make the ObjectId a link
    objectId.setText(jobReport.getObjectId());
    // TODO check if AIP exists
    // TODO make the AIP id a link
    aip.setText(jobReport.getAipId());
    DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
    dateCreated.setText(dateTimeFormat.format(jobReport.getDateCreated()));
    dateUpdated.setText(dateTimeFormat.format(jobReport.getDateUpdated()));

    for (Attribute attribute : jobReport.getReport().getAttributes()) {
      if (attribute.getValue() != null && attribute.getValue().length() > 0) {
        Label attributeLabel = new Label(attribute.getName());
        attributeLabel.setStyleName("label");
        reportAttributes.add(attributeLabel);

        Label attributeValue = new Label(attribute.getValue());
        reportAttributes.add(attributeValue);
      }
    }

    for (ReportItem reportItem : jobReport.getReport().getItems()) {
      Label reportItemTitleLabel = new Label(reportItem.getTitle());
      reportItemTitleLabel.setStyleName("report-item-title");
      reportItems.add(reportItemTitleLabel);

      FlowPanel reportItemAttributes = new FlowPanel();
      reportItemAttributes.addStyleName("report-item-attributes");
      reportItems.add(reportItemAttributes);

      for (Attribute attribute : reportItem.getAttributes()) {
        if (attribute.getValue() != null && attribute.getValue().length() > 0) {
          Label attributeLabel = new Label(attribute.getName());
          attributeLabel.setStyleName("label");
          reportItemAttributes.add(attributeLabel);

          Label attributeValue = new Label(attribute.getValue());
          reportItemAttributes.add(attributeValue);

          if (attribute.getName().equals("outcomeDetails")) {
            attributeValue.addStyleName("code-pre");
          }
        }
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
