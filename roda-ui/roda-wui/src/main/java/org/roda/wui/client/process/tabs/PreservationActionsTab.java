package org.roda.wui.client.process.tabs;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gwt.user.client.ui.TextBox;
import org.roda.core.data.v2.jobs.Certificate;
import org.roda.core.data.v2.jobs.CertificateInfo;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoList;
import org.roda.wui.client.common.BadgePanel;
import org.roda.wui.client.ingest.process.PluginOptionsPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class PreservationActionsTab extends Composite {
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final PluginOptionsPanel optionsPanel = new PluginOptionsPanel();
  @UiField
  SimplePanel content;
  private PluginInfo selectedPlugin;
  private FlowPanel pluginDetailsPanel;
  private Consumer<PluginInfo> onPluginSelected;
  private Runnable onStartJobCallback;
  private Runnable onCancelJobCallback;

  public PreservationActionsTab(PluginInfoList pluginInfoList, String selectedClass) {
    initWidget(uiBinder.createAndBindUi(this));

    Set<String> uniqueCategories;
    List<PluginInfo> filteredPlugins;

    if (selectedClass != null) {
      uniqueCategories = pluginInfoList.getPluginInfoList().stream().filter(PluginInfo::isInstalled)
        .filter(pluginInfo -> pluginInfo.hasObjectClass(selectedClass)).map(PluginInfo::getCategories)
        .flatMap(List::stream).collect(Collectors.toCollection(TreeSet::new));
      filteredPlugins = pluginInfoList.getPluginInfoList().stream().filter(PluginInfo::isInstalled)
        .filter(pluginInfo -> pluginInfo.hasObjectClass(selectedClass)).collect(Collectors.toList());
    } else {
      uniqueCategories = pluginInfoList.getPluginInfoList().stream().filter(PluginInfo::isInstalled)
        .map(PluginInfo::getCategories).flatMap(List::stream).collect(Collectors.toCollection(TreeSet::new));
      filteredPlugins = pluginInfoList.getPluginInfoList().stream().filter(PluginInfo::isInstalled)
        .collect(Collectors.toList());
    }

    initLayout(filteredPlugins, uniqueCategories);
  }

  public PluginInfo getSelectedPlugin() {
    return selectedPlugin;
  }

  public Map<String, String> getPluginParameters() {
    return optionsPanel.getValue();
  }

  public void setOnPluginSelected(Consumer<PluginInfo> onPluginSelected) {
    this.onPluginSelected = onPluginSelected;
  }

  public void setOnStartJobCallback(Runnable onStartJobCallback) {
    this.onStartJobCallback = onStartJobCallback;
  }

  public void setOnCancelJobCallback(Runnable onCancelJobCallback) {
    this.onCancelJobCallback = onCancelJobCallback;
  }

  /**
   * Helper method to assemble the two distinct areas into the main view
   */
  private void initLayout(List<PluginInfo> pluginInfos, Set<String> categories) {
    FlowPanel wrapperPanel = new FlowPanel();
    pluginDetailsPanel = new FlowPanel();

    // Add the top Search/Filter area
    wrapperPanel.add(buildSearchAndFilterArea(pluginInfos, categories));

    // Add the bottom Details area
    wrapperPanel.add(pluginDetailsPanel);

    content.setWidget(wrapperPanel);
  }

  private FlowPanel buildSearchAndFilterArea(List<PluginInfo> pluginInfos, Set<String> categories) {
    FlowPanel mainContainer = new FlowPanel();
    mainContainer.addStyleName("search-filter-container mb-16");

    // --- Left Side ---
    FlowPanel leftGroup = new FlowPanel();
    leftGroup.addStyleName("input-group");

    Label leftLabel = new Label(messages.createJobCategorySelect());
    leftLabel.addStyleName("label");

    ListBox comboBox = new ListBox();
    comboBox.addStyleName("form-listbox");

    comboBox.addItem(messages.showPluginCategories("All"), "all");
    for (String category : categories) {
      comboBox.addItem(messages.showPluginCategories(category), category);
    }

    leftGroup.add(leftLabel);
    leftGroup.add(comboBox);

    // --- Right Side ---
    FlowPanel rightGroup = new FlowPanel();
    rightGroup.addStyleName("input-group");
    Label rightLabel = new Label(messages.search());
    rightLabel.addStyleName("label");
    TextBox textBox = new TextBox();
    textBox.addStyleName("form-textbox");
    rightGroup.add(rightLabel);
    rightGroup.add(textBox);

    // Attach handlers for BOTH inputs so they work together
    comboBox.addChangeHandler(event -> buildPluginDetailsArea(comboBox.getSelectedValue(), textBox.getText(), pluginInfos));

    // Use KeyUpHandler for real-time filtering as the user types
    textBox.addKeyUpHandler(event -> buildPluginDetailsArea(comboBox.getSelectedValue(), textBox.getText(), pluginInfos));

    // Fire for the first element
    if (comboBox.getItemCount() > 0) {
      comboBox.setSelectedIndex(0);
      buildPluginDetailsArea(comboBox.getSelectedValue(), textBox.getText(), pluginInfos);
    }

    mainContainer.add(leftGroup);
    mainContainer.add(rightGroup);

    return mainContainer;
  }

  /**
   * Dedicated method to build out the complex plugin details UI
   */
  private void buildPluginDetailsArea(String selectedCategory, String searchText, List<PluginInfo> pluginInfos) {
    // Always clear the panel first when a new category is selected or text changes
    pluginDetailsPanel.clear();

    if (selectedCategory == null) {
      return;
    }

    // 1. Apply Category Filter
    List<PluginInfo> filteredPlugins;
    if (selectedCategory.equals("all")) {
      filteredPlugins = pluginInfos;
    } else {
      filteredPlugins = pluginInfos.stream()
              .filter(pluginInfo -> pluginInfo.getCategories().contains(selectedCategory) && pluginInfo.isInstalled())
              .collect(Collectors.toList());
    }

    // 2. Apply Text Search Filter (Case-insensitive)
    if (searchText != null && !searchText.trim().isEmpty()) {
      final String lowerCaseSearch = searchText.trim().toLowerCase();
      filteredPlugins = filteredPlugins.stream()
              .filter(info -> info.getName().toLowerCase().contains(lowerCaseSearch) ||
                      (info.getDescription() != null && info.getDescription().toLowerCase().contains(lowerCaseSearch)))
              .collect(Collectors.toList());
    }

    filteredPlugins.sort(Comparator.comparing(PluginInfo::getName, String.CASE_INSENSITIVE_ORDER));

    FlowPanel splitContainer = new FlowPanel();
    splitContainer.addStyleName("split-panel-container");

    // Create the first equal-width column (The List)
    FlowPanel leftColumn = new FlowPanel();
    leftColumn.addStyleName("split-panel-column plugin-list-column");

    // Create the second equal-width column (The Details)
    FlowPanel rightColumn = new FlowPanel();
    rightColumn.addStyleName("split-panel-column right-side");

    // Add both to the parent
    splitContainer.add(leftColumn);
    splitContainer.add(rightColumn);

    pluginDetailsPanel.add(splitContainer);

    // Populate the left column with clickable items
    for (PluginInfo info : filteredPlugins) {
      Label pluginItem = new Label(info.getName());
      pluginItem.addStyleName("plugin-list-item-new");

      String safeCategoryName = info.getCategories().get(0).toLowerCase().replaceAll("\\s+", "-");
      pluginItem.addStyleName("plugin-list-item-new-" + safeCategoryName);

      pluginItem.addClickHandler(event -> selectPluginItem(pluginItem, info, leftColumn, rightColumn));

      leftColumn.add(pluginItem);
    }

    // Automatically select the first plugin if the list is not empty
    if (!filteredPlugins.isEmpty()) {
      Widget firstItem = leftColumn.getWidget(0);
      selectPluginItem(firstItem, filteredPlugins.get(0), leftColumn, rightColumn);
    } else {
      this.selectedPlugin = null;
      Label label = new Label(messages.noPluginsFoundMatchingYourCriteria());
      rightColumn.add(label);
    }
  }

  /**
   * Helper method to populate the right column with specific plugin details
   */
  private void showPluginDetails(PluginInfo info, FlowPanel rightColumn) {
    // 1. Clear previous details
    rightColumn.clear();

    if (info == null) {
      return;
    }

    // You can add as many details as you want here
    rightColumn.add(buildPluginDetailsHeaderPanel(info));
    rightColumn.add(buildPluginDescriptionPanel(info.getDescription()));
    if (info.getParameters() != null && !info.getParameters().isEmpty()) {
      rightColumn.add(buildPluginParametersPanel(info));
    }

    FlowPanel buttonsContainer = new FlowPanel();
    buttonsContainer.addStyleName("buttons-container");
    rightColumn.add(buttonsContainer);

    Button button = new Button();
    button.addStyleName("btn btn-primary btn-play");
    if (info.getCertificateInfo().getCertificateStatus().equals(CertificateInfo.CertificateStatus.NOT_VERIFIED)) {
      button.setEnabled(false);
      button.setTitle(messages.pluginUntrustedMessage());
    }
    button.setText(messages.startButton());

    button.addClickHandler(event -> {
      if (onStartJobCallback != null) {
        onStartJobCallback.run();
      }
    });

    buttonsContainer.add(button);

    Button buttonCancel = new Button();
    buttonCancel.addStyleName("btn btn-link btn-cancel");
    buttonCancel.setText(messages.cancelButton());
    buttonCancel.addClickHandler(event -> {
      if (onCancelJobCallback != null) {
        onCancelJobCallback.run();
      }
    });
    buttonsContainer.add(buttonCancel);
  }

  private FlowPanel buildPluginParametersPanel(PluginInfo info) {
    FlowPanel mainContainer = new FlowPanel();
    mainContainer.addStyleName("plugin-parameters mb-16");

    optionsPanel.setPluginInfo(info);
    mainContainer.add(optionsPanel);

    return mainContainer;
  }

  private FlowPanel buildPluginDescriptionPanel(String description) {
    FlowPanel mainContainer = new FlowPanel();
    mainContainer.addStyleName("description-container mb-16");

    // Protect against null descriptions
    String safeDescription = description != null ? description : "";

    // The text paragraph
    HTMLPanel textPanel = new HTMLPanel("p", safeDescription);
    textPanel.addStyleName("description-text clamped");

    // The button (using a Label styled as a link)
    Label showMoreBtn = new Label(messages.showMore());
    showMoreBtn.addStyleName("show-more-btn");
    showMoreBtn.setVisible(false); // Hidden by default

    // Toggle logic for the button
    showMoreBtn.addClickHandler(event -> {
      if (textPanel.getStyleName().contains("clamped")) {
        textPanel.removeStyleName("clamped");
        showMoreBtn.setText(messages.showLess());
      } else {
        textPanel.addStyleName("clamped");
        showMoreBtn.setText("Show more");
      }
    });

    // Check if the text exceeds 2 lines ONLY after it's attached to the DOM
    textPanel.addAttachHandler(event -> {
      if (event.isAttached()) {
        // If the real scrollable height is greater than the clamped visible height,
        // it means the text was cut off, so we need to show the button.
        int scrollHeight = textPanel.getElement().getScrollHeight();
        int clientHeight = textPanel.getElement().getClientHeight();

        if (scrollHeight > clientHeight) {
          showMoreBtn.setVisible(true);
        }
      }
    });

    mainContainer.add(textPanel);
    mainContainer.add(showMoreBtn);

    return mainContainer;
  }

  private FlowPanel buildPluginVersionAndOtherInfo(PluginInfo info) {
    FlowPanel mainContainer = new FlowPanel();

    FlowPanel leftGroup = new FlowPanel();
    Label titleLabel = new Label(info.getVersion());
    titleLabel.addStyleName("plugin-header-subtitle");
    leftGroup.add(titleLabel);

    FlowPanel rightGroup = new FlowPanel();
    for (String category : info.getCategories()) {
      BadgePanel badgePanel = new BadgePanel();
      badgePanel.setIconClass("badge-icon-" + category);
      badgePanel.addStyleName("badge-panel-secondary");
      badgePanel.setText(messages.showPluginCategories(category));
      rightGroup.add(badgePanel);
    }

    mainContainer.add(buildSplitHeaderRow(leftGroup, rightGroup));

    return mainContainer;
  }

  private FlowPanel buildPluginDetailsHeaderPanel(PluginInfo info) {
    FlowPanel mainContainer = new FlowPanel();
    mainContainer.addStyleName("mb-16");

    FlowPanel leftGroup = new FlowPanel();
    Label titleLabel = new Label(info.getName());
    titleLabel.addStyleName("plugin-header-title");
    leftGroup.add(titleLabel);
    leftGroup.add(buildPluginValidationPanel(info.getCertificateInfo()));

    FlowPanel rightGroup = new FlowPanel();
    if (!CertificateInfo.CertificateStatus.INTERNAL.equals(info.getCertificateInfo().getCertificateStatus())) {
      Anchor pluginHomePage = new Anchor();
      pluginHomePage.setHTML(messages.pluginHomepageLabel() + " <i class='fas fa-link' />");
      pluginHomePage.addStyleName("resource-download-link");
      pluginHomePage.addClickHandler(clickEvent -> Window.open(info.getMarketInfo().getHomepage(), "_blank", ""));
      rightGroup.add(pluginHomePage);

      Anchor licensePage = new Anchor();
      licensePage.setHTML(messages.pluginLicenseLabel() + " <i class='fas fa-balance-scale' />");
      licensePage.addStyleName("resource-download-link");
      licensePage.addClickHandler(clickEvent -> Window.open(info.getMarketInfo().getHomepage(), "_blank", ""));
      rightGroup.add(licensePage);
    } else if (CertificateInfo.CertificateStatus.INTERNAL.equals(info.getCertificateInfo().getCertificateStatus())) {
      Anchor licensePage = new Anchor();
      licensePage.setHTML("LGPL-3.0 <i class='fas fa-balance-scale' />");
      licensePage.addStyleName("resource-download-link");
      licensePage.addClickHandler(
        clickEvent -> Window.open("https://github.com/keeps/roda?tab=LGPL-3.0-1-ov-file#readme", "_blank", ""));
      rightGroup.add(licensePage);
    }

    mainContainer.add(buildSplitHeaderRow(leftGroup, rightGroup));
    mainContainer.add(buildPluginVersionAndOtherInfo(info));

    return mainContainer;
  }

  private BadgePanel buildPluginValidationPanel(CertificateInfo info) {
    BadgePanel badgePanel = new BadgePanel();

    CertificateInfo.CertificateStatus certificateStatus = info.getCertificateStatus();
    badgePanel.setText(messages.pluginLicenseStatus(certificateStatus));
    if (CertificateInfo.CertificateStatus.INTERNAL.equals(certificateStatus)) {
      badgePanel.setIconClass("badge-icon-plugin-verified");
      badgePanel.addStyleName("badge-panel-success");
    } else if (CertificateInfo.CertificateStatus.VERIFIED.equals(certificateStatus)) {
      badgePanel.setIconClass("badge-icon-plugin-verified");
      badgePanel.addStyleName("badge-panel-success");
      Certificate certificate = info.getCertificates().iterator().next();
      String issuer = certificate.getOrganizationName(certificate.getIssuerDN());
      String subject = certificate.getOrganizationName(certificate.getSubjectDN());
      // badgePanel.setTitle(messages.pluginTrustedMessage(issuer,
      // subject).asString());
    } else {
      // badgePanel.setIcon(HtmlSnippetUtils.getStackIcon("fas fa-shield-alt", "fas
      // fa-slash"));
      badgePanel.addStyleName("badge-panel-danger");
      badgePanel.setIconClass("badge-icon-invalid");
      // badgePanel.setTitle(messages.pluginUntrustedMessage().asString());
    }

    return badgePanel;
  }

  private void selectPluginItem(Widget itemWidget, PluginInfo info, FlowPanel leftColumn, FlowPanel rightColumn) {
    for (int i = 0; i < leftColumn.getWidgetCount(); i++) {
      leftColumn.getWidget(i).removeStyleName("selected-plugin");
    }

    itemWidget.addStyleName("selected-plugin");
    this.selectedPlugin = info;
    showPluginDetails(info, rightColumn);

    if (onPluginSelected != null) {
      onPluginSelected.accept(info);
    }
  }

  private FlowPanel buildSplitHeaderRow(FlowPanel leftGroup, FlowPanel rightGroup) {
    FlowPanel headerPanel = new FlowPanel();
    headerPanel.addStyleName("plugin-header-container mb-8");

    leftGroup.addStyleName("header-left-group");
    rightGroup.addStyleName("header-right-group");

    headerPanel.add(leftGroup);
    headerPanel.add(rightGroup);

    return headerPanel;
  }

  interface MyUiBinder extends UiBinder<Widget, PreservationActionsTab> {
  }
}
