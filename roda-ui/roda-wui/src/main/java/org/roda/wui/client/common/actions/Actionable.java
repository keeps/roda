package org.roda.wui.client.common.actions;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public interface Actionable<T extends IsIndexed> {

  static interface Action<T> {

  }
  
  static enum ActionImpact {
    NONE, UPDATED, DESTROYED;
  }

  // SINGLE OBJECT

  boolean canAct(Action<T> action, T object);

  void act(Action<T> action, T object);

  void act(Action<T> action, T object, AsyncCallback<ActionImpact> callback);

  // MULTIPLE OBJECTS

  boolean canAct(Action<T> action, SelectedItems<T> object);

  void act(Action<T> action, SelectedItems<T> objects);

  void act(Action<T> action, SelectedItems<T> objects, AsyncCallback<ActionImpact> callback);

  // Layout

  Widget createActionsLayout(T object, AsyncCallback<ActionImpact> callback);

  Widget createActionsLayout(SelectedItems<T> objects, AsyncCallback<ActionImpact> callback);
}
