package org.roda.wui.client.common.actions;

import org.roda.wui.client.common.NoAsyncCallback;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class ActionAsyncCallback<T> implements AsyncCallback<T> {
  private final AsyncCallback<Actionable.ActionImpact> actionCallback;

  /**
   * Use ActionAsyncCallback(AsyncCallback<Actionable.ActionImpact>
   * actionCallback)
   */
  private ActionAsyncCallback() {
    // This assignment is never used, but it avoids "possible null" when accessing
    // `this.actionCallback`
    this.actionCallback = new NoAsyncCallback<>();
  }

  ActionAsyncCallback(AsyncCallback<Actionable.ActionImpact> actionCallback) {
    this.actionCallback = actionCallback;
  }

  void doActionCallbackNone() {
    actionCallback.onSuccess(Actionable.ActionImpact.NONE);
  }

  void doActionCallbackUpdated() {
    actionCallback.onSuccess(Actionable.ActionImpact.UPDATED);
  }

  void doActionCallbackDestroyed() {
    actionCallback.onSuccess(Actionable.ActionImpact.DESTROYED);
  }

  @Override
  public void onFailure(Throwable caught) {
    actionCallback.onFailure(caught);
  }
}
