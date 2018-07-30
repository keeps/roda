package org.roda.wui.client.common.actions.callbacks;

import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class ActionLoadingAsyncCallback<T> extends LoadingAsyncCallback<T> {
  private final AsyncCallback<Actionable.ActionImpact> actionCallback;

  public ActionLoadingAsyncCallback(AsyncCallback<Actionable.ActionImpact> actionCallback) {
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
    super.onFailure(caught);
    actionCallback.onFailure(caught);
  }
}
