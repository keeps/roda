/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Collections;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.common.NoAsyncCallback;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractActionable<T extends IsIndexed> implements Actionable<T> {

  private static AsyncCallback<ActionImpact> createDefaultAsyncCallback() {
    return new NoAsyncCallback<>();
  }

  SelectedItemsList<T> objectToSelectedItems(T object, Class<T> objectClass) {
    if (object != null) {
      return new SelectedItemsList<>(Collections.singletonList(object.getUUID()), object.getClass().getName());
    } else {
      return objectToSelectedItems(objectClass);
    }
  }

  SelectedItemsList<T> objectToSelectedItems(Class<T> objectClass) {
    return new SelectedItemsList<>(Collections.emptyList(), objectClass.getName());
  }

  @Override
  public void act(Actionable.Action<T> action) {
    act(action, createDefaultAsyncCallback());
  }

  @Override
  public void act(Actionable.Action<T> action, T object) {
    act(action, object, createDefaultAsyncCallback());
  }

  @Override
  public void act(Actionable.Action<T> action, SelectedItems<T> objects) {
    act(action, objects, createDefaultAsyncCallback());
  }

  @Override
  public boolean canAct(Action<T> action, ActionableObject<T> object) {
    switch (object.getType()) {
      case MULTIPLE:
        return canAct(action, object.getObjects());
      case SINGLE:
        return canAct(action, object.getObject());
      case NONE:
      default:
        return canAct(action);
    }
  }

  @Override
  public void act(Action<T> action, ActionableObject<T> object) {
    act(action, object, createDefaultAsyncCallback());
  }

  @Override
  public void act(Action<T> action, ActionableObject<T> object, AsyncCallback<ActionImpact> callback) {
    switch (object.getType()) {
      case MULTIPLE:
        act(action, object.getObjects());
        break;
      case SINGLE:
        act(action, object.getObject());
        break;
      case NONE:
      default:
        act(action);
    }
  }

  @Override
  public boolean canAct(Action<T> action) {
    // by default the actionable can not act
    return false;
  }

  @Override
  public void act(Action<T> action, AsyncCallback<ActionImpact> callback) {
    // by default the actionable can not act
  }

  @Override
  public boolean canAct(Action<T> action, T object) {
    // by default the actionable can not act
    return false;
  }

  @Override
  public void act(Action<T> action, T object, AsyncCallback<ActionImpact> callback) {
    // by default the actionable can not act
  }

  @Override
  public boolean canAct(Action<T> action, SelectedItems<T> objects) {
    // by default the actionable can not act
    return false;
  }

  @Override
  public void act(Action<T> action, SelectedItems<T> objects, AsyncCallback<ActionImpact> callback) {
    // by default the actionable can not act
  }
}
