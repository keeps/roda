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
package org.roda.wui.client.planning;

import java.util.Date;
import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.lists.SimpleFileList;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.process.PluginOptionsPanel;
import org.roda.wui.client.search.Search;
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
public class CreateRiskJob extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 0) {
        final Pair<SelectedItems, String> selected = Search.getInstance().getSelected();
        if (SelectedItemsUtils.isEmpty(selected.getFirst())) {
          Tools.newHistory(RiskRegister.RESOLVER);
          callback.onSuccess(null);
        } else {
          BrowserService.Util.getInstance().getRiskJobBundle(new AsyncCallback<RiskJobBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
            }

            @Override
            public void onSuccess(RiskJobBundle bundle) {
              CreateRiskJob create = new CreateRiskJob(selected, bundle.getPlugins());
              callback.onSuccess(create);
            }
          });

        }
      } else {
        Tools.newHistory(CreateRiskJob.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {RiskRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "create_risk_job";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateRiskJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private final SelectedItems selected;
  private final String selectedType;
  private final List<PluginInfo> plugins;
  private PluginInfo selectedIngestPlugin = null;

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

  public CreateRiskJob(Pair<SelectedItems, String> selected, List<PluginInfo> plugins) {

    this.selected = selected.getFirst();
    this.selectedType = selected.getSecond();
    this.plugins = plugins;

    initWidget(uiBinder.createAndBindUi(this));

    name.setText(messages.ingestProcessNewDefaultName(new Date()));
    ingestWorkflowOptions.setSipToAipPlugins(plugins);

    updateObjectList();
    configureIngestPlugins();

    ingestWorkflowList.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String selectedPluginId = ingestWorkflowList.getSelectedValue();
        if (selectedPluginId != null) {
          CreateRiskJob.this.selectedIngestPlugin = lookupIngestPlugin(selectedPluginId);
        }
        updateWorkflowOptions();
      }
    });
  }

  private void updateObjectList() {

    if (selected != null) {
      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList) selected).getIds();

        if (IndexedAIP.class.getSimpleName().equals(selectedType)) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID, ids));
          AIPList list = new AIPList(filter, null, "AIPs", false);
          targetPanel.clear();
          targetPanel.add(list);
        }

        if (Representation.class.getSimpleName().equals(selectedType)) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_UUID, ids));
          RepresentationList list = new RepresentationList(filter, null, "Representations", false);
          targetPanel.clear();
          targetPanel.add(list);
        }

        if (File.class.getSimpleName().equals(selectedType)) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_UUID, ids));
          SimpleFileList list = new SimpleFileList(filter, null, "Files", false);
          targetPanel.clear();
          targetPanel.add(list);
        }

      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter) selected).getFilter();

        if (IndexedAIP.class.getSimpleName().equals(selectedType)) {
          AIPList list = new AIPList(filter, null, "AIPs", false);
          targetPanel.clear();
          targetPanel.add(list);
        }

        if (Representation.class.getSimpleName().equals(selectedType)) {
          RepresentationList list = new RepresentationList(filter, null, "Representations", false);
          targetPanel.clear();
          targetPanel.add(list);
        }

        if (File.class.getSimpleName().equals(selectedType)) {
          SimpleFileList list = new SimpleFileList(filter, null, "Files", false);
          targetPanel.clear();
          targetPanel.add(list);
        }

      } else {
        // do nothing
      }
    }

  }

  protected void configureIngestPlugins() {

    GWT.log(plugins.toString());

    if (plugins != null) {
      PluginUtils.sortByName(plugins);
      for (PluginInfo pluginInfo : plugins) {
        if (pluginInfo != null) {
          ingestWorkflowList.addItem(messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()),
            pluginInfo.getId());
        } else {
          GWT.log("Got a null plugin");
        }
      }

      ingestWorkflowList.setSelectedIndex(0);
      selectedIngestPlugin = plugins.get(0);
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
    if (plugins != null) {
      for (PluginInfo pluginInfo : plugins) {
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
    // agent.setFormatIds(formatIds.getTextBoxesValue());
    buttonCreate.setEnabled(false);
    String jobName = this.name.getText();

    BrowserService.Util.getInstance().createRiskProcess(jobName, selected, selectedType, selectedIngestPlugin.getId(),
      ingestWorkflowOptions.getValue(), new AsyncCallback<Job>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError("Error", caught.getMessage());
          buttonCreate.setEnabled(true);
        }

        @Override
        public void onSuccess(Job result) {
          Toast.showInfo("Done", "New ingest process created");
          Tools.newHistory(RiskRegister.RESOLVER);
        }
      });

  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(Search.RESOLVER);
  }

}
