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

import config.i18n.client.ClientMessages;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class Tabs extends Composite {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Tabs.MyUiBinder uiBinder = GWT.create(Tabs.MyUiBinder.class);

  @UiField
  FlowPanel tabButtons;
  @UiField
  SimplePanel tabContentWrapper;

  private Widget selectedTab;
  private final Map<Widget, TabContentBuilder> tabs;

  public Tabs() {
    initWidget(uiBinder.createAndBindUi(this));

    tabs = new HashMap<>();
    selectedTab = null;
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

  private void selectTab(Widget tabButtonContainer) {
    if (selectedTab == null || !selectedTab.equals(tabButtonContainer)) {
      for (Map.Entry<Widget, TabContentBuilder> tab : tabs.entrySet()) {
        if (tab.getKey().equals(tabButtonContainer)) {
          selectedTab = tabButtonContainer;
          tab.getKey().addStyleName("tabSelected");
          tabContentWrapper.setWidget(tab.getValue().buildTabWidget());
        } else {
          tab.getKey().removeStyleName("tabSelected");
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
