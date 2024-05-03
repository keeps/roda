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
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.CertificateInfo;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.LicenseInfo;
import org.roda.core.data.v2.jobs.MarketInfo;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.BadgePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.lists.utils.ListFactory;
import org.roda.wui.client.common.search.SearchWrapper;
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
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.user.client.ui.ListBox;
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
public class CreateDefaultJob extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        CreateDefaultJob createDefaultJob = new CreateDefaultJob();
        callback.onSuccess(createDefaultJob);
      } else {
        HistoryUtils.newHistory(CreateDefaultJob.RESOLVER);
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
      return "create_job";
    }
  };

  public interface MyUiBinder extends UiBinder<Widget, CreateDefaultJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @SuppressWarnings("rawtypes")
  private SearchWrapper search = null;
  private List<PluginInfo> plugins = null;
  private PluginInfo selectedPlugin = null;
  private boolean isListEmpty = true;
  private JobPriority priority = JobPriority.MEDIUM;
  private JobParallelism parallelism = JobParallelism.NORMAL;
  private static List<PluginType> pluginTypes = PluginUtils.getPluginTypesWithoutIngest();

  @UiField
  TextBox name;

  @UiField
  FlowPanel workflowListTitle;

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

  public CreateDefaultJob() {
    highPriorityRadioButton = new RadioButton("priority", HtmlSnippetUtils.getJobPriorityHtml(JobPriority.HIGH, false));
    mediumPriorityRadioButton = new RadioButton("priority",
      HtmlSnippetUtils.getJobPriorityHtml(JobPriority.MEDIUM, false));
    lowPriorityRadioButton = new RadioButton("priority", HtmlSnippetUtils.getJobPriorityHtml(JobPriority.LOW, false));
    normalParallelismRadioButton = new RadioButton("parallelism",
      HtmlSnippetUtils.getJobParallelismTypeHtml(JobParallelism.NORMAL, false));
    limitedParallelismRadioButton = new RadioButton("parallelism",
      HtmlSnippetUtils.getJobParallelismTypeHtml(JobParallelism.LIMITED, false));

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
    configurePlugins();
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

  public void configurePlugins() {
    List<String> categoriesOnListBox = new ArrayList<>();
    workflowTabPanel.selectTab(0);

    if (plugins != null) {
      PluginUtils.sortByName(plugins);

      for (int p = 0; p < plugins.size(); p++) {
        PluginInfo pluginInfo = plugins.get(p);

        if (pluginInfo != null) {
          List<String> pluginCategories = pluginInfo.getCategories();

          if (pluginCategories != null && !pluginCategories.contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)) {
            for (String category : pluginCategories) {
              if (!categoriesOnListBox.contains(category)) {

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

                      for (int p = 0; p < plugins.size(); p++) {
                        PluginInfo pluginInfo = plugins.get(p);
                        if (pluginInfo != null) {
                          List<String> categories = pluginInfo.getCategories();

                          if (categories != null) {
                            for (int i = 0; i < workflowCategoryList.getWidgetCount(); i++) {
                              CheckBox checkbox = (CheckBox) workflowCategoryList.getWidget(i);

                              if (checkbox.getValue().booleanValue()) {
                                noChecks = false;

                                if (categories.contains(checkbox.getName())
                                  && !categories.contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)
                                  && !pluginsAdded.contains(pluginInfo.getId())) {
                                  Widget pluginItem = addPluginItemWidgetToWorkflowList(pluginInfo);
                                  if (pluginsAdded.isEmpty()) {
                                    CreateDefaultJob.this.selectedPlugin = lookupPlugin(pluginInfo.getId());
                                    pluginItem.addStyleName("plugin-list-item-selected");
                                  }
                                  pluginsAdded.add(pluginInfo.getId());
                                }
                              }
                            }

                            if (noChecks) {
                              if (!pluginInfo.getCategories().contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)) {
                                Widget pluginItem = addPluginItemWidgetToWorkflowList(pluginInfo);
                                if (p == 0) {
                                  CreateDefaultJob.this.selectedPlugin = lookupPlugin(pluginInfo.getId());
                                  pluginItem.addStyleName("plugin-list-item-selected");
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

            if (!pluginCategories.contains(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE)) {
              Widget pluginItem = addPluginItemWidgetToWorkflowList(pluginInfo);
              if (p == 0) {
                CreateDefaultJob.this.selectedPlugin = lookupPlugin(pluginInfo.getId());
                pluginItem.addStyleName("plugin-list-item-selected");
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
          CreateDefaultJob.this.selectedPlugin = lookupPlugin(selectedPluginId);
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
    isListEmpty = true;
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
      } else {
        workflowPanel.setVisible(true);
        workflowOptions.setPluginInfo(selectedPlugin);
      }

      targetList.clear();
      List<String> rodaClasses = getPluginNames(selectedPlugin.getObjectClasses());
      for (String objectClass : rodaClasses) {
        targetList.addItem(messages.allOfAObject(objectClass), objectClass);
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
      rightPanel.add(licenseButton);
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
    version.setTitle(selectedPlugin.getVersion());
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
    statusPanel.setTitle(statusMessage.getText());
    workflowListPluginStatus.add(statusPanel);
  }

  private boolean shouldEnableCreateButton() {
    // Enable it for development or for verified plugins
    boolean optIn = ConfigurationManager.getBoolean(false, RodaConstants.PLUGINS_CERTIFICATE_OPT_IN_PROPERTY);
    return (optIn && selectedPlugin.isInstalled()) || selectedPlugin.isVerified() && selectedPlugin.isInstalled();
  }

  private List<String> getPluginNames(Set<String> objectClasses) {
    List<String> objectList = new ArrayList<>();
    for (String objectClass : objectClasses) {
      if (IndexedAIP.class.getName().equals(objectClass)) {
        objectList = addIfNotExists(objectList, AIP.class.getName());
      } else if (IndexedRepresentation.class.getName().equals(objectClass)) {
        objectList = addIfNotExists(objectList, Representation.class.getName());
      } else if (IndexedFile.class.getName().equals(objectClass)) {
        objectList = addIfNotExists(objectList, File.class.getName());
      } else if (IndexedRisk.class.getName().equals(objectClass)) {
        objectList = addIfNotExists(objectList, Risk.class.getName());
      } else if (IndexedDIP.class.getName().equals(objectClass)) {
        objectList = addIfNotExists(objectList, DIP.class.getName());
      } else if (IndexedReport.class.getName().equals(objectClass)) {
        objectList = addIfNotExists(objectList, Report.class.getName());
      } else {
        objectList = addIfNotExists(objectList, objectClass);
      }
    }
    return objectList;
  }

  private List<String> addIfNotExists(List<String> objectList, String value) {
    if (!objectList.contains(value)) {
      objectList.add(value);
    }
    return objectList;
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void defineTargetInformation(String objectClassName) {
    ListFactory listFactory = new ListFactory();
    isListEmpty = true;

    ListBuilder<? extends IsIndexed> listBuilder = listFactory.getListBuilder("CreateDefaultJob", objectClassName, "",
      new Filter(new AllFilterParameter()), 10, 50);

    if (listBuilder != null) {
      listBuilder.getOptions().addStyleName("searchResults").addCheckboxSelectionListener(
        (CheckboxSelectionListener) selected -> isListEmpty = ClientSelectedItemsUtils.isEmpty(selected));

      search = new SearchWrapper(false).createListAndSearchPanel(listBuilder);

      targetListPanel.add(search);
      targetListPanel.setVisible(true);
    } else {
      targetListPanel.setVisible(false);
    }

  }

  @SuppressWarnings("rawtypes")
  @UiHandler("buttonCreate")
  public void buttonCreateHandler(ClickEvent e) {
    buttonCreate.setEnabled(false);
    String jobName = getName().getText();
    SelectedItems selected = search.getSelectedItemsInCurrentList();

    if (org.roda.core.data.v2.Void.class.getName().equals(targetList.getSelectedValue())) {
      selected = new SelectedItemsNone();
    } else if (isListEmpty) {
      selected = SelectedItemsAll.create(targetList.getSelectedValue());
    }

    BrowserService.Util.getInstance().createProcess(jobName, priority, parallelism, selected,
      getSelectedPlugin().getId(), getWorkflowOptions().getValue(), selected.getSelectedClass(),
      new AsyncCallback<Job>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(messages.dialogFailure(), caught.getMessage());
          buttonCreate.setEnabled(true);
        }

        @Override
        public void onSuccess(Job result) {
          Toast.showInfo(messages.dialogDone(), messages.processCreated());
          HistoryUtils.newHistory(ActionProcess.RESOLVER);
        }
      });
  }

  @SuppressWarnings("rawtypes")
  @UiHandler("buttonObtainCommand")
  public void buttonObtainCommandHandler(ClickEvent e) {
    String jobName = getName().getText();
    SelectedItems selected = search.getSelectedItemsInCurrentList();

    if (org.roda.core.data.v2.Void.class.getName().equals(targetList.getSelectedValue())) {
      selected = new SelectedItemsNone();
    } else if (isListEmpty) {
      selected = SelectedItemsAll.create(targetList.getSelectedValue());
    }

    BrowserService.Util.getInstance().createProcessJson(jobName, priority, parallelism, selected,
      getSelectedPlugin().getId(), getWorkflowOptions().getValue(), selected.getSelectedClass(),
      new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(messages.dialogFailure(), caught.getMessage());
        }

        @Override
        public void onSuccess(String result) {
          Dialogs.showInformationDialog(messages.createJobCurlCommand(), result, messages.closeButton(), true);
        }
      });
  }

  @UiHandler("buttonCancel")
  public void cancel(ClickEvent e) {
    HistoryUtils.newHistory(ActionProcess.RESOLVER);
  }

  public PluginInfo getSelectedPlugin() {
    return selectedPlugin;
  }

  public void setSelectedPlugin(PluginInfo selectedPlugin) {
    this.selectedPlugin = selectedPlugin;
  }

  public TextBox getName() {
    return this.name;
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
