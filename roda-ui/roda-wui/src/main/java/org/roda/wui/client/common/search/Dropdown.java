/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Dropdown extends Composite implements HasValueChangeHandlers<String> {
  private FocusPanel focusPanel;
  private SimplePanel panel;
  private FlowPanel iconAndLabelPanel;
  private Label selectedLabel;
  private InlineHTML selectedIcon;
  private PopupPanel popup;
  private VerticalPanel popupPanel;
  private boolean popupShowing = false;

  private Map<String, String> popupValues;
  private Map<String, String> popupIcons;

  public Dropdown() {
    focusPanel = new FocusPanel();
    panel = new SimplePanel();
    iconAndLabelPanel = new FlowPanel();
    selectedLabel = new Label();
    selectedIcon = new InlineHTML();
    popup = new PopupPanel(true);
    popupPanel = new VerticalPanel();
    popupValues = new HashMap<>();
    popupIcons = new HashMap<>();

    iconAndLabelPanel.add(selectedIcon);
    iconAndLabelPanel.add(selectedLabel);
    panel.add(iconAndLabelPanel);
    focusPanel.add(panel);

    focusPanel.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        popup();
      }
    });

    popup.addCloseHandler(new CloseHandler<PopupPanel>() {

      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        panel.removeStyleName("open");
      }
    });

    initWidget(focusPanel);

    focusPanel.addStyleName("dropdown");
    panel.addStyleName("dropdown-panel");
    iconAndLabelPanel.addStyleName("dropdown-label");
    popup.addStyleName("dropdown-popup");
  }

  public void addItem(final String label, final String value, final String icon) {
    popupValues.put(label, value);
    popupIcons.put(label, icon);

    FlowPanel item = new FlowPanel();

    InlineHTML iconPanel = new InlineHTML();
    iconPanel.setHTML(createHtmlForIcon(icon));
    item.add(iconPanel);

    Label labelWidget = new Label(label);
    item.addStyleName("dropdown-item");

    item.addDomHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        selectedLabel.setText(label);
        selectedIcon.setHTML(createHtmlForIcon(icon));
        onChange();
        popup();
      }
    }, ClickEvent.getType());

    item.add(labelWidget);

    popupPanel.add(item);

    popup.setWidget(popupPanel);
    setPanelWidth();
  }

  public void setLabel(String text) {
    selectedLabel.setText(text);
  }

  public void popup() {
    if (popupShowing) {
      popup.hide();
      popupShowing = popup.isShowing();
    } else {
      // popup.showRelativeTo(panel);
      popup.setWidth(panel.getOffsetWidth() + "px");
      popup.setPopupPosition(panel.getAbsoluteLeft(),
        panel.getAbsoluteTop() + panel.getOffsetHeight());
      popup.show();
      popupShowing = popup.isShowing();
      panel.addStyleName("open");
    }
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getSelectedValue());
  }

  public String getSelectedValue() {
    return popupValues.get(selectedLabel.getText());
  }

  public boolean setSelectedValue(String value, boolean fire) {
    String label = null;
    for (Entry<String, String> entry : popupValues.entrySet()) {
      if (entry.getValue().equals(value)) {
        label = entry.getKey();
        break;
      }
    }

    if (label != null) {
      selectedLabel.setText(label);
      selectedIcon.setHTML(createHtmlForIcon(popupIcons.get(label)));
      if (fire) {
        onChange();
      }
      return true;
    } else {
      return false;
    }
  }

  public void setPanelWidth() {
    boolean visible = popup.isVisible();
    popup.setVisible(true);
    focusPanel.getElement().setAttribute("min-width", popup.getOffsetWidth() + "px");
    popup.setVisible(visible);
  }

  public void addPopupStyleName(String styleName) {
    popup.addStyleName(styleName);
  }

  private SafeHtml createHtmlForIcon(String icon) {
    return SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-" + icon + "\"></i>");
  }
}
