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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsAll;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.index.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.DefaultSelectDialog;
import org.roda.wui.client.common.dialogs.SelectDialogFactory;
import org.roda.wui.client.common.lists.BasicAsyncTableCell;
import org.roda.wui.client.common.lists.ListFactory;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.process.PluginOptionsPanel;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

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

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateActionJob extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 0) {
        CreateActionJob createActionJob = new CreateActionJob();
        callback.onSuccess(createActionJob);
      } else {
        Tools.newHistory(CreateActionJob.RESOLVER);
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
      return "create_job";
    }
  };

  public interface MyUiBinder extends UiBinder<Widget, CreateActionJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private SelectedItems selected = new SelectedItemsNone();
  private List<PluginInfo> plugins = null;
  private PluginInfo selectedPlugin = null;
  private Map<String, String> rodaMap;

  private static List<PluginType> pluginTypes = PluginUtils.getPluginTypesWithoutIngest();

  @UiField
  TextBox name;

  @UiField
  Label workflowListTitle;

  @UiField
  Label workflowCategoryLabel;

  @UiField
  FlowPanel workflowCategoryList;

  @UiField
  ListBox workflowList;

  @UiField
  Label workflowListDescription, workflowListDescriptionCategories;

  @UiField
  FlowPanel workflowPanel;

  @UiField
  PluginOptionsPanel workflowOptions;

  @UiField
  Label selectedObject;

  @UiField
  ListBox targetList;

  @UiField
  FlowPanel targetListPanel;

  @UiField
  Button buttonCreate;

  @UiField
  Button buttonCancel;

  @UiField
  Button buttonSelect;

  public CreateActionJob() {
    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrievePluginsInfo(pluginTypes, new AsyncCallback<List<PluginInfo>>() {

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
    configurePlugins();

    workflowList.setVisibleItemCount(20);

    workflowCategoryList.addStyleName("form-listbox-job");
    workflowList.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String selectedPluginId = workflowList.getSelectedValue();
        if (selectedPluginId != null) {
          CreateActionJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
        }

        updateWorkflowOptions();
      }
    });
  }

  public void configurePlugins() {
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
                box.setText(messages.showPluginCategories(category));
                box.setName(category);
                box.addStyleName("form-checkbox-job");

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

                              if (checkbox.getValue()) {
                                noChecks = false;

                                if (categories.contains(checkbox.getName())
                                  && !categories.contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)) {
                                  workflowList.addItem(
                                    messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()),
                                    pluginInfo.getId());
                                }

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

                    workflowList.setSelectedIndex(0);
                    String selectedPluginId = workflowList.getSelectedValue();
                    if (selectedPluginId != null) {
                      CreateActionJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
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
        CreateActionJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
      }

      updateWorkflowOptions();
    }
  }

  protected void updateWorkflowOptions() {
    if (selectedPlugin == null) {
      workflowListDescription.setText("");
      workflowListDescriptionCategories.setText("");
      workflowListDescription.setVisible(false);
      workflowListDescriptionCategories.setVisible(false);
      workflowOptions.setPluginInfo(null);
    } else {
      String pluginName = messages.pluginLabel(selectedPlugin.getName(), selectedPlugin.getVersion());
      name.setText(pluginName);
      workflowListTitle.setText(pluginName);

      String description = selectedPlugin.getDescription();
      if (description != null && description.length() > 0) {
        workflowListDescription.setText(description);

        String categories = messages.createJobCategoryWorkflow() + ": ";
        for (String category : selectedPlugin.getCategories()) {
          categories += messages.showPluginCategories(category) + ", ";
        }

        workflowListDescriptionCategories.setText(categories.substring(0, categories.length() - 2));
        workflowListDescription.setVisible(true);
        workflowListDescriptionCategories.setVisible(true);
      } else {
        workflowListDescription.setVisible(false);
        workflowListDescriptionCategories.setVisible(false);
      }

      if (selectedPlugin.getParameters().size() == 0) {
        workflowPanel.setVisible(false);
      } else {
        workflowPanel.setVisible(true);
        workflowOptions.setPluginInfo(selectedPlugin);
      }

      targetList.clear();
      rodaMap = getPluginNames(selectedPlugin.getObjectClasses());
      for (Entry<String, String> objectClass : rodaMap.entrySet()) {
        targetList.addItem(messages.allOfAObject(objectClass.getKey()), objectClass.getKey());
        buttonSelect.setVisible(!(objectClass.getKey().equals(org.roda.core.data.v2.Void.class.getName())));
      }

      targetList.addChangeHandler(new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          targetListPanel.clear();
          defineTargetInformation(targetList.getSelectedValue());
        }
      });

      targetListPanel.clear();
      defineTargetInformation(targetList.getSelectedValue());
    }
  }

  private Map<String, String> getPluginNames(Set<String> objectClasses) {
    Map<String, String> objectMap = new HashMap<String, String>();
    for (String objectClass : objectClasses) {
      if (AIP.class.getName().equals(objectClass) || IndexedAIP.class.getName().equals(objectClass)) {
        objectMap.put(AIP.class.getName(), IndexedAIP.class.getName());
      } else if (Representation.class.getName().equals(objectClass)
        || IndexedRepresentation.class.getName().equals(objectClass)) {
        objectMap.put(Representation.class.getName(), IndexedRepresentation.class.getName());
      } else if (File.class.getName().equals(objectClass) || IndexedFile.class.getName().equals(objectClass)) {
        objectMap.put(File.class.getName(), IndexedFile.class.getName());
      } else if (Risk.class.getName().equals(objectClass) || IndexedRisk.class.getName().equals(objectClass)) {
        objectMap.put(Risk.class.getName(), IndexedRisk.class.getName());
      } else if (RiskIncidence.class.getName().equals(objectClass)) {
        objectMap.put(RiskIncidence.class.getName(), RiskIncidence.class.getName());
      } else if (Format.class.getName().equals(objectClass)) {
        objectMap.put(Format.class.getName(), Format.class.getName());
      } else if (Notification.class.getName().equals(objectClass)) {
        objectMap.put(Notification.class.getName(), Notification.class.getName());
      } else if (Job.class.getName().equals(objectClass)) {
        objectMap.put(Job.class.getName(), Job.class.getName());
      } else if (Report.class.getName().equals(objectClass)) {
        objectMap.put(Report.class.getName(), Report.class.getName());
      } else if (LogEntry.class.getName().equals(objectClass)) {
        objectMap.put(LogEntry.class.getName(), LogEntry.class.getName());
      } else if (TransferredResource.class.getName().equals(objectClass)) {
        objectMap.put(TransferredResource.class.getName(), TransferredResource.class.getName());
      } else if (org.roda.core.data.v2.Void.class.getName().equals(objectClass)) {
        objectMap.put(org.roda.core.data.v2.Void.class.getName(), org.roda.core.data.v2.Void.class.getName());
      }
    }
    return objectMap;
  }

  private PluginInfo lookupPlugin(String selectedPluginId) {
    PluginInfo p = null;
    if (plugins != null && selectedPluginId != null) {
      for (PluginInfo pluginInfo : plugins) {
        if (pluginInfo != null && pluginInfo.getId().equals(selectedPluginId)) {
          p = pluginInfo;
          break;
        }
      }
    }
    return p;
  }

  private void defineTargetInformation(String objectClassName) {
    ListFactory listFactory = new ListFactory();
    try {
      BasicAsyncTableCell list = listFactory.getList(objectClassName, "", Filter.ALL, 10, 10);
      selected = new SelectedItemsAll(objectClassName);
      targetListPanel.add(list);
      targetListPanel.setVisible(true);
    } catch (RODAException e) {
      targetListPanel.setVisible(false);
    }
  }

  @UiHandler("buttonCreate")
  public void buttonCreateHandler(ClickEvent e) {
    getButtonCreate().setEnabled(false);
    String jobName = getName().getText();

    if (selected == null || selected instanceof SelectedItemsNone) {
      selected = new SelectedItemsAll(targetList.getSelectedValue());
    }

    BrowserService.Util.getInstance().createProcess(jobName, selected, getSelectedPlugin().getId(),
      getWorkflowOptions().getValue(), selected.getSelectedClass(), new AsyncCallback<Job>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(messages.dialogFailure(), caught.getMessage());
          getButtonCreate().setEnabled(true);
        }

        @Override
        public void onSuccess(Job result) {
          Toast.showInfo(messages.dialogDone(), messages.processCreated());
          Tools.newHistory(ActionProcess.RESOLVER);
        }
      });

  }

  @UiHandler("buttonSelect")
  public void buttonSelectHandler(ClickEvent e) {
    SelectDialogFactory factory = new SelectDialogFactory();
    try {
      final DefaultSelectDialog<?, ?> dialog = factory.getSelectDialog(targetList.getSelectedValue(),
        targetList.getSelectedItemText(), Filter.ALL, true);
      dialog.showAndCenter();
      dialog.addValueChangeHandler(new ValueChangeHandler() {
        @Override
        public void onValueChange(ValueChangeEvent event) {
          targetListPanel.clear();
          ListFactory listFactory = new ListFactory();

          try {
            selected = dialog.getList().getSelected();
            Filter filter = new Filter();

            if (selected instanceof SelectedItemsList) {
              SelectedItemsList selectedList = (SelectedItemsList) selected;

              if (Representation.class.getName().equals(targetList.getSelectedValue())
                || File.class.getName().equals(targetList.getSelectedValue())) {
                filter.add(new OneOfManyFilterParameter(RodaConstants.OBJECT_GENERIC_UUID, selectedList.getIds()));
              } else {
                filter.add(new OneOfManyFilterParameter(RodaConstants.OBJECT_GENERIC_ID, selectedList.getIds()));
              }
            } else {
              filter = new Filter(dialog.getList().getFilter());
            }

            BasicAsyncTableCell list = listFactory.getList(targetList.getSelectedValue(), dialog.getTitle(), filter, 10,
              10);
            targetListPanel.add(list);
          } catch (RODAException e) {
            // do nothing
          }
        }
      });
    } catch (NotFoundException e1) {
      // do nothing
    }
  }

  @UiHandler("buttonCancel")
  public void cancel(ClickEvent e) {
    Tools.newHistory(ActionProcess.RESOLVER);
  }

  public SelectedItems<?> getSelected() {
    return selected;
  }

  public PluginInfo getSelectedPlugin() {
    return selectedPlugin;
  }

  public void setSelectedPlugin(PluginInfo selectedPlugin) {
    this.selectedPlugin = selectedPlugin;
  }

  public Button getButtonCreate() {
    return this.buttonCreate;
  }

  public TextBox getName() {
    return this.name;
  }

  public ListBox getWorkflowList() {
    return workflowList;
  }

  public PluginOptionsPanel getWorkflowOptions() {
    return this.workflowOptions;
  }

  public void setCategoryListBoxVisible(boolean visible) {
    workflowCategoryLabel.setVisible(visible);
    workflowCategoryList.setVisible(visible);
  }

  public FlowPanel getCategoryList() {
    return workflowCategoryList;
  }

}
