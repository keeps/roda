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
import org.roda.core.data.v2.jobs.CertificateInfo;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.LicenseInfo;
import org.roda.core.data.v2.jobs.MarketInfo;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.BadgePanel;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.process.PluginOptionsPanel;
import org.roda.wui.client.main.Theme;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TabPanel;
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
  private JobPriority priority = JobPriority.MEDIUM;
  private JobParallelism parallelism = JobParallelism.NORMAL;

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
  TabPanel workflowTabPanel;

  @UiField
  FlowPanel workflowList, workflowStoreList;

  @UiField
  FlowPanel workflowListPluginStatus;

  @UiField
  FlowPanel workflowListDescription;

  @UiField
  FlowPanel workflowListDescriptionCategories;

  @UiField
  FlowPanel workflowListTitle;

  @UiField
  FlowPanel workflowPanel;

  @UiField
  PluginOptionsPanel workflowOptions;

  @UiField
  Button buttonCreate;

  @UiField
  Button buttonObtainCommand;

  @UiField
  Button buttonCancel;

  @UiField
  FlowPanel jobPriorityRadioButtons;

  @UiField
  FlowPanel jobParallelismRadioButtons;

  @UiField(provided = true)
  RadioButton highPriorityRadioButton;

  @UiField(provided = true)
  RadioButton mediumPriorityRadioButton;

  @UiField(provided = true)
  RadioButton lowPriorityRadioButton;

  @UiField(provided = true)
  RadioButton normalParallelismRadioButton;

  @UiField(provided = true)
  RadioButton limitedParallelismRadioButton;

  public CreateSelectedJob(final List<PluginType> pluginType) {
    this.selected = LastSelectedItemsSingleton.getInstance().getSelectedItems();
    highPriorityRadioButton = new RadioButton("priority", HtmlSnippetUtils.getJobPriorityHtml(JobPriority.HIGH));
    mediumPriorityRadioButton = new RadioButton("priority", HtmlSnippetUtils.getJobPriorityHtml(JobPriority.MEDIUM));
    lowPriorityRadioButton = new RadioButton("priority", HtmlSnippetUtils.getJobPriorityHtml(JobPriority.LOW));
    normalParallelismRadioButton = new RadioButton("parallelism",
      HtmlSnippetUtils.getJobParallelismTypeHtml(JobParallelism.NORMAL));
    limitedParallelismRadioButton = new RadioButton("parallelism",
      HtmlSnippetUtils.getJobParallelismTypeHtml(JobParallelism.LIMITED));
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
    configureOrchestration();
    this.plugins = plugins;
    name.setText(messages.processNewDefaultName(new Date()));
    workflowOptions.setPlugins(plugins);
    configurePlugins(selected.getSelectedClass());
    workflowCategoryList.addStyleName("form-listbox-job");
  }

  private void configureOrchestration() {
    mediumPriorityRadioButton.setValue(true);
    normalParallelismRadioButton.setValue(true);

    highPriorityRadioButton.addClickHandler(e -> priority = JobPriority.HIGH);
    mediumPriorityRadioButton.addClickHandler(e -> priority = JobPriority.MEDIUM);
    lowPriorityRadioButton.addClickHandler(e -> priority = JobPriority.LOW);
    normalParallelismRadioButton.addClickHandler(e -> parallelism = JobParallelism.NORMAL);
    limitedParallelismRadioButton.addClickHandler(e -> parallelism = JobParallelism.LIMITED);
  }

  public abstract boolean updateObjectList();

  public void configurePlugins(final String selectedClass) {
    List<String> categoriesOnListBox = new ArrayList<>();
    workflowTabPanel.selectTab(0);

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
                    workflowStoreList.clear();
                    boolean noChecks = true;

                    if (plugins != null) {
                      PluginUtils.sortByName(plugins);
                      List<String> pluginsAdded = new ArrayList<>();

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
                                    || (isSelectedEmpty() && pluginInfo.hasObjectClass(listSelectedClass)))
                                  && !pluginsAdded.contains(pluginInfo.getId())) {
                                  Widget pluginItem = addPluginItemWidgetToWorkflowList(pluginInfo);

                                  if (pluginsAdded.isEmpty()) {
                                    CreateSelectedJob.this.selectedPlugin = lookupPlugin(pluginInfo.getId());
                                    pluginItem.addStyleName("plugin-list-item-selected");
                                  }

                                  pluginsAdded.add(pluginInfo.getId());
                                }

                              }
                            }

                            if (noChecks) {
                              if (!pluginInfo.getCategories().contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)
                                && ((!isSelectedEmpty() && pluginInfo.hasObjectClass(selectedClass))
                                  || (isSelectedEmpty() && pluginInfo.hasObjectClass(listSelectedClass)))) {
                                Widget pluginItem = addPluginItemWidgetToWorkflowList(pluginInfo);
                                if (pluginsAdded.isEmpty()) {
                                  CreateSelectedJob.this.selectedPlugin = lookupPlugin(pluginInfo.getId());
                                  pluginItem.addStyleName("plugin-list-item-selected");
                                }

                                pluginsAdded.add(pluginInfo.getId());
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

        for (int i = 0; i < workflowStoreList.getWidgetCount(); i++) {
          Widget panelWidget = workflowStoreList.getWidget(i);
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
    String labelContent = messages.pluginLabelWithVersion(pluginInfo.getName(), pluginInfo.getVersion());
    label.setText(labelContent);
    label.setTitle(labelContent);
    label.addStyleName(getWorkFlowListItemLabel(pluginInfo));

    panel.add(itemImage);
    panel.add(label);

    if (pluginInfo.isInstalled()) {
      workflowList.add(panel);
    } else {
      workflowStoreList.add(panel);
    }
    return panel;
  }

  private String getWorkFlowListItemLabel(PluginInfo pluginInfo) {
    if (!pluginInfo.isInstalled()) {
      return "plugin-list-item-label-disable";
    }
    if (!pluginInfo.isVerified()) {
      return "plugin-list-item-label-untrusted";
    }
    return "plugin-list-item-label";
  }

  protected void updateWorkflowOptions() {
    if (selectedPlugin == null) {
      workflowListDescription.clear();
      workflowListDescriptionCategories.clear();
      workflowListDescription.setVisible(false);
      workflowListDescriptionCategories.setVisible(false);
      workflowOptions.setPluginInfo(null);
    } else {
      buttonCreate.setEnabled(shouldEnableCreateButton());
      buildPluginHeader();
      buildPluginStatusPanel();

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

        workflowListDescriptionCategories.clear();
        for (String categoryTranslation : categoryTranslations) {
          BadgePanel categoryBadge = new BadgePanel();
          categoryBadge.setText(categoryTranslation);
          workflowListDescriptionCategories.add(categoryBadge);
        }

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

    // Remove store tab if there is no item on store
    if (workflowStoreList.getWidgetCount() == 0) {
      TabBar tabBar = workflowTabPanel.getTabBar();
      if (tabBar.getTabCount() > 1) {
        workflowTabPanel.remove(1);
      }
    }
  }

  private void buildPluginHeader() {
    workflowListTitle.clear();
    FlowPanel leftPanel = new FlowPanel();
    FlowPanel rightPanel = new FlowPanel();

    // PLUGIN NAME
    String pluginName = messages.pluginLabel(selectedPlugin.getName());
    name.setText(pluginName);
    Label pluginNameLabel = new Label(pluginName);
    pluginNameLabel.addStyleName("form-label h5");
    leftPanel.add(pluginNameLabel);

    MarketInfo marketInfo = selectedPlugin.getMarketInfo();
    // HOMEPAGE
    if (marketInfo != null && marketInfo.getHomepage() != null) {
      Button homepageButton = new Button(messages.pluginHomepageLabel());
      homepageButton.addStyleName("btn pluginWorkFlowListTitleButtons btn-home");
      homepageButton.addClickHandler(clickEvent -> Window.open(marketInfo.getHomepage(), "_blank", ""));
      rightPanel.add(homepageButton);
    }
    // LICENSE
    Button licenseButton = new Button(messages.pluginLicenseLabel());
    licenseButton.addStyleName("btn pluginWorkFlowListTitleButtons btn-stamp");
    if (selectedPlugin.hasLicenseFile()) {
      if (selectedPlugin.getCertificateInfo().getCertificateStatus()
        .equals(CertificateInfo.CertificateStatus.INTERNAL)) {
        licenseButton.addClickHandler(e -> Dialogs.showLicenseModal(messages.pluginLicenseLabel(),
          new HTMLWidgetWrapper(selectedPlugin.getLicenseFilePath(), RodaConstants.ResourcesTypes.INTERNAL)));
      } else {
        licenseButton.addClickHandler(e -> Dialogs.showLicenseModal(messages.pluginLicenseLabel(),
          new HTMLWidgetWrapper(selectedPlugin.getLicenseFilePath(), RodaConstants.ResourcesTypes.PLUGINS)));
      }
    } else if (marketInfo != null && marketInfo.getLicense() != null) {
      LicenseInfo license = marketInfo.getLicense();
      licenseButton.setText(license.getName());
      licenseButton.addClickHandler(clickEvent -> Window.open(license.getUrl(), "_blank", ""));
      rightPanel.add(licenseButton);
    }

    // DOCUMENTATION
    Button documentationButton = new Button(messages.pluginDocumentationLabel());
    documentationButton.addStyleName("btn pluginWorkFlowListTitleButtons btn-book");
    if (selectedPlugin.hasDocumentationFile()) {
      documentationButton.addClickHandler(clickEvent -> HistoryUtils.newHistory(Theme.RESOLVER,
        RodaConstants.ResourcesTypes.PLUGINS.toString(), selectedPlugin.getDocumentationFilePath()));
      rightPanel.add(documentationButton);
    }

    // VERSION
    BadgePanel version = new BadgePanel();
    version.setText(selectedPlugin.getVersion());
    if (selectedPlugin.getMarketInfo() != null
      && !selectedPlugin.getMarketInfo().getVersion().equals(selectedPlugin.getVersion())) {
      version.enableNotification(true);
      version.setTitle(messages.marketVersionLabel(selectedPlugin.getMarketInfo().getVersion()));
    }
    version.addStyleName("badge-for-version");
    rightPanel.add(version);
    rightPanel.addStyleName("pluginWorkflowListTitle");

    workflowListTitle.addStyleName("pluginWorkflowListTitle");
    workflowListTitle.add(leftPanel);
    workflowListTitle.add(rightPanel);
  }

  private void buildPluginStatusPanel() {
    workflowListPluginStatus.clear();
    FlowPanel statusPanel = new FlowPanel();
    statusPanel.addStyleName("plugin-status-panel");
    BadgePanel badgePanel = new BadgePanel();
    HTML statusMessage = new HTML();
    statusMessage.addStyleName("plugin-status-message");

    if (selectedPlugin.isInstalled()) {
      CertificateInfo certificateInfo = selectedPlugin.getCertificateInfo();
      CertificateInfo.CertificateStatus certificateStatus = certificateInfo.getCertificateStatus();
      badgePanel.setText(messages.pluginLicenseStatus(certificateStatus));
      if (CertificateInfo.CertificateStatus.INTERNAL.equals(certificateStatus)) {
        badgePanel.setIcon("fas fa-cog");
        badgePanel.addStyleName("badge-panel-success");
        statusMessage.setText(messages.pluginInternalMessage());
      } else if (CertificateInfo.CertificateStatus.VERIFIED.equals(certificateStatus)) {
        badgePanel.setIcon("fas fa-shield-alt");
        badgePanel.addStyleName("badge-panel-success");
        CertificateInfo.Certificate certificate = certificateInfo.getCertificates().iterator().next();
        String issuer = certificate.getOrganizationName(certificate.getIssuerDN());
        String subject = certificate.getOrganizationName(certificate.getSubjectDN());
        statusMessage.setHTML(messages.pluginTrustedMessage(issuer, subject));
      } else {
        badgePanel.setIcon(HtmlSnippetUtils.getStackIcon("fas fa-shield-alt", "fas fa-slash"));
        badgePanel.addStyleName("badge-panel-danger");
        statusMessage.setHTML(messages.pluginUntrustedMessage());
      }
      statusPanel.add(badgePanel);
      statusPanel.add(statusMessage);
    } else {
      badgePanel.setIcon(HtmlSnippetUtils.getStackIcon("far fa-circle", "fas fa-slash"));
      badgePanel.addStyleName("badge-panel-dark");
      badgePanel.setText(messages.pluginNotInstalledLabel());
      statusMessage.setText(messages.pluginNotInstalledMessage());

      MarketInfo marketInfo = selectedPlugin.getMarketInfo();
      Button installBtn = new Button(messages.marketStoreInstallLabel());
      installBtn.addStyleName("btn btn-download plugin-install-btn");
      if (marketInfo != null && marketInfo.getLinkToQuote().get("en") != null) {
        installBtn.addClickHandler(clickEvent -> Window.open(marketInfo.getLinkToQuote().get("en") + URL.encodeQueryString(marketInfo.getName()), "_blank", ""));
      } else {
        installBtn.addClickHandler(clickEvent -> Window.open(RodaConstants.DEFAULT_MARKET_SUPPORT_URL, "_blank", ""));
      }
      installBtn.addStyleName("btn plugin-status-btn");

      statusPanel.add(badgePanel);
      statusPanel.add(statusMessage);
      statusPanel.add(installBtn);
    }
    workflowListPluginStatus.add(statusPanel);
  }

  private boolean shouldEnableCreateButton() {
    // Enable it for development or for verified plugins
    boolean optIn = ConfigurationManager.getBoolean(false, RodaConstants.PLUGINS_CERTIFICATE_OPT_IN_PROPERTY);
    return (optIn && selectedPlugin.isInstalled()) || selectedPlugin.isVerified() && selectedPlugin.isInstalled();
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

  @UiHandler("buttonObtainCommand")
  public abstract void buttonObtainCommandHandler(ClickEvent e);

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

  public JobPriority getJobPriority() {
    return this.priority;
  }

  public JobParallelism getJobParallelism() {
    return this.parallelism;
  }

}
