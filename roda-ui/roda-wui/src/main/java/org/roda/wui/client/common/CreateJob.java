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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.process.PluginOptionsPanel;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.process.CreateActionJob;
import org.roda.wui.client.process.CreateIngestJob;
import org.roda.wui.client.process.Process;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
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

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        if (historyTokens.get(0).equals("ingest")) {
          CreateIngestJob createIngestJob = new CreateIngestJob();
          callback.onSuccess(createIngestJob);
        } else if (historyTokens.get(0).equals("action")) {
          CreateActionJob createActionJob = new CreateActionJob();
          callback.onSuccess(createActionJob);
        } else {
          Tools.newHistory(CreateJob.RESOLVER);
          callback.onSuccess(null);
        }
      } else {
        Tools.newHistory(CreateJob.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Process.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Process.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "create";
    }
  };

  @SuppressWarnings("rawtypes")
  public interface MyUiBinder extends UiBinder<Widget, CreateJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private SelectedItems selected = null;
  private List<PluginInfo> plugins = null;
  private PluginInfo selectedPlugin = null;
  private String selectedClass = null;

  @UiField
  TextBox name;

  @UiField
  FlowPanel targetPanel;

  @UiField
  Label selectedObject;

  @UiField
  Label workflowCategoryLabel;

  @UiField
  FlowPanel workflowCategoryList;

  @UiField
  ListBox workflowList;

  @UiField
  Label workflowListDescription;

  @UiField
  FlowPanel workflowPanel;

  @UiField
  PluginOptionsPanel workflowOptions;

  @UiField
  Button buttonCreate;

  @UiField
  Button buttonCancel;

  public CreateJob(Class<T> classToReceive, final List<PluginType> pluginType) {
    if (classToReceive.getCanonicalName().equals(TransferredResource.class.getCanonicalName())) {
      this.selected = IngestTransfer.getInstance().getSelected();
    } else {
      this.selected = Search.getInstance().getSelected();
    }

    initWidget(uiBinder.createAndBindUi(this));
    BrowserService.Util.getInstance().getPluginsInfo(pluginType, new AsyncCallback<List<PluginInfo>>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(List<PluginInfo> pluginsInfo) {
        init(pluginsInfo);
      }
    });
  }

  public void init(List<PluginInfo> plugins) {

    this.plugins = plugins;

    name.setText(messages.processNewDefaultName(new Date()));
    workflowOptions.setPlugins(plugins);

    boolean isEmpty = updateObjectList();
    configurePlugins(isEmpty, selected.getSelectedClass());

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

  public abstract boolean updateObjectList();

  protected void configurePlugins(boolean isEmpty, String selectedClass) {
    // TODO filter pluginInfos considering the empty or selected type
    GWT.log(plugins.toString());
    List<String> categoriesOnListBox = new ArrayList<String>();

    if (plugins != null) {
      PluginUtils.sortByName(plugins);

      for (PluginInfo pluginInfo : plugins) {
        if (pluginInfo != null) {

          List<String> pluginCategories = pluginInfo.getCategories();

          if (pluginCategories != null) {
            for (String category : pluginCategories) {
              if (!categoriesOnListBox.contains(category)
                && !category.equals(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)) {

                CheckBox box = new CheckBox();
                box.setText(category);

                box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                  @Override
                  public void onValueChange(ValueChangeEvent<Boolean> event) {
                    workflowList.clear();
                    boolean noChecks = true;

                    if (plugins != null) {
                      PluginUtils.sortByName(plugins);
                      for (PluginInfo pluginInfo : plugins) {
                        if (pluginInfo != null) {
                          List<String> categories = pluginInfo.getCategories();

                          if (categories != null) {
                            for (int i = 0; i < workflowCategoryList.getWidgetCount(); i++) {
                              CheckBox checkbox = (CheckBox) workflowCategoryList.getWidget(i);

                              if (checkbox.isChecked()) {
                                if (categories.contains(checkbox.getText())
                                  && !categories.contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)) {
                                  workflowList.addItem(
                                    messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()),
                                    pluginInfo.getId());
                                }

                                noChecks = false;
                              }
                            }

                            if (noChecks) {
                              if (!pluginInfo.getCategories().contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)) {
                                workflowList.addItem(
                                  messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()),
                                  pluginInfo.getId());
                              }
                            }
                          }
                        }
                      }
                    }

                    String selectedPluginId = workflowList.getSelectedValue();
                    if (selectedPluginId != null) {
                      CreateJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
                    }
                    updateWorkflowOptions();
                  }

                });

                workflowCategoryList.add(box);
                categoriesOnListBox.add(category);
              }
            }

            if (!pluginCategories.contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)) {
              workflowList.addItem(messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()),
                pluginInfo.getId());
            }
          }

        } else {
          GWT.log("Got a null plugin");
        }
      }

      String selectedPluginId = workflowList.getSelectedValue();
      if (selectedPluginId != null) {
        CreateJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
      }
      updateWorkflowOptions();
    }
  }

  protected void updateWorkflowOptions() {
    if (selectedPlugin == null) {
      workflowListDescription.setText("");
      workflowListDescription.setVisible(false);
      workflowOptions.setPluginInfo(null);
    } else {
      name.setText(selectedPlugin.getName());
      String description = selectedPlugin.getDescription();
      if (description != null && description.length() > 0) {
        workflowListDescription.setText(description);
        workflowListDescription.setVisible(true);
      } else {
        workflowListDescription.setVisible(false);
      }

      if (selectedPlugin.getParameters().size() == 0) {
        workflowPanel.setVisible(false);
      } else {
        workflowPanel.setVisible(true);
        workflowOptions.setPluginInfo(selectedPlugin);
      }

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

  public void setJobSelectedDescription(String text) {
    selectedObject.setText(text);
  }

  public void setCategoryListBoxVisible(boolean visible) {
    workflowCategoryLabel.setVisible(visible);
    workflowCategoryList.setVisible(visible);
  }

  public String getSelectedClass() {
    return selectedClass;
  }

  public void setSelectedClass(String selectedClass) {
    this.selectedClass = selectedClass;
  }

}
