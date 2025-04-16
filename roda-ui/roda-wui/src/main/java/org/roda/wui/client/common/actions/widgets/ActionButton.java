/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions.widgets;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.actions.model.ActionableButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionButton<T extends IsIndexed> extends Composite implements HasEnabled, HasClickHandlers, HasText {
  private final ActionableButton<T> actionButton;

  private FlowPanel button;
  private Label label;

  public ActionButton(ActionableButton<T> actionButton) {
    this.actionButton = actionButton;
    button = new FlowPanel();

    initWidget(button);

    setStylePrimaryName("actionable-button");
    addStyleDependentName(actionButton.getImpact().toString().toLowerCase());
    setEnabled(true);

    label = new Label(actionButton.getText());
    label.addStyleName("actionable-button-label");
    button.add(label);

    String iconClass = "fa fa-question-circle";
    for (String possibleIcon : actionButton.getExtraCssClasses()) {
      if (possibleIcon.startsWith("btn-")) {
        iconClass = possibleIcon.replaceFirst("btn-", "fa fa-");
      } else if (possibleIcon.startsWith("fas fa-") || possibleIcon.startsWith("far fa-")
        || possibleIcon.startsWith("fal fa-")) {
        iconClass = possibleIcon;
      }
    }
    HTMLPanel icon = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<i class='" + iconClass + "'></i>"));
    icon.addStyleName("actionable-button-icon");
    button.add(icon);
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(new ClickHandlerWrapper(this, handler), ClickEvent.getType());
  }

  @Override
  public boolean isEnabled() {
    return !getElement().getPropertyBoolean("disabled");
  }

  @Override
  public void setEnabled(boolean enabled) {
    getElement().setPropertyBoolean("disabled", !enabled);

    if (getElement().getPropertyBoolean("disabled")) {
      addStyleDependentName("disabled");
    } else {
      removeStyleDependentName("disabled");
    }
  }

  @Override
  public String getText() {
    return label.getText();
  }

  @Override
  public void setText(String text) {
    label.setText(text);
  }

  /**
   * Wrapper for click handler that only triggers the action if the button is
   * enabled
   */
  private class ClickHandlerWrapper implements ClickHandler {
    private final ActionButton button;
    private final ClickHandler innerClickHandler;

    ClickHandlerWrapper(ActionButton button, ClickHandler innerClickHandler) {
      this.button = button;
      this.innerClickHandler = innerClickHandler;
    }

    @Override
    public void onClick(ClickEvent event) {
      if (button.isEnabled()) {
        innerClickHandler.onClick(event);
      }
    }
  }
}
