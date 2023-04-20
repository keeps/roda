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
package org.roda.wui.client.process;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.common.UpSalePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.search.JobSearch;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class IngestProcess extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "process";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static IngestProcess instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static IngestProcess getInstance() {
    if (instance == null) {
      instance = new IngestProcess();
    }

    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, IngestProcess> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final String cardIdentifier="collapsable-ingest-process-card";

  @UiField
  FlowPanel ingestProcessDescription;

  @UiField(provided = true)
  JobSearch jobSearch;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField(provided = true)
  UpSalePanel ingestPanel;


  private IngestProcess() {
    Filter jobIngestFilter = new Filter(
      new SimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INGEST.name()));
    Filter jobReportIngestFilter = new Filter(
      new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_PLUGIN_TYPE, PluginType.INGEST.name()));

    jobSearch = new JobSearch("IngestProcess_jobs", "IngestProcess_reports", jobIngestFilter, jobReportIngestFilter,
      true, IngestTransfer.RESOLVER);
    ingestPanel = new UpSalePanel(messages.dropFolderInformationText(), messages.learnMore() ,ConfigurationManager.getString(RodaConstants.UI_DROPFOLDER_URL), cardIdentifier);
    initWidget(uiBinder.createAndBindUi(this));
    ingestPanel.setVisible(JavascriptUtils.accessLocalStorage(cardIdentifier));
    ingestProcessDescription.add(new HTMLWidgetWrapper("IngestProcessDescription.html"));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateIngestJob.RESOLVER.getHistoryToken())) {
      CreateIngestJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(ShowJob.RESOLVER.getHistoryToken())) {
      ShowJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

}
