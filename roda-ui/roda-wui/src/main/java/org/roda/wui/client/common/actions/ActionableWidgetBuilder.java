/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import static org.roda.wui.client.common.actions.Actionable.ActionImpact;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.NoAsyncCallback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ActionableWidgetBuilder<T extends IsIndexed> {

  private Actionable<T> actionable = null;

  private boolean isTitle = false;
  private String title = null;
  private String icon = null;

  private AsyncCallback<ActionImpact> callback = new NoAsyncCallback<>();

  public ActionableWidgetBuilder(Actionable<T> actionable) {
    this.actionable = actionable;
  }

  public ActionableWidgetBuilder<T> asTitle(String title, String icon) {
    this.title = title;
    this.icon = icon;
    return this;
  }

  public ActionableWidgetBuilder<T> withCallback(AsyncCallback<ActionImpact> callback) {
    this.callback = callback;
    return this;
  }

  public Widget buildWithObjects(ActionableObject<T> objects) {
    ActionsBundle<T> actionsBundle = actionable.createActionsBundle();
    return isTitle ? createActionsTitle(actionsBundle, objects) : createActionsList(actionsBundle, objects);
  }

  private FlowPanel createActionsList(ActionsBundle<T> actionsBundle, ActionableObject<T> objects) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("actions-layout");

    for (ActionsGroup<T> actionGroup : actionsBundle) {
      for (ActionsButton<T> button : actionGroup.getButtons()) {
        if (actionable.canAct(button.getAction(), objects)) {
          panel.add(createGroupTitle(actionGroup.getTitle()));
          break;
        }
      }

      for (ActionsButton<T> actionButton : actionGroup.getButtons()) {
        Button possibleButton = createGroupButton(actionButton, objects);
        if (possibleButton != null) {
          panel.add(possibleButton);
        }
      }
    }

    return panel;
  }

  private Label createGroupTitle(ActionsTitle actionsTitle) {
    Label title = new Label(actionsTitle.getTitle());
    title.addStyleName("h4");
    title.addStyleName("actionable-group-title");
    if (!actionsTitle.hasTitle()) {
      title.addStyleName("actionable-group-title-empty");
    }
    return title;
  }

  private Button createGroupButton(ActionsButton<T> actionButton, ActionableObject<T> objects) {
    if (actionable.canAct(actionButton.getAction(), objects)) {
      // Construct
      Button button = new Button(actionButton.getText());
      button.setTitle(actionButton.getText());
      button.getElement().setId(actionButton.getId());

      // CSS
      button.setStyleName("actions-group-button");
      button.addStyleName("btn");
      button.addStyleName("btn-block");

      if (ActionImpact.DESTROYED.equals(actionButton.getImpact())) {
        button.addStyleName("btn-danger");
      } else if (ActionImpact.UPDATED.equals(actionButton.getImpact())) {
        button.addStyleName("btn-primary");
      } else {
        button.addStyleName("btn-default");
      }

      button.addStyleDependentName(actionButton.getImpact().name().toLowerCase());

      for (String extraCssClass : actionButton.getExtraCssClasses()) {
        button.addStyleName(extraCssClass);
      }

      // Action
      button.addClickHandler(event -> actionable.act(actionButton.getAction(), objects, callback));

      return button;
    }
    return null;
  }

  private Widget createActionsTitle(ActionsBundle<T> actionsBundle, ActionableObject<T> objects) {
    return null;
  }
}
