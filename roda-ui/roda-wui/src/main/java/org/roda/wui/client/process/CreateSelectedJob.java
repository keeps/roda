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
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.process.PluginOptionsPanel;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public abstract class CreateSelectedJob<T extends IsIndexed> extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        if (RodaConstants.JOB_PROCESS_INGEST.equals(historyTokens.get(0))) {
          CreateIngestJob createIngestJob = new CreateIngestJob();
          callback.onSuccess(createIngestJob);
        } else if (RodaConstants.JOB_PROCESS_ACTION.equals(historyTokens.get(0))) {
          CreateActionJob createActionJob = new CreateActionJob();
          callback.onSuccess(createActionJob);
        } else {
          HistoryUtils.newHistory(CreateSelectedJob.RESOLVER);
          callback.onSuccess(null);
        }
      } else {
        HistoryUtils.newHistory(CreateSelectedJob.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Process.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Process.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create";
    }
  };

  @SuppressWarnings("rawtypes")
  public interface MyUiBinder extends UiBinder<Widget, CreateSelectedJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private SelectedItems<?> selected = new SelectedItemsNone<>();
  private List<PluginInfo> plugins = null;
  private PluginInfo selectedPlugin = null;
  private String listSelectedClass = TransferredResource.class.getName();

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
  FlowPanel workflowList;

  @UiField
  FlowPanel workflowListDescription;

  @UiField
  Label workflowListTitle;

  @UiField
  HTML workflowListDescriptionCategories;

  @UiField
  FlowPanel workflowPanel;

  @UiField
  PluginOptionsPanel workflowOptions;

  @UiField
  Button buttonCreate;

  @UiField
  Button buttonCancel;

  public CreateSelectedJob(final List<PluginType> pluginType) {
    this.selected = LastSelectedItemsSingleton.getInstance().getSelectedItems();

    initWidget(uiBinder.createAndBindUi(this));

    boolean isEmpty = updateObjectList();
    if (isEmpty) {
      List<String> lastHistory = LastSelectedItemsSingleton.getInstance().getLastHistory();
      HistoryUtils.newHistory(lastHistory);
    }

    BrowserService.Util.getInstance().retrievePluginsInfo(pluginType, new AsyncCallback<List<PluginInfo>>() {

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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void init(List<PluginInfo> plugins) {
    this.plugins = plugins;
    name.setText(messages.processNewDefaultName(new Date()));
    workflowOptions.setPlugins(plugins);
    configurePlugins(selected.getSelectedClass());
    workflowCategoryList.addStyleName("form-listbox-job");
  }

  public abstract boolean updateObjectList();

  public void configurePlugins(final String selectedClass) {
    List<String> categoriesOnListBox = new ArrayList<>();

    if (plugins != null) {
      PluginUtils.sortByName(plugins);

      int pluginAdded = 0;
      for (PluginInfo pluginInfo : plugins) {

        if (pluginInfo != null) {
          List<String> pluginCategories = pluginInfo.getCategories();

          if (pluginCategories != null) {
            for (String category : pluginCategories) {
              if (!categoriesOnListBox.contains(category)
                && !category.equals(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)
                && ((!isSelectedEmpty() && pluginInfo.hasObjectClass(selectedClass))
                  || (isSelectedEmpty() && pluginInfo.hasObjectClass(listSelectedClass)))) {

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

                      int pluginsAdded = 0;
                      for (PluginInfo pluginInfo : plugins) {
                        if (pluginInfo != null) {
                          List<String> categories = pluginInfo.getCategories();

                          if (categories != null) {
                            for (int i = 0; i < workflowCategoryList.getWidgetCount(); i++) {
                              CheckBox checkbox = (CheckBox) workflowCategoryList.getWidget(i);

                              if (checkbox.getValue()) {
                                noChecks = false;

                                if (categories.contains(checkbox.getName())
                                  && !categories.contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)
                                  && ((!isSelectedEmpty() && pluginInfo.hasObjectClass(selectedClass))
                                    || (isSelectedEmpty() && pluginInfo.hasObjectClass(listSelectedClass)))) {
                                  Widget pluginItem = addPluginItemWidgetToWorkflowList(pluginInfo);
                                  if (pluginsAdded == 0) {
                                    CreateSelectedJob.this.selectedPlugin = lookupPlugin(pluginInfo.getId());
                                    pluginItem.addStyleName("plugin-list-item-selected");
                                    pluginsAdded++;
                                  }
                                }

                              }
                            }

                            if (noChecks) {
                              if (!pluginInfo.getCategories().contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)
                                && ((!isSelectedEmpty() && pluginInfo.hasObjectClass(selectedClass))
                                  || (isSelectedEmpty() && pluginInfo.hasObjectClass(listSelectedClass)))) {
                                Widget pluginItem = addPluginItemWidgetToWorkflowList(pluginInfo);
                                if (pluginsAdded == 0) {
                                  CreateSelectedJob.this.selectedPlugin = lookupPlugin(pluginInfo.getId());
                                  pluginItem.addStyleName("plugin-list-item-selected");
                                  pluginsAdded++;
                                }
                              }
                            }
                          }
                        }
                      }
                    }

                    updateWorkflowOptions();
                  }

                });

                workflowCategoryList.add(box);
                categoriesOnListBox.add(category);
              }
            }

            if (!pluginCategories.contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)
              && ((!isSelectedEmpty() && pluginInfo.hasObjectClass(selectedClass))
                || (isSelectedEmpty() && pluginInfo.hasObjectClass(listSelectedClass)))) {
              Widget pluginItem = addPluginItemWidgetToWorkflowList(pluginInfo);
              if (pluginAdded == 0) {
                CreateSelectedJob.this.selectedPlugin = lookupPlugin(pluginInfo.getId());
                pluginItem.addStyleName("plugin-list-item-selected");
                pluginAdded++;
              }
            }
          }

        } else {
          GWT.log("Got a null plugin");
        }
      }

      updateWorkflowOptions();
    }
  }

  private Widget addPluginItemWidgetToWorkflowList(PluginInfo pluginInfo) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("plugin-list-item");
    panel.getElement().setAttribute("data-id", pluginInfo.getId());

    panel.addDomHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        FlowPanel panel = (FlowPanel) event.getSource();
        String selectedPluginId = panel.getElement().getAttribute("data-id");

        for (int i = 0; i < workflowList.getWidgetCount(); i++) {
          Widget panelWidget = workflowList.getWidget(i);
          panelWidget.removeStyleName("plugin-list-item-selected");
        }

        if (selectedPluginId != null) {
          CreateSelectedJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
          panel.addStyleName("plugin-list-item-selected");
        }

        updateWorkflowOptions();
      }

    }, ClickEvent.getType());

    FlowPanel itemImage = new FlowPanel();
    itemImage.addStyleName("fa");
    itemImage.addStyleName("plugin-list-item-icon");
    if (pluginInfo.getCategories().isEmpty()) {
      itemImage.addStyleName("plugin-list-item-icon-default");
    } else {
      itemImage.addStyleName("plugin-list-item-icon-" + pluginInfo.getCategories().get(0));
      itemImage.setTitle(pluginInfo.getCategories().get(0));
    }

    Label label = new Label();
    String labelContent = messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion());
    label.setText(labelContent);
    label.setTitle(labelContent);
    label.addStyleName("plugin-list-item-label");

    panel.add(itemImage);
    panel.add(label);

    workflowList.add(panel);
    return panel;
  }

  protected void updateWorkflowOptions() {
    if (selectedPlugin == null) {
      workflowListDescription.clear();
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
        String[] split = description.split("\\r?\\n");
        workflowListDescription.clear();

        for (String s : split) {
          Label descriptionLine = new Label(s);
          descriptionLine.addStyleName("p");
          descriptionLine.addStyleName("plugin-description");
          workflowListDescription.add(descriptionLine);
        }

        List<String> categoryTranslations = new ArrayList<>();
        for (String category : selectedPlugin.getCategories()) {
          categoryTranslations.add(messages.showPluginCategories(category));
        }

        SafeHtml categories = messages.createJobCategoryWorkflow(categoryTranslations);

        workflowListDescriptionCategories.setHTML(categories);
        workflowListDescription.setVisible(true);
        workflowListDescriptionCategories.setVisible(true);
      } else {
        workflowListDescription.setVisible(false);
        workflowListDescriptionCategories.setVisible(false);
      }

      if (selectedPlugin.getParameters().isEmpty()) {
        workflowPanel.setVisible(false);
        workflowOptions.setPluginInfo(null);
      } else {
        workflowPanel.setVisible(true);
        workflowOptions.setPluginInfo(selectedPlugin);
      }

    }
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

  @UiHandler("buttonCreate")
  public abstract void buttonCreateHandler(ClickEvent e);

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  public abstract void cancel();

  public SelectedItems<?> getSelected() {
    return selected;
  }

  public void setSelected(SelectedItems<?> selected) {
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

  public FlowPanel getWorkflowList() {
    return workflowList;
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
    return listSelectedClass;
  }

  public void setSelectedClass(String selectedClass) {
    this.listSelectedClass = selectedClass;
  }

  public FlowPanel getCategoryList() {
    return workflowCategoryList;
  }

  public boolean isSelectedEmpty() {
    if (selected instanceof SelectedItemsList) {
      return ((SelectedItemsList<?>) selected).getIds().isEmpty();
    }
    return false;
  }

}
