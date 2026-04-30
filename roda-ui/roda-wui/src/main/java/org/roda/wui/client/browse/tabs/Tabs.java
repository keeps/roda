/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.tabs;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.storage.client.Storage;

import config.i18n.client.ClientMessages;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class Tabs extends Composite {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Tabs.MyUiBinder uiBinder = GWT.create(Tabs.MyUiBinder.class);
  private final Map<Widget, TabContentBuilder> tabs;
  @UiField
  FlowPanel tabButtons;
  @UiField
  SimplePanel tabContentWrapper;
  private Widget selectedTab;
  private TabContentBuilder defaultContent;
  private String storageKey = null;

  public Tabs() {
    initWidget(uiBinder.createAndBindUi(this));

    tabs = new HashMap<>();
    defaultContent = new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return new FlowPanel();
      }
    };
    selectTab(null);
  }

  public void setStorageKey(String storageKey) {
    this.storageKey = storageKey;

    // Immediately attempt to restore the saved tab index
    Storage sessionStorage = Storage.getSessionStorageIfSupported();
    if (sessionStorage != null) {
      String savedIndexStr = sessionStorage.getItem(storageKey);
      if (savedIndexStr != null) {
        try {
          int activeIndex = Integer.parseInt(savedIndexStr);
          selectTabByIndex(activeIndex);
        } catch (NumberFormatException e) {
          // If parsing fails, it safely ignores and keeps the default tab
        }
      }
    }
  }

  public void setDefaultContent(TabContentBuilder tabContentBuilder) {
    defaultContent = tabContentBuilder;
  }

  public void createAndAddTab(Widget tabButtonInnerWidget, TabContentBuilder tabContentBuilder) {
    SimplePanel tabButtonContainer = new SimplePanel();
    tabButtonContainer.addStyleName("tabButtonContainer");
    FocusPanel tabButton = new FocusPanel(tabButtonInnerWidget);
    tabButton.addStyleName("tabButton");
    tabButtonContainer.setWidget(tabButton);
    tabButtons.add(tabButtonContainer);
    tabs.put(tabButtonContainer, tabContentBuilder);
    tabButton.addClickHandler(createTabClickHandler(tabButtonContainer));
    if (selectedTab == null) {
      selectTab(tabButtonContainer);
    }
  }

  public void createAndAddTab(SafeHtml tabTitle, TabContentBuilder tabContentBuilder) {
    SimplePanel tabButtonContainer = new SimplePanel();
    tabButtonContainer.addStyleName("tabButtonContainer");
    Button tabButton = new Button(tabTitle);
    tabButton.addStyleName("tabButton");
    tabButtonContainer.setWidget(tabButton);
    tabButtons.add(tabButtonContainer);
    tabs.put(tabButtonContainer, tabContentBuilder);
    tabButton.addClickHandler(createTabClickHandler(tabButtonContainer));
    if (selectedTab == null) {
      selectTab(tabButtonContainer);
    }
  }

  public void addStaticElement(Widget widget) {
    SimplePanel tabButtonContainer = new SimplePanel();
    tabButtonContainer.addStyleName("tabButtonContainer");
    tabButtonContainer.setWidget(widget);
    tabButtons.add(tabButtonContainer);
  }

  private ClickHandler createTabClickHandler(Widget tabButtonContainer) {
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        selectTab(tabButtonContainer);
      }
    };
  }

  protected void selectTab(Widget tabButtonContainer) {
    if (selectedTab == null || !selectedTab.equals(tabButtonContainer)) {
      for (Map.Entry<Widget, TabContentBuilder> tab : tabs.entrySet()) {
        if (tab.getKey().equals(tabButtonContainer)) {
          selectedTab = tabButtonContainer;
          tab.getKey().addStyleName("tabSelected");
          tabContentWrapper.setWidget(tab.getValue().buildTabWidget());

          if (storageKey != null) {
            Storage sessionStorage = Storage.getSessionStorageIfSupported();
            if (sessionStorage != null) {
              sessionStorage.setItem(storageKey, String.valueOf(getSelectedTabIndex()));
            }
          }

        } else {
          tab.getKey().removeStyleName("tabSelected");
        }
      }
      if (tabButtonContainer == null) {
        tabContentWrapper.setWidget(defaultContent.buildTabWidget());
      }
    }
  }

  public void clear() {
    tabButtons.clear();
    tabContentWrapper.clear();
    tabs.clear();
    selectedTab = null;
  }

  public int getSelectedTabIndex() {
    if (selectedTab == null) {
      return 0; // Default to first tab
    }
    return tabButtons.getWidgetIndex(selectedTab);
  }

  public void selectTabByIndex(int index) {
    if (index >= 0 && index < tabButtons.getWidgetCount()) {
      Widget tabToSelect = tabButtons.getWidget(index);
      selectTab(tabToSelect);
    }
  }

  // --- NEW: Add the onLoad method ---
  @Override
  protected void onLoad() {
    super.onLoad();

    // 1. Dynamically generate a unique key based on the child class (e.g.,
    // "org.roda...DisposalPolicyTabs_ActiveIndex")
    if (this.storageKey == null) {
      this.storageKey = this.getClass().getName() + "_ActiveIndex";
    }

    // 2. Restore the saved index now that the tabs are built and the widget is
    // attached
    Storage sessionStorage = Storage.getSessionStorageIfSupported();
    if (sessionStorage != null) {
      String savedIndexStr = sessionStorage.getItem(storageKey);
      if (savedIndexStr != null) {
        try {
          int activeIndex = Integer.parseInt(savedIndexStr);
          // Only select if the saved index is valid for the current number of tabs
          if (activeIndex >= 0 && activeIndex < tabButtons.getWidgetCount()) {
            selectTabByIndex(activeIndex);
          }
        } catch (NumberFormatException e) {
          // Defaults gracefully if parsing fails
        }
      }
    }
  }

  public interface TabContentBuilder {
    public Widget buildTabWidget();
  }

  interface MyUiBinder extends UiBinder<Widget, Tabs> {
  }
}
