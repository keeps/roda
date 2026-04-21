/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.wui.client.common.actions.AbstractActionable;
import org.roda.wui.client.common.actions.ActionableInterceptor;
import org.roda.wui.client.common.actions.DisposalScheduleAction;
import org.roda.wui.client.common.actions.DisposalScheduleToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateOrUpdateDisposalScheduleActionsToolbar extends BrowseObjectActionsToolbar<DisposalSchedule> {
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
    final DisposalScheduleAction targetAction = isCreate ? DisposalScheduleAction.SAVE : DisposalScheduleAction.UPDATE;

    // Create an interceptor that checks validity before calling the backend
    AbstractActionable<DisposalSchedule> interceptor = new ActionableInterceptor<DisposalSchedule>(
      DisposalScheduleToolbarActions.get()) {

      @Override
      public void act(Action<DisposalSchedule> action, DisposalSchedule schedule,
        AsyncCallback<ActionImpact> callback) {

        if (DisposalScheduleAction.SAVE.equals(action) || DisposalScheduleAction.UPDATE.equals(action)) {
          // FRONT-END VALIDATION
          if (getDataPanel() != null && !getDataPanel().isValid()) {
            callback.onSuccess(ActionImpact.NONE);
            return;
          }

          // Pass the freshly validated data down the chain
          DisposalSchedule value = getDataPanel().getValue();
          if (!isCreate) {
            value.setId(schedule.getId());
          }
          super.act(action, value, callback);
        } else {
          super.act(action, schedule, callback);
        }
      }
    };

    // Bind the SAVE button to our new interceptor
    this.actions.add(new ActionableWidgetBuilder<DisposalSchedule>(interceptor).withActionCallback(actionCallback)
      .buildUngroupedActions(new ActionableObject<>(object), List.of(targetAction)));
  }
}
