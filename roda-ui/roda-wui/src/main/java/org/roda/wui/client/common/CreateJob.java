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
package org.roda.wui.client.common;

import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.process.PluginOptionsPanel;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.search.Search;

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
public abstract class CreateJob<T extends IsIndexed> extends Composite {

  @SuppressWarnings("rawtypes")
  public interface MyUiBinder extends UiBinder<Widget, CreateJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private SelectedItems selected = null;
  private List<PluginInfo> plugins = null;
  private PluginInfo selectedPlugin = null;

  @UiField
  TextBox name;

  @UiField
  FlowPanel targetPanel;

  @UiField
  ListBox workflowList;

  @UiField
  Label workflowListDescription;

  @UiField
  PluginOptionsPanel workflowOptions;

  @UiField
  Button buttonCreate;

  @UiField
  Button buttonCancel;

  public CreateJob(Class<T> classToReceive, final List<PluginType> pluginType) {
    SelectedItems selectedItems = null;

    if (classToReceive.getName().equals(TransferredResource.class.getName())) {
      selectedItems = IngestTransfer.getInstance().getSelected();
    } else {
      selectedItems = Search.getInstance().getSelected();
    }

    initWidget(uiBinder.createAndBindUi(this));
    final SelectedItems items = selectedItems;

    BrowserService.Util.getInstance().getPluginsInfo(pluginType, new AsyncCallback<List<PluginInfo>>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(List<PluginInfo> pluginsInfo) {
        init(items, pluginsInfo);
      }
    });
  }

  public void init(SelectedItems selected, List<PluginInfo> plugins) {

    this.selected = selected;
    this.plugins = plugins;

    name.setText(messages.processNewDefaultName(new Date()));
    workflowOptions.setPlugins(plugins);

    updateObjectList();
    configurePlugins();

    workflowList.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String selectedPluginId = workflowList.getSelectedValue();
        if (selectedPluginId != null) {
          CreateJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
        }
        updateWorkflowOptions();
      }
    });
  }

  public abstract void updateObjectList();

  protected void configurePlugins() {

    GWT.log(plugins.toString());

    if (plugins != null) {
      PluginUtils.sortByName(plugins);
      for (PluginInfo pluginInfo : plugins) {
        if (pluginInfo != null) {
          workflowList.addItem(messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()), pluginInfo.getId());
        } else {
          GWT.log("Got a null plugin");
        }
      }

      workflowList.setSelectedIndex(0);
      selectedPlugin = plugins.get(0);
      updateWorkflowOptions();
    }
  }

  protected void updateWorkflowOptions() {
    if (selectedPlugin == null) {
      workflowListDescription.setText("");
      workflowListDescription.setVisible(false);
      workflowOptions.setPluginInfo(null);
    } else {
      String description = selectedPlugin.getDescription();
      if (description != null && description.length() > 0) {
        workflowListDescription.setText(description);
        workflowListDescription.setVisible(true);
      } else {
        workflowListDescription.setVisible(false);
      }

      workflowOptions.setPluginInfo(selectedPlugin);

    }
  }

  private PluginInfo lookupPlugin(String selectedPluginId) {
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
  public abstract void buttonCreateHandler(ClickEvent e);

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  public abstract void cancel();

  public SelectedItems getSelected() {
    return selected;
  }

  public void setSelected(SelectedItems selected) {
    this.selected = selected;
  }

  public PluginInfo getSelectedPlugin() {
    return selectedPlugin;
  }

  public void setSelectedPlugin(PluginInfo selectedPlugin) {
    this.selectedPlugin = selectedPlugin;
  }

  public FlowPanel getTargetPanel() {
    return this.targetPanel;
  }

  public Button getButtonCreate() {
    return this.buttonCreate;
  }

  public TextBox getName() {
    return this.name;
  }

  public PluginOptionsPanel getWorkflowOptions() {
    return this.workflowOptions;
  }

}
