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
import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
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
        final SelectedItems selected = IngestTransfer.getInstance().getSelected();
        if (SelectedItemsUtils.isEmpty(selected)) {
          Tools.newHistory(IngestTransfer.RESOLVER);
          callback.onSuccess(null);
        } else {
          BrowserService.Util.getInstance().getCreateIngestProcessBundle(new AsyncCallback<CreateIngestJobBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
            }

            @Override
            public void onSuccess(CreateIngestJobBundle bundle) {

              CreateJob create = new CreateJob(selected, bundle.getIngestPlugins(), bundle.getSipToAipPlugins());
              callback.onSuccess(create);
            }
          });

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

  private final SelectedItems selected;
  private final List<PluginInfo> ingestPlugins;

  @UiField
  TextBox name;

  @UiField
  FlowPanel targetPanel;

  @UiField
  ListBox ingestWorkflowList;

  @UiField
  Label ingestWorkflowListDescription;

  @UiField
  PluginOptionsPanel ingestWorkflowOptions;

  @UiField
  Button buttonCreate;

  @UiField
  Button buttonCancel;

  private PluginInfo selectedIngestPlugin = null;

  public CreateJob(SelectedItems selected, List<PluginInfo> ingestPlugins,
    List<PluginInfo> sipToAipPlugins) {
    this.selected = selected;
    this.ingestPlugins = ingestPlugins;

    initWidget(uiBinder.createAndBindUi(this));

    name.setText(messages.ingestProcessNewDefaultName(new Date()));
    ingestWorkflowOptions.setSipToAipPlugins(sipToAipPlugins);

    updateObjectList();
    configureIngestPlugins();

    ingestWorkflowList.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String selectedPluginId = ingestWorkflowList.getSelectedValue();
        GWT.log("ingest workflow changed");
        if (selectedPluginId != null) {
          CreateJob.this.selectedIngestPlugin = lookupIngestPlugin(selectedPluginId);
        }
        updateWorkflowOptions();
      }
    });
  }

  private void updateObjectList() {

    if (selected != null) {
      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList) selected).getIds();
        Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_UUID, ids));
        TransferredResourceList list = new TransferredResourceList(filter, null, "Transferred resources", false, 10,
          10);
        targetPanel.clear();
        targetPanel.add(list);
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter) selected).getFilter();
        TransferredResourceList list = new TransferredResourceList(filter, null, "Transferred resources", false, 10,
          10);
        targetPanel.clear();
        targetPanel.add(list);
      } else {
        // do nothing
      }
    }

  }

  protected void configureIngestPlugins() {
    if (ingestPlugins != null) {
      PluginUtils.sortByName(ingestPlugins);
      for (PluginInfo pluginInfo : ingestPlugins) {
        if (pluginInfo != null) {
          ingestWorkflowList.addItem(messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()),
            pluginInfo.getId());
        } else {
          GWT.log("Got a null plugin");
        }
      }

      ingestWorkflowList.setSelectedIndex(0);
      selectedIngestPlugin = ingestPlugins.get(0);
      updateWorkflowOptions();
    }
  }

  protected void updateWorkflowOptions() {
    if (selectedIngestPlugin == null) {
      ingestWorkflowListDescription.setText("");
      ingestWorkflowListDescription.setVisible(false);
      ingestWorkflowOptions.setPluginInfo(null);
    } else {
      String description = selectedIngestPlugin.getDescription();
      if (description != null && description.length() > 0) {
        ingestWorkflowListDescription.setText(description);
        ingestWorkflowListDescription.setVisible(true);
      } else {
        ingestWorkflowListDescription.setVisible(false);
      }

      ingestWorkflowOptions.setPluginInfo(selectedIngestPlugin);

    }
  }

  private PluginInfo lookupIngestPlugin(String selectedPluginId) {
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
    buttonCreate.setEnabled(false);
    String jobName = this.name.getText();

    List<PluginParameter> missingMandatoryParameters = ingestWorkflowOptions.getMissingMandatoryParameters();
    if (missingMandatoryParameters.isEmpty()) {

      BrowserService.Util.getInstance().createIngestProcess(jobName, selected, selectedIngestPlugin.getId(),
        ingestWorkflowOptions.getValue(), new AsyncCallback<Job>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError("Error", caught.getMessage());
            buttonCreate.setEnabled(true);
          }

          @Override
          public void onSuccess(Job result) {
            Toast.showInfo("Done", "New ingest process created");
            Tools.newHistory(IngestProcess.RESOLVER);
          }
        });
    } else {

      List<String> missingPluginNames = new ArrayList<>();
      for (PluginParameter parameter : missingMandatoryParameters) {
        missingPluginNames.add(parameter.getName());
      }

      Dialogs.showInformationDialog(messages.ingestProcessNewMissingMandatoryInfoDialogTitle(),
        messages.ingestProcessNewMissingMandatoryInfoDialogMessage(missingPluginNames), messages.dialogOk(),
        new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError(caught.getMessage());
            buttonCreate.setEnabled(true);
          }

          @Override
          public void onSuccess(Void result) {
            // do nothing
            buttonCreate.setEnabled(true);
          }
        });
    }

  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(IngestProcess.RESOLVER);
  }

}
