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

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableButton;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.model.ActionableTitle;
import org.roda.wui.client.common.popup.CalloutPopup;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class ActionableWidgetBuilder<T extends IsIndexed> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final String TITLE_CSS_H2 = "h2";
  private static final String TITLE_CSS_H5 = "h5";
  private static final String TITLE_CSS_H1_DEFAULT = "h1";

  private static String handleIconCss(String icon) {
    return icon == null ? null : "fa fa-" + icon.replaceFirst("fa ", "").replaceFirst("fa-", "");
  }

  private Actionable<T> actionable = null;

  private String title = null;
  private String icon = null;
  private String titleCss = null;

  private AsyncCallback<ActionImpact> callback = new NoAsyncCallback<>();

  public ActionableWidgetBuilder(Actionable<T> actionable) {
    this.actionable = actionable;
  }

  // Adding a title
  public ActionableWidgetBuilder<T> withTitle(String title) {
    this.title = title;
    this.icon = null;
    this.titleCss = TITLE_CSS_H1_DEFAULT;
    return this;
  }

  public ActionableWidgetBuilder<T> withTitleSmall(String title, String icon) {
    this.title = title;
    this.icon = handleIconCss(icon);
    this.titleCss = TITLE_CSS_H2;
    return this;
  }

  // Adding a callback

  public ActionableWidgetBuilder<T> withCallback(AsyncCallback<ActionImpact> callback) {
    this.callback = callback;
    return this;
  }

  // Changing the initial actionable (to re-use the same builder)

  public ActionableWidgetBuilder<T> changeActionable(Actionable<T> actionable) {
    this.actionable = actionable;
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

  public Widget buildTitleWithObjects(ActionableObject<T> objects) {
    ActionableBundle<T> actionableBundle = actionable.createActionsBundle();
    return createActionsTitle(actionableBundle, objects);
  }

  public Widget buildTitleWithoutActions() {
    return createTitleWithoutActions();
  }

  // Internal (GUI elements creation)

  private FlowPanel createActionsMenu(ActionableBundle<T> actionableBundle, ActionableObject<T> objects) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("actionable-menu");

    boolean isEmpty = true;

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

            button.addClickHandler(event -> {
              button.setEnabled(false);
              actionable.act(actionButton.getAction(), objects, new AsyncCallback<Actionable.ActionImpact>() {
                @Override
                public void onFailure(Throwable caught) {
                  callback.onFailure(caught);
                  button.setEnabled(true);
                }

                @Override
                public void onSuccess(Actionable.ActionImpact result) {
                  callback.onSuccess(result);
                  button.setEnabled(true);
                }
              });
            });

            panel.add(button);
            isEmpty = false;
          }
        }
      }
    }

    if (isEmpty) {
      Label emptyHelpText = new Label(messages.actionableEmptyHelp(objects.getType()));
      emptyHelpText.addStyleName("actions-empty-help");
      panel.add(emptyHelpText);
    }

    return panel;
  }

  private Widget createActionsTitle(ActionableBundle<T> actionableBundle, ActionableObject<T> objects) {
    // build inner-container
    FlowPanel inlinePanel = new FlowPanel();
    inlinePanel.addStyleName("actionable-header actionable-header-with-actions " + titleCss);

    // build icon
    HTMLPanel iconPanel;
    if (StringUtils.isNotBlank(icon)) {
      iconPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<i class='" + icon + "'></i>"));
    } else {
      iconPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
    }
    iconPanel.addStyleName("actionable-header-icon");
    inlinePanel.add(iconPanel);

    // build title
    Label titleLabel = new Label(title != null ? title : "");
    titleLabel.addStyleName("actionable-header-text");
    inlinePanel.add(titleLabel);

    // build caret
    InlineHTML inlineHTML = new InlineHTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-caret-down'></i>"));
    inlineHTML.addStyleName("actionable-header-caret");
    inlinePanel.add(inlineHTML);

    // build outer-container
    FocusPanel focusPanel = new FocusPanel(inlinePanel);
    focusPanel.addStyleName("actionable-header-focus-panel");

    // add actions popup
    final CalloutPopup popup = new CalloutPopup();
    popup.addStyleName("actionable-popup ActionableStyleMenu");
    popup.setWidget(createActionsMenu(actionableBundle, objects));
    popup.addCloseHandler(event -> inlinePanel.removeStyleName("actionable-header-with-actions-clicked"));

    focusPanel.addMouseDownHandler(event -> {
      inlinePanel.addStyleName("actionable-header-with-actions-clicked");
      popup.showRelativeTo(inlinePanel, CalloutPopup.CalloutPosition.NONE);
    });

    return focusPanel;
  }

  private Widget createTitleWithoutActions() {
    // build inner-container
    FlowPanel inlinePanel = new FlowPanel();
    inlinePanel.addStyleName("actionable-header actionable-header-with-actions " + titleCss);

    // build icon
    HTMLPanel iconPanel;
    if (StringUtils.isNotBlank(icon)) {
      iconPanel = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<i class='" + icon + "'></i>"));
    } else {
      iconPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
    }
    iconPanel.addStyleName("actionable-header-icon");
    inlinePanel.add(iconPanel);

    // build title
    Label titleLabel = new Label(title != null ? title : "");
    titleLabel.addStyleName("actionable-header-text");
    inlinePanel.add(titleLabel);

    // build outer-container
    FocusPanel focusPanel = new FocusPanel(inlinePanel);
    focusPanel.addStyleName("actionable-header-focus-panel");

    return focusPanel;
  }
}
