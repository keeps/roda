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

import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
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

  private final Report jobReport;
  // private final Map<String, PluginInfo> pluginsInfo;

  @UiField
  Anchor job;
  @UiField
  Anchor aip;
  @UiField
  Anchor objectId;
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

  public ShowJobReport(Report jobReport) {
    this.jobReport = jobReport;

    initWidget(uiBinder.createAndBindUi(this));

    job.setText(jobReport.getJobId());
    job.setHref(Tools.createHistoryHashLink(ShowJob.RESOLVER, jobReport.getJobId()));
    objectId.setText(jobReport.getOtherId());
    objectId.setHref(RestUtils.createTransferredResourceDownloadUri(jobReport.getOtherId()));

    if (jobReport.getItemId() != null) {
      aip.setText(jobReport.getItemId());
      aip.setHref(Tools.createHistoryHashLink(Browse.RESOLVER, jobReport.getItemId()));
    } else {
      // TODO show better message
      aip.setText("No AIP created");
    }
    DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
    dateCreated.setText(dateTimeFormat.format(jobReport.getDateCreated()));
    dateUpdated.setText(dateTimeFormat.format(jobReport.getDateUpdated()));

    for (Report reportItem : jobReport.getReports()) {
      Label reportItemTitleLabel = new Label(reportItem.getTitle());
      reportItemTitleLabel.setStyleName("report-item-title");
      reportItems.add(reportItemTitleLabel);

      FlowPanel reportItemAttributes = new FlowPanel();
      reportItemAttributes.addStyleName("report-item-attributes");
      reportItems.add(reportItemAttributes);

      // FIXME
      Label attributeLabel = new Label("Plugin");
      attributeLabel.setStyleName("label");
      reportItemAttributes.add(attributeLabel);
      Label attributeValue = new Label(reportItem.getPlugin());
      reportItemAttributes.add(attributeValue);

      attributeLabel = new Label("Start datetime");
      attributeLabel.setStyleName("label");
      reportItemAttributes.add(attributeLabel);
      attributeValue = new Label(dateTimeFormat.format(reportItem.getDateCreated()));
      reportItemAttributes.add(attributeValue);

      attributeLabel = new Label("End datetime");
      attributeLabel.setStyleName("label");
      reportItemAttributes.add(attributeLabel);
      attributeValue = new Label(dateTimeFormat.format(reportItem.getDateUpdated()));
      reportItemAttributes.add(attributeValue);

      attributeLabel = new Label("Outcome");
      attributeLabel.setStyleName("label");
      reportItemAttributes.add(attributeLabel);
      attributeValue = new Label(reportItem.getPluginState().toString());
      reportItemAttributes.add(attributeValue);

      if (reportItem.getPluginDetails() != null && !"".equals(reportItem.getPluginDetails())) {
        attributeLabel = new Label("Outcome details");
        attributeLabel.setStyleName("label");
        reportItemAttributes.add(attributeLabel);
        attributeValue = new Label(reportItem.getPluginDetails());
        attributeValue.addStyleName("code-pre");
        reportItemAttributes.add(attributeValue);
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
