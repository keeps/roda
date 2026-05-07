package org.roda.wui.client.process.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoList;
import org.roda.wui.client.common.BadgePanel;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class MarketplaceTab extends Composite {
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  SimplePanel content;

  private FlowPanel pluginDetailsPanel;

  public MarketplaceTab(PluginInfoList pluginInfoList) {
    initWidget(uiBinder.createAndBindUi(this));

    Set<String> uniqueCategories = pluginInfoList.getPluginInfoList().stream()
      .filter(pluginInfo -> !pluginInfo.isInstalled()).map(PluginInfo::getCategories).flatMap(List::stream)
      .collect(Collectors.toCollection(TreeSet::new));

    initLayout(pluginInfoList.getPluginInfoList(), uniqueCategories);
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

    for (String category : categories) {
      comboBox.addItem(messages.showPluginCategories(category), category);
    }

    // Attach the change handler to call our dedicated build method
    comboBox.addChangeHandler(event -> buildPluginDetailsArea(comboBox.getSelectedValue(), pluginInfos));

    // Fire for the first element
    if (comboBox.getItemCount() > 0) {
      comboBox.setSelectedIndex(0);
      buildPluginDetailsArea(comboBox.getSelectedValue(), pluginInfos);
    }

    leftGroup.add(leftLabel);
    leftGroup.add(comboBox);

    mainContainer.add(leftGroup);

    return mainContainer;
  }

  private void buildPluginDetailsArea(String selectedCategory, List<PluginInfo> pluginInfos) {
    // Always clear the panel first when a new category is selected
    pluginDetailsPanel.clear();

    if (selectedCategory == null) {
      return;
    }

    List<PluginInfo> filteredByCategory = pluginInfos.stream()
      .filter(pluginInfo -> pluginInfo.getCategories().contains(selectedCategory) && !pluginInfo.isInstalled())
      .collect(Collectors.toList());

    filteredByCategory.sort(Comparator.comparing(PluginInfo::getName, String.CASE_INSENSITIVE_ORDER));

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
    for (PluginInfo info : filteredByCategory) {
      Label pluginItem = new Label(info.getName());
      pluginItem.addStyleName("plugin-list-item-new");

      String safeCategoryName = selectedCategory.toLowerCase().replaceAll("\\s+", "-");
      pluginItem.addStyleName("plugin-list-item-new-" + safeCategoryName);

      // USE HELPER HERE
      pluginItem.addClickHandler(event -> selectPluginItem(pluginItem, info, leftColumn, rightColumn));

      leftColumn.add(pluginItem);
    }

    // Automatically select the first plugin if the list is not empty
    if (!filteredByCategory.isEmpty()) {
      Widget firstItem = leftColumn.getWidget(0);

      // USE HELPER HERE
      selectPluginItem(firstItem, filteredByCategory.get(0), leftColumn, rightColumn);
    } else {
      rightColumn.add(new Label("not found"));
    }
  }

  private void selectPluginItem(Widget itemWidget, PluginInfo info, FlowPanel leftColumn, FlowPanel rightColumn) {
    for (int i = 0; i < leftColumn.getWidgetCount(); i++) {
      leftColumn.getWidget(i).removeStyleName("selected-plugin");
    }

    itemWidget.addStyleName("selected-plugin");
    showPluginDetails(info, rightColumn);
  }

  private void showPluginDetails(PluginInfo info, FlowPanel rightColumn) {
    // 1. Clear previous details
    rightColumn.clear();

    if (info == null) {
      return;
    }

    rightColumn.add(buildPluginDetailsHeaderPanel(info));
    rightColumn.add(buildPluginDescriptionPanel(info.getDescription()));
  }

  private FlowPanel buildPluginDescriptionPanel(String description) {
    FlowPanel mainContainer = new FlowPanel();
    mainContainer.addStyleName("description-container mb-16");

    // Protect against null descriptions
    String safeDescription = description != null ? description : "";

    // The text paragraph
    HTMLPanel textPanel = new HTMLPanel("p", safeDescription);
    textPanel.addStyleName("description-text");
    mainContainer.add(textPanel);

    return mainContainer;
  }

  private FlowPanel buildPluginDetailsHeaderPanel(PluginInfo info) {
    FlowPanel mainContainer = new FlowPanel();
    mainContainer.addStyleName("mb-16");

    FlowPanel leftGroup = new FlowPanel();
    Label titleLabel = new Label(info.getName());
    titleLabel.addStyleName("plugin-header-title");
    leftGroup.add(titleLabel);

    FlowPanel rightGroup = new FlowPanel();

    Anchor pluginHomePage = new Anchor();
    pluginHomePage.setHTML(messages.pluginHomepageLabel() + " <i class='fas fa-link' />");
    pluginHomePage.addStyleName("resource-download-link");
    pluginHomePage.addClickHandler(clickEvent -> Window.open(info.getMarketInfo().getHomepage(), "_blank", ""));
    rightGroup.add(pluginHomePage);

    Anchor licensePage = new Anchor();
    licensePage.setHTML(messages.pluginLicenseLabel() + " <i class='fas fa-balance-scale' />");
    licensePage.addStyleName("resource-download-link");
    licensePage.addClickHandler(clickEvent -> Window.open(info.getMarketInfo().getLicense().getUrl(), "_blank", ""));
    rightGroup.add(licensePage);

    mainContainer.add(buildSplitHeaderRow(leftGroup, rightGroup));
    mainContainer.add(buildPluginVersionAndOtherInfo(info));

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

  private FlowPanel buildSplitHeaderRow(FlowPanel leftGroup, FlowPanel rightGroup) {
    FlowPanel headerPanel = new FlowPanel();
    headerPanel.addStyleName("plugin-header-container mb-8");

    leftGroup.addStyleName("header-left-group");
    rightGroup.addStyleName("header-right-group");

    headerPanel.add(leftGroup);
    headerPanel.add(rightGroup);

    return headerPanel;
  }

  interface MyUiBinder extends UiBinder<Widget, MarketplaceTab> {
  }
}
