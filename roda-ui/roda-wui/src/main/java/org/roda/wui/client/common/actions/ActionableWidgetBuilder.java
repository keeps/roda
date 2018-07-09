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
import org.roda.wui.client.common.popup.CalloutPopup;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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

  public ActionableWidgetBuilder<T> withTitle(String title, String icon) {
    this.title = title;
    this.icon = handleIconCss(icon);
    this.titleCss = TITLE_CSS_H1_DEFAULT;
    return this;
  }

  public ActionableWidgetBuilder<T> withTitle(String title) {
    this.title = title;
    this.icon = null;
    this.titleCss = TITLE_CSS_H1_DEFAULT;
    return this;
  }

  public ActionableWidgetBuilder<T> withTitleForCard(String title, String icon) {
    this.title = title;
    this.icon = handleIconCss(icon);
    this.titleCss = TITLE_CSS_H5;
    return this;
  }

  public ActionableWidgetBuilder<T> withTitleSmall(String title, String icon) {
    this.title = title;
    this.icon = handleIconCss(icon);
    this.titleCss = TITLE_CSS_H2;
    return this;
  }

  public ActionableWidgetBuilder<T> withTitleLoading() {
    this.title = messages.browseLoading();
    this.icon = null;
    this.titleCss = TITLE_CSS_H1_DEFAULT;
    return this;
  }

  public ActionableWidgetBuilder<T> withTitleSmallLoading() {
    this.title = messages.browseLoading();
    this.icon = null;
    this.titleCss = TITLE_CSS_H2;
    return this;
  }

  // Adding a callback

  public ActionableWidgetBuilder<T> withCallback(AsyncCallback<ActionImpact> callback) {
    this.callback = callback;
    return this;
  }

  // Changing the initial actionable (to re-use the same builder)

  public ActionableWidgetBuilder<T> changeActionable(Actionable<T> actionable){
    this.actionable = actionable;
    return this;
  }

  // Builder methods for lists and titles

  public Widget buildListWithObjects(ActionableObject<T> objects) {
    ActionsBundle<T> actionsBundle = actionable.createActionsBundle();
    return createActionsList(actionsBundle, objects);
  }

  public Widget buildMenuWithObjects(ActionableObject<T> objects) {
    ActionsBundle<T> actionsBundle = actionable.createActionsBundle();
    return createActionsMenu(actionsBundle, objects);
  }

  public Widget buildTitleWithObjects(ActionableObject<T> objects) {
    ActionsBundle<T> actionsBundle = actionable.createActionsBundle();
    return createActionsTitle(actionsBundle, objects);
  }

  public Widget buildTitleWithoutActions() {
    return createTitleWithoutActions();
  }

  // Internal (GUI elements creation)

  private FlowPanel createActionsMenu(ActionsBundle<T> actionsBundle, ActionableObject<T> objects) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("actionable-menu");

    // boolean isEmpty = true;

    for (ActionsGroup<T> actionGroup : actionsBundle) {
      for (ActionsButton<T> actionButton : actionGroup.getButtons()) {
        if (actionable.canAct(actionButton.getAction(), objects)) {
          FlowPanel menuItem = new FlowPanel();
          menuItem.setStylePrimaryName("actionable-menu-item");
          menuItem.addStyleDependentName(actionButton.getImpact().toString().toLowerCase());

          // icon

          String iconClass = "fa fa-exclamation-triangle";
          for (String possibleIcon : actionButton.getExtraCssClasses()) {
            if (possibleIcon.startsWith("btn-")) {
              iconClass = possibleIcon.replaceFirst("btn-", "fa fa-");
            }
          }
          HTMLPanel menuItemIcon = new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<i class='" + iconClass + "'></i>"));
          menuItemIcon.addStyleName("actionable-menu-item-icon");
          menuItem.add(menuItemIcon);

          // label

          Label menuItemLabel = new Label(actionButton.getText());
          menuItemLabel.addStyleName("actionable-menu-item-label");
          menuItem.add(menuItemLabel);

          menuItem.addDomHandler(event -> actionable.act(actionButton.getAction(), objects, callback),
            ClickEvent.getType());

          panel.add(menuItem);
        }
      }
    }

    // if (isEmpty) {
    // Label emptyHelpText = new
    // Label(messages.actionableEmptyHelp(objects.getType()));
    // emptyHelpText.addStyleName("actions-menu-empty-help");
    // panel.add(emptyHelpText);
    // }

    return panel;
  }

  private FlowPanel createActionsList(ActionsBundle<T> actionsBundle, ActionableObject<T> objects) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("actions-layout");

    boolean isEmpty = true;

    for (ActionsGroup<T> actionGroup : actionsBundle) {
      boolean hasButtonsOnThisGroup = false;
      for (ActionsButton<T> button : actionGroup.getButtons()) {
        if (actionable.canAct(button.getAction(), objects)) {
          panel.add(createGroupTitle(actionGroup.getTitle()));
          hasButtonsOnThisGroup = true;
          isEmpty = false;
          break;
        }
      }

      if (hasButtonsOnThisGroup) {
        for (ActionsButton<T> actionButton : actionGroup.getButtons()) {
          Button possibleButton = createGroupButton(actionButton, objects);
          if (possibleButton != null) {
            panel.add(possibleButton);
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
    popup.addStyleName("actionable-popup");
    popup.setWidget(createActionsMenu(actionsBundle, objects));
    focusPanel.addClickHandler(event -> {
      if (popup.isShowing()) {
        popup.hide();
      } else {
        popup.showRelativeTo(inlineHTML, CalloutPopup.CalloutPosition.TOP_RIGHT);
      }
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
