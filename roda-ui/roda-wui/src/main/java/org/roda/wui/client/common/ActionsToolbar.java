package org.roda.wui.client.common;

import org.roda.wui.client.common.labels.LabelWithIcon;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ActionsToolbar extends Composite {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ActionsToolbar.MyUiBinder uiBinder = GWT.create(ActionsToolbar.MyUiBinder.class);

  // UI Fields
  @UiField
  AccessibleFocusPanel keyboardFocus;
  @UiField
  FlowPanel toolbar;
  @UiField
  LabelWithIcon label;
  @UiField
  FlowPanel tags;
  @UiField
  FlowPanel actions;

  public ActionsToolbar() {
    initWidget(uiBinder.createAndBindUi(this));

    // UI
    this.label.reverseOrder();
  }

  public void setLabelVisible(boolean visible) {
    label.setVisible(visible);
  }

  public void setLabel(String labelText) {
    label.setText(labelText);
  }

  public void setIcon(String iconClasses) {
    label.setIcon(iconClasses);
  }

  public void addAction(ClickHandler onClick, String title, String buttonStyle) {
    if (actions.getWidgetCount() > 0) {
      SimplePanel divider = new SimplePanel();
      divider.setStyleName("verticalDivider");
      actions.add(divider);
    }
    Button actionButton = new Button(SafeHtmlUtils.fromString(title));
    actionButton.addClickHandler(onClick);
    actionButton.addStyleName(buttonStyle);
    actions.add(actionButton);
  }

  public void setActionableMenu(Widget actionableMenu) {
    actions.clear();
    actions.add(actionableMenu);
  }

  interface MyUiBinder extends UiBinder<Widget, ActionsToolbar> {
  }
}
