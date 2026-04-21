package org.roda.wui.client.common.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.wui.client.common.actions.model.ActionableBundle;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class ActionableInterceptor<T extends IsIndexed> extends AbstractActionable<T> {
  private final Actionable<T> delegate;

  public ActionableInterceptor(Actionable<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Action<T>[] getActions() {
    return delegate.getActions();
  }

  @Override
  public CanActResult userCanAct(Action<T> action) {
    return delegate.userCanAct(action);
  }

  @Override
  public CanActResult contextCanAct(Action<T> action) {
    return delegate.contextCanAct(action);
  }

  @Override
  public CanActResult userCanAct(Action<T> action, T object) {
    return delegate.userCanAct(action, object);
  }

  @Override
  public CanActResult contextCanAct(Action<T> action, T object) {
    return delegate.contextCanAct(action, object);
  }

  @Override
  public CanActResult userCanAct(Action<T> action, SelectedItems<T> objects) {
    return delegate.userCanAct(action, objects);
  }

  @Override
  public CanActResult contextCanAct(Action<T> action, SelectedItems<T> objects) {
    return delegate.contextCanAct(action, objects);
  }

  @Override
  public ActionableBundle<T> createActionsBundle() {
    return delegate.createActionsBundle();
  }

  @Override
  public Action<T> actionForName(String name) {
    return delegate.actionForName(name);
  }

  @Override
  public void act(Action<T> action, AsyncCallback<ActionImpact> callback) {
    delegate.act(action, callback);
  }

  @Override
  public void act(Action<T> action, T object, AsyncCallback<ActionImpact> callback) {
    delegate.act(action, object, callback);
  }

  @Override
  public void act(Action<T> action, SelectedItems<T> objects, AsyncCallback<ActionImpact> callback) {
    delegate.act(action, objects, callback);
  }
}
