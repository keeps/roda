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
package org.roda.wui.common.client.widgets;

import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.ReportService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class ReportWindow extends WUIWindow {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private String reportId;

  private ScrollPanel scroll;
  private ReportPanel reportPanel;
  private WUIButton printPDF;
  private WUIButton printCSV;
  private WUIButton close;

  /**
   * Create a new report window
   * 
   * @param reportId
   */
  public ReportWindow(String reportId) {
    super(reportId, 600, 600);
    this.reportId = reportId;
    scroll = new ScrollPanel();

    printPDF = new WUIButton(messages.reportWindowPrintPDF(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_DOWN);
    printCSV = new WUIButton(messages.reportWindowPrintCSV(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_DOWN);

    close = new WUIButton(messages.reportWindowClose(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

    String locale = LocaleInfo.getCurrentLocale().getLocaleName();

    printPDF.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        Window.open(GWT.getModuleBaseURL() + "ReportDownload?type=REPORT&output=PDF&locale=" + locale + "&id="
          + ReportWindow.this.reportId, "_blank", "");
      }

    });

    printCSV.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        Window.open(GWT.getModuleBaseURL() + "ReportDownload?type=REPORT&output=CSV&locale=" + locale + "&id="
          + ReportWindow.this.reportId, "_blank", "");
      }

    });

    close.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        hide();
      }

    });

    this.setWidget(scroll);
    addToBottom(printPDF);
    addToBottom(printCSV);
    addToBottom(close);

    init();

    scroll.addStyleName("wui-report-window");

  }

  private void init() {
    ReportService.Util.getInstance().getReport(reportId, new AsyncCallback<Report>() {

      public void onFailure(Throwable caught) {
        logger.error("Error initializing report window", caught);
      }

      public void onSuccess(Report report) {
        setTitle(report.getTitle());
        reportPanel = new ReportPanel(report);
        scroll.setWidget(reportPanel);
      }

    });

  }
}
