package org.roda.wui.client.common.actions.callbacks;

import org.roda.wui.client.common.actions.Actionable;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class ActionAsyncCallback<T> implements AsyncCallback<T> {
  private final AsyncCallback<Actionable.ActionImpact> actionCallback;

  public ActionAsyncCallback(AsyncCallback<Actionable.ActionImpact> actionCallback) {
    this.actionCallback = actionCallback;
  }

  public void doActionCallbackNone() {
    actionCallback.onSuccess(Actionable.ActionImpact.NONE);
  }

  public void doActionCallbackUpdated() {
    actionCallback.onSuccess(Actionable.ActionImpact.UPDATED);
  }

  public void doActionCallbackDestroyed() {
    actionCallback.onSuccess(Actionable.ActionImpact.DESTROYED);
  }

  @Override
  public void onFailure(Throwable caught) {
    actionCallback.onFailure(caught);
  }
}
