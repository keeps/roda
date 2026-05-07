package org.roda.wui.client.ingest.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.client.common.JobReportActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.planning.tabs.RepresentationInformationDetailsPanel;
import org.roda.wui.client.process.IngestProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ShowJobReportV2 extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 3) {
        String jobReportId = historyTokens.get(2);
        Services services = new Services("Get job report items", "get");

        services.jobReportResource(s -> s.findByUuid(jobReportId, LocaleInfo.getCurrentLocale().getLocaleName()))
          .thenCompose(indexedReport -> services
            .jobsResource(s -> s.getJobReport(indexedReport.getJobId(), jobReportId)).whenComplete((reports, error) -> {
              if (reports != null) {
                indexedReport.setReports(reports.getReports());
                ShowJobReportV2 showJob = new ShowJobReportV2(indexedReport);
                callback.onSuccess(showJob);
              } else if (error != null) {
                callback.onFailure(error);
              }
            }));
      } else {
        HistoryUtils.newHistory(IngestProcess.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {IngestProcess.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(ShowJob.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "report-new";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  NavigationToolbar<IndexedReport> navigationToolbar;

  @UiField
  JobReportActionsToolbar actionsToolbar;

  @UiField
  TitlePanel title;

  @UiField
  FlowPanel content;

  public ShowJobReportV2(IndexedReport report) {
    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withObject(report).build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getJobReportBreadcrumbs(report));

    title.setIconClass("IndexedReport");
    title.setText(messages.reportTitle());
    title.addStyleName("mb-20");

    actionsToolbar.setObjectAndBuild(report, null, null);
    actionsToolbar.setLabel("Report");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    content.add(new JobReportDetailsPanel(report));
  }

  interface MyUiBinder extends UiBinder<Widget, ShowJobReportV2> {
  }
}
