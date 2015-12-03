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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.PluginInfo;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.PluginType;
import org.roda.core.data.v2.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateJob extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 0) {
        Set<TransferredResource> selected = IngestTransfer.getInstance().getSelected();
        if (selected.isEmpty()) {
          Tools.newHistory(IngestTransfer.RESOLVER);
          callback.onSuccess(null);
        } else {
          CreateJob create = new CreateJob(selected);
          callback.onSuccess(create);
        }
      } else {
        Tools.newHistory(CreateJob.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for create job permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {IngestProcess.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(IngestProcess.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "create_job";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private final Set<TransferredResource> selected;

  @UiField
  TextBox name;

  @UiField
  HTML objectList;

  @UiField
  ListBox ingestWorkflowList;

  @UiField
  FlowPanel ingestWorkflowOptions;

  @UiField
  Button buttonCreate;

  @UiField
  Button buttonCancel;

  private List<PluginInfo> ingestPlugins = null;
  private PluginInfo selectedPlugin = null;

  public CreateJob(Set<TransferredResource> selected) {
    this.selected = selected;
    initWidget(uiBinder.createAndBindUi(this));
    updateObjectList();
    name.setText(messages.ingestProcessNewDefaultName(new Date()));

    BrowserService.Util.getInstance().getPluginsInfo(PluginType.INGEST, new AsyncCallback<List<PluginInfo>>() {

      @Override
      public void onFailure(Throwable caught) {
        Toast.showError("Error", caught.getMessage());
      }

      @Override
      public void onSuccess(List<PluginInfo> ingestPlugins) {
        CreateJob.this.ingestPlugins = ingestPlugins;
        configurePlugins(ingestPlugins);
      }
    });

    ingestWorkflowList.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String selectedPluginId = ingestWorkflowList.getSelectedValue();
        if (selectedPluginId != null) {
          CreateJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
        }
        updateWorkflowOptions();
      }
    });

  }

  private void updateObjectList() {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<ul>"));

    for (TransferredResource transferredResource : selected) {
      b.append(SafeHtmlUtils.fromSafeConstant("<li>"));
      b.append(SafeHtmlUtils.fromSafeConstant(
        transferredResource.isFile() ? "<i class='fa fa-file-o'></i>" : "<i class='fa fa-folder-o'></i>"));
      b.append(SafeHtmlUtils.fromString(transferredResource.getName()));
      b.append(SafeHtmlUtils.fromSafeConstant("</li>"));
    }

    b.append(SafeHtmlUtils.fromSafeConstant("</ul>"));
    objectList.setHTML(b.toSafeHtml());

  }

  protected void configurePlugins(List<PluginInfo> ingestPlugins) {
    for (PluginInfo pluginInfo : ingestPlugins) {
      if (pluginInfo != null) {
        ingestWorkflowList.addItem(messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()),
          pluginInfo.getId());
      } else {
        GWT.log("Got a null plugin");
      }
    }
    ingestWorkflowList.setSelectedIndex(0);
  }

  protected void updateWorkflowOptions() {
    ingestWorkflowOptions.clear();
    if (selectedPlugin == null) {
      // TODO set fall-back message
    } else {
      selectedPlugin.getDescription();
      Label description = new Label(selectedPlugin.getDescription());
      ingestWorkflowOptions.add(description);
    }
  }

  private PluginInfo lookupPlugin(String selectedPluginId) {
    PluginInfo p = null;
    if (ingestPlugins != null) {
      for (PluginInfo pluginInfo : ingestPlugins) {
        if (pluginInfo.getId().equals(selectedPluginId)) {
          p = pluginInfo;
          break;
        }
      }
    }
    return p;
  }

  @UiHandler("buttonCreate")
  void buttonCreateHandler(ClickEvent e) {
    String jobName = this.name.getText();
    // TODO test if name is valid
    Job job = new Job();
    job.setName(jobName);

    List<String> objectIds = new ArrayList<String>();
    for (TransferredResource r : selected) {
      objectIds.add(r.getId());
    }
    job.setObjectIds(objectIds);
    job.setOrchestratorMethod("runPluginOnTransferredResources");

    // TODO get plugin from list
    job.setPlugin("org.roda.core.plugins.plugins.ingest.SimpleIngestPlugin");
    Map<String, String> pluginParameters = new HashMap<String, String>();
    pluginParameters.put("parameter.sip_to_aip_class", "org.roda.core.plugins.plugins.ingest.BagitToAIPPlugin");
    pluginParameters.put("parameter.do_virus_check", "false");
    job.setPluginParameters(pluginParameters);

    BrowserService.Util.getInstance().createJob(job, new AsyncCallback<Job>() {

      @Override
      public void onFailure(Throwable caught) {
        Toast.showError("Error", caught.getMessage());
      }

      @Override
      public void onSuccess(Job result) {
        Toast.showInfo("Done", "New ingest process created");
        Tools.newHistory(IngestProcess.RESOLVER);
      }
    });

  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(IngestProcess.RESOLVER);
  }

}
