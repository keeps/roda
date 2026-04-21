/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.wui.client.common.actions.AbstractActionable;
import org.roda.wui.client.common.actions.ActionableInterceptor;
import org.roda.wui.client.common.actions.DisposalRuleAction;
import org.roda.wui.client.common.actions.DisposalRuleSearchWrapperActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateOrUpdateDisposalRuleActionsToolbar extends BrowseObjectActionsToolbar<DisposalRule> {
  private boolean isCreate = true;

  public void setIsCreate(boolean isCreate) {
    this.isCreate = isCreate;
  }

  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();

    // Determine the correct action based on context
    final DisposalRuleAction targetAction = isCreate ? DisposalRuleAction.SAVE : DisposalRuleAction.UPDATE;

    // Create an interceptor that checks validity before calling the backend
    AbstractActionable<DisposalRule> interceptor = new ActionableInterceptor<DisposalRule>(
      DisposalRuleSearchWrapperActions.get()) {

      @Override
      public void act(Action<DisposalRule> action, DisposalRule rule, AsyncCallback<ActionImpact> callback) {

        if (DisposalRuleAction.SAVE.equals(action) || DisposalRuleAction.UPDATE.equals(action)) {
          // FRONT-END VALIDATION
          if (getDataPanel() != null && !getDataPanel().isValid()) {
            callback.onSuccess(ActionImpact.NONE);
            return;
          }

          // Pass the freshly validated data down the chain
          DisposalRule value = getDataPanel().getValue();
          value.setOrder(rule.getOrder());
          if (!isCreate) {
            value.setId(rule.getId());
          }
          super.act(action, value, callback);
        } else {
          super.act(action, rule, callback);
        }
      }
    };

    // Bind the SAVE button to our new interceptor
    this.actions.add(new ActionableWidgetBuilder<DisposalRule>(interceptor).withActionCallback(actionCallback)
      .buildUngroupedActions(new ActionableObject<>(object), List.of(targetAction)));
  }
}
