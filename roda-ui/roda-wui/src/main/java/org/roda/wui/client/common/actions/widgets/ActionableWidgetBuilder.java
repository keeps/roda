/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions.widgets;

import static org.roda.wui.client.common.actions.Actionable.ActionImpact;

import java.util.List;
import java.util.function.Consumer;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableButton;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.model.ActionableTitle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class ActionableWidgetBuilder<T extends IsIndexed> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final AsyncCallback<ActionImpact> DEFAULT_ACTION_CALLBACK = new NoAsyncCallback<>();
  private static final Consumer<Integer> DEFAULT_WIDGET_CREATION_HANDLER = new Consumer<Integer>() {
    @Override
    public void accept(Integer buttonCount) {
    }
  };

  private Actionable<T> actionable = null;

  private String title = null;
  private String icon = null;
  private String titleCss = null;

  private AsyncCallback<ActionImpact> actionImpactCallback = DEFAULT_ACTION_CALLBACK;
  private Consumer<Integer> widgetCreatedHandler = DEFAULT_WIDGET_CREATION_HANDLER;
  private boolean includeBackButton = false;

  public ActionableWidgetBuilder(Actionable<T> actionable) {
    this.actionable = actionable;
  }

  // Adding a actionImpactCallback

  public ActionableWidgetBuilder<T> withActionCallback(AsyncCallback<ActionImpact> callback) {
    this.actionImpactCallback = callback;
    return this;
  }

  /**
   * Add a consumer to be called when the actionable widget is generated, the
   * integer parameter will have the number of buttons the uer can act on.
   */
  public ActionableWidgetBuilder<T> withWidgetCreatedHandler(Consumer<Integer> widgetCreatedHandler) {
    this.widgetCreatedHandler = widgetCreatedHandler;
    return this;
  }

  // Changing the initial actionable (to re-use the same builder)

  public ActionableWidgetBuilder<T> changeActionable(Actionable<T> actionable) {
    this.actionable = actionable;
    return this;
  }

  public ActionableWidgetBuilder<T> withBackButton() {
    this.includeBackButton = true;
    return this;
  }

  // Builder methods for lists and titles

  public Widget buildListWithObjects(ActionableObject<T> objects) {
    ActionableBundle<T> actionableBundle = actionable.createActionsBundle();
    return createActionsMenu(actionableBundle, objects);
  }

  public Widget buildListWithObjects(ActionableObject<T> objects, List<Actionable.Action<T>> actionWhitelist) {
    ActionableBundle<T> actionableBundle = actionable.createActionsBundle();

    if (!actionWhitelist.isEmpty()) {
      // remove unwanted buttons, and the whole group if it is empty
      actionableBundle.getGroups().removeIf(group -> {
        group.getButtons().removeIf(button -> !actionWhitelist.contains(button.getAction()));
        return group.getButtons().isEmpty();
      });
    }

    return createActionsMenu(actionableBundle, objects);
  }

  // Internal (GUI elements creation)

  private FlowPanel createActionsMenu(ActionableBundle<T> actionableBundle, ActionableObject<T> objects) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("actionable-menu");

    int addedButtonCount = 0;

    for (ActionableGroup<T> actionGroup : actionableBundle.getGroups()) {
      boolean hasButtonsOnThisGroup = false;
      for (ActionableButton<T> button : actionGroup.getButtons()) {
        if (actionable.canAct(button.getAction(), objects)) {
          ActionableTitle actionableTitle = actionGroup.getTitle();
          Label groupTitle = new Label(actionableTitle.getTitle());
          groupTitle.addStyleName("h4 actionable-title");
          if (!actionableTitle.hasTitle()) {
            groupTitle.addStyleName("actionable-title-empty");
          }
          panel.add(groupTitle);
          hasButtonsOnThisGroup = true;
          break;
        }
      }

      if (hasButtonsOnThisGroup) {
        for (ActionableButton<T> actionButton : actionGroup.getButtons()) {
          if (actionable.canAct(actionButton.getAction(), objects)) {

            ActionButton<T> button = new ActionButton<>(actionButton);

            button.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                button.setEnabled(false);
                actionable.act(actionButton.getAction(), objects, new AsyncCallback<Actionable.ActionImpact>() {
                  @Override
                  public void onFailure(Throwable caught) {
                    actionImpactCallback.onFailure(caught);
                    button.setEnabled(true);
                  }

                  @Override
                  public void onSuccess(Actionable.ActionImpact result) {
                    actionImpactCallback.onSuccess(result);
                    button.setEnabled(true);
                  }
                });
              }
            });

            panel.add(button);
            addedButtonCount++;
          }
        }
      }
    }

    if (includeBackButton) {
      ActionButton<T> backButton = new ActionButton<>(
        new ActionableButton<>(messages.backButton(), null, ActionImpact.NONE, "fas fa-arrow-circle-left"));
      backButton.addClickHandler(event -> History.back());
      backButton.addStyleName("actionable-button-back");
      panel.add(backButton);
      addedButtonCount++;
    }

    if (addedButtonCount == 0) {
      Label emptyHelpText = new Label(messages.actionableEmptyHelp(objects.getType()));
      emptyHelpText.addStyleName("actions-empty-help");
      panel.add(emptyHelpText);
    }

    widgetCreatedHandler.accept(addedButtonCount);

    return panel;
  }

}
