/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.wui.client.common.actions.AbstractActionable;
import org.roda.wui.client.common.actions.ActionableInterceptor;
import org.roda.wui.client.common.actions.DisposalHoldAction;
import org.roda.wui.client.common.actions.DisposalHoldToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateOrUpdateDisposalHoldActionsToolbar extends BrowseObjectActionsToolbar<DisposalHold> {
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
    final DisposalHoldAction targetAction = isCreate ? DisposalHoldAction.SAVE : DisposalHoldAction.UPDATE;

    // Create an interceptor that checks validity before calling the backend
    AbstractActionable<DisposalHold> interceptor = new ActionableInterceptor<DisposalHold>(
      DisposalHoldToolbarActions.get()) {

      @Override
      public void act(Action<DisposalHold> action, DisposalHold hold, AsyncCallback<ActionImpact> callback) {

        if (DisposalHoldAction.SAVE.equals(action) || DisposalHoldAction.UPDATE.equals(action)) {
          // FRONT-END VALIDATION
          if (getDataPanel() != null && !getDataPanel().isValid()) {
            callback.onSuccess(ActionImpact.NONE);
            return;
          }

          // Pass the freshly validated data down the chain
          DisposalHold value = getDataPanel().getValue();
          if (!isCreate) {
            value.setId(hold.getId());
          }
          super.act(action, value, callback);
        } else {
          super.act(action, hold, callback);
        }
      }
    };

    // Bind the SAVE button to our new interceptor
    this.actions.add(new ActionableWidgetBuilder<DisposalHold>(interceptor).withActionCallback(actionCallback)
      .buildUngroupedActions(new ActionableObject<>(object), List.of(targetAction)));
  }
}
