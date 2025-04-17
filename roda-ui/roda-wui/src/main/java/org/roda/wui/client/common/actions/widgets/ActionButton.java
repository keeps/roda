/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions.widgets;

import com.google.gwt.user.client.ui.Button;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.actions.model.ActionableButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionButton<T extends IsIndexed> extends Button implements HasEnabled, HasClickHandlers, HasText {
  private final ActionableButton<T> actionButton;

  public ActionButton(ActionableButton<T> actionButton) {
    this.actionButton = actionButton;

    setStylePrimaryName("actionable-button");
    addStyleDependentName(actionButton.getImpact().toString().toLowerCase());
    setEnabled(true);

    addStyleName("actionable-button-label");
    setText(actionButton.getText());

    boolean addedIcon = false;
    for (String possibleIcon : actionButton.getExtraCssClasses()) {
      if (possibleIcon.startsWith("btn-")) {
        addStyleName(possibleIcon);
        addedIcon = true;
      } else if (possibleIcon.startsWith("fas fa-") || possibleIcon.startsWith("far fa-")
        || possibleIcon.startsWith("fal fa-")) {
        addStyleName(possibleIcon.replaceFirst("fa[srl] fa-", "btn-"));
        addedIcon = true;
      }
    }
    if (!addedIcon) {
      addStyleName("btn-question-circle");
    }
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
