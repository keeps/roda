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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.CanActResult;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableButton;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.model.ActionableTitle;
import org.roda.wui.common.client.tools.ConfigurationManager;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class ActionableWidgetBuilder<T extends IsIndexed> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final AsyncCallback<ActionImpact> DEFAULT_ACTION_CALLBACK = new NoAsyncCallback<>();
  private static final Consumer<Integer> DEFAULT_WIDGET_CREATION_HANDLER = new Consumer<Integer>() {
    @Override
    public void accept(Integer buttonCount) {
      // do nothing
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

  public Widget buildListWithObjects(ActionableObject<T> objects, List<Actionable.Action<T>> actionWhitelist,
    List<Actionable.Action<T>> actionBlacklist) {
    ActionableBundle<T> actionableBundle = actionable.createActionsBundle();

    if (!actionWhitelist.isEmpty()) {
      // remove unwanted buttons, and the whole group if it is empty
      actionableBundle.getGroups().removeIf(group -> {
        group.getButtons().removeIf(button -> !actionWhitelist.contains(button.getAction()));
        return group.getButtons().isEmpty();
      });
    }
    if (!actionBlacklist.isEmpty()) {
      // remove blacklisted buttons
      actionableBundle.getGroups().removeIf(group -> {
        group.getButtons().removeIf(button -> actionBlacklist.contains(button.getAction()));
        return group.getButtons().isEmpty();
      });
    }

    return createActionsMenu(actionableBundle, objects);
  }

  public Widget buildListWithObjectsAndDefaults(ActionableObject<T> objects) {
    ActionableBundle<T> actionableBundle = actionable.createActionsBundle();
    return createActionsMenuWithDefaults(actionableBundle, objects);
  }

  public Widget buildListWithObjectsAndDefaults(ActionableObject<T> objects, List<Actionable.Action<T>> actionWhitelist,
    List<Actionable.Action<T>> actionBlacklist) {
    ActionableBundle<T> actionableBundle = actionable.createActionsBundle();

    if (!actionWhitelist.isEmpty()) {
      // remove unwanted buttons, and the whole group if it is empty
      actionableBundle.getGroups().removeIf(group -> {
        group.getButtons().removeIf(button -> !actionWhitelist.contains(button.getAction()));
        return group.getButtons().isEmpty();
      });
    }
    if (!actionBlacklist.isEmpty()) {
      // remove blacklisted buttons
      actionableBundle.getGroups().removeIf(group -> {
        group.getButtons().removeIf(button -> actionBlacklist.contains(button.getAction()));
        return group.getButtons().isEmpty();
      });
    }

    return createActionsMenuWithDefaults(actionableBundle, objects);
  }

  public Widget buildGroupedListWithObjects(ActionableObject<T> objects) {
    ActionableBundle<T> actionableBundle = actionable.createActionsBundle();
    return createGroupedActionsMenu(actionableBundle, objects, List.of());
  }

  public Widget buildGroupedListWithObjects(ActionableObject<T> objects, List<Actionable.Action<T>> actionWhitelist,
    List<Actionable.Action<T>> ungroupedActions) {
    ActionableBundle<T> actionableBundle = actionable.createActionsBundle();

    if (!actionWhitelist.isEmpty()) {
      // remove unwanted buttons, and the whole group if it is empty
      actionableBundle.getGroups().removeIf(group -> {
        group.getButtons().removeIf(button -> !actionWhitelist.contains(button.getAction()));
        return group.getButtons().isEmpty();
      });
    }

    return createGroupedActionsMenu(actionableBundle, objects, ungroupedActions);
  }

  // Internal (GUI elements creation)

  private FlowPanel createActionsMenu(ActionableBundle<T> actionableBundle, ActionableObject<T> objects) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("actionable-menu");

    boolean isReadonly = NodeType.valueOf(ConfigurationManager.getString(RodaConstants.RODA_NODE_TYPE_KEY))
      .equals(NodeType.REPLICA);
    int addedButtonCount = 0;

    for (ActionableGroup<T> actionGroup : actionableBundle.getGroups()) {
      boolean hasButtonsOnThisGroup = false;
      for (ActionableButton<T> actionButton : actionGroup.getButtons()) {
        if ((!isReadonly || actionButton.getImpact().equals(ActionImpact.NONE))
          && actionable.canAct(actionButton.getAction(), objects).canAct()) {
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
          if ((!isReadonly || actionButton.getImpact().equals(ActionImpact.NONE))
            && actionable.canAct(actionButton.getAction(), objects).canAct()) {

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

  private FlowPanel createActionsMenuWithDefaults(ActionableBundle<T> actionableBundle, ActionableObject<T> objects) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("actionable-menu");

    boolean isReadonly = NodeType.valueOf(ConfigurationManager.getString(RodaConstants.RODA_NODE_TYPE_KEY))
      .equals(NodeType.REPLICA);
    int addedButtonCount = 0;

    for (ActionableGroup<T> actionGroup : actionableBundle.getGroups()) {
      ActionableTitle actionableTitle = actionGroup.getTitle();
      Label groupTitle = new Label(actionableTitle.getTitle());
      groupTitle.addStyleName("h4 actionable-title");
      if (!actionableTitle.hasTitle()) {
        groupTitle.addStyleName("actionable-title-empty");
      }
      panel.add(groupTitle);
      groupTitle.setVisible(false);

      for (ActionableButton<T> actionButton : actionGroup.getButtons()) {
        if ((!isReadonly || actionButton.getImpact().equals(ActionImpact.NONE))
          && actionable.userCanAct(actionButton.getAction(), objects).canAct()) {
          groupTitle.setVisible(true);
          ActionButton<T> button = new ActionButton<>(actionButton);
          panel.add(button);
          addedButtonCount++;
          CanActResult contextCanAct = actionable.contextCanAct(actionButton.getAction(), objects);
          if (contextCanAct.canAct()) {
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
          } else {
            button.setTitle(contextCanAct.getReasonSummary());
            button.setEnabled(false);
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

    widgetCreatedHandler.accept(addedButtonCount);

    return panel;
  }

  private FlowPanel createGroupedActionsMenu(ActionableBundle<T> actionableBundle, ActionableObject<T> objects,
    List<Actionable.Action<T>> ungroupedActions) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("groupedActionableMenu");

    boolean isReadonly = NodeType.valueOf(ConfigurationManager.getString(RodaConstants.RODA_NODE_TYPE_KEY))
      .equals(NodeType.REPLICA);
    int addedButtonCount = 0;

    boolean firstGroup = true;
    for (ActionableGroup<T> actionGroup : actionableBundle.getGroups()) {
      FlowPanel groupPanel = null;
      FlowPanel buttonsPanel = new FlowPanel();
      Button groupButton = null;
      for (ActionableButton<T> actionButton : actionGroup.getButtons()) {
        if ((!isReadonly || actionButton.getImpact().equals(ActionImpact.NONE))
          && actionable.canAct(actionButton.getAction(), objects).canAct()
          && !ungroupedActions.contains(actionButton.getAction())) {
          ActionableTitle actionableTitle = actionGroup.getTitle();

          groupPanel = new FlowPanel();

          SimplePanel anchorPanel = new SimplePanel();
          anchorPanel.addStyleName("popupAnchor");

          PopupPanel popupPanel = new PopupPanel(true);

          buttonsPanel.addStyleName("groupedActionableDropdown");

          groupButton = new Button(actionableTitle.getTitle());
          if (actionGroup.getIcon() != null) {
            groupButton.addStyleName(actionGroup.getIcon());
          }
          groupButton.addStyleName("groupedActionableDropdownButton");
          if (!actionableTitle.hasTitle()) {
            groupButton.addStyleName("groupedActionableDropdownButtonEmpty");
          }
          groupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              if (popupPanel.isShowing()) {
                popupPanel.hide();
              } else {
                popupPanel.showRelativeTo(anchorPanel);
              }
            }
          });

          if (!firstGroup) {
            SimplePanel verticalDivider = new SimplePanel();
            verticalDivider.addStyleName("verticalDivider");
            panel.add(verticalDivider);
          } else {
            firstGroup = false;
          }
          panel.add(groupPanel);
          groupPanel.add(groupButton);
          groupPanel.add(anchorPanel);
          popupPanel.add(buttonsPanel);
          break;
        }

      }

      for (ActionableButton<T> actionButton : actionGroup.getButtons()) {
        if ((!isReadonly || actionButton.getImpact().equals(ActionImpact.NONE))
          && actionable.canAct(actionButton.getAction(), objects).canAct()) {

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

          addedButtonCount++;
          if (ungroupedActions.contains(actionButton.getAction())) {
            if (!firstGroup) {
              SimplePanel verticalDivider = new SimplePanel();
              verticalDivider.addStyleName("verticalDivider");
              panel.add(verticalDivider);
            } else {
              firstGroup = false;
            }
            panel.add(button);
          } else {
            buttonsPanel.add(button);
          }
        }
      }
    }

    if (includeBackButton) {
      ActionButton<T> backButton = new ActionButton<>(
        new ActionableButton<>(messages.backButton(), null, ActionImpact.NONE, "fas fa-arrow-circle-left"));
      backButton.addClickHandler(event -> History.back());
      backButton.addStyleName("groupedActionableButtonBack");
      panel.add(backButton);
      addedButtonCount++;
    }

    if (addedButtonCount == 0) {
      Label emptyHelpText = new Label(messages.actionableEmptyHelp(objects.getType()));
      emptyHelpText.addStyleName("groupedActionableEmptyHelp");
      panel.add(emptyHelpText);
    }

    widgetCreatedHandler.accept(addedButtonCount);

    return panel;
  }

}
