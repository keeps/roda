/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.Collections;

import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.utils.PermissionClientUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractActionable<T extends IsIndexed> implements Actionable<T> {

  private static AsyncCallback<ActionImpact> createDefaultAsyncCallback() {
    return new NoAsyncCallback<>();
  }

  protected static void unsupportedAction(Actionable.Action action, AsyncCallback<ActionImpact> callback) {
    callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
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
  public CanActResult canAct(Action<T> action, ActionableObject<T> object) {
    CanActResult userCanActResult = userCanAct(action, object);
    if (userCanActResult.canAct()) {
      return contextCanAct(action, object);
    } else {
      return userCanActResult;
    }
  }

  @Override
  public CanActResult userCanAct(Action<T> action, ActionableObject<T> object) {
    switch (object.getType()) {
      case MULTIPLE:
        return userCanAct(action, object.getObjects());
      case SINGLE:
        return userCanAct(action, object.getObject());
      case NONE:
      default:
        return userCanAct(action);
    }
  }

  @Override
  public CanActResult contextCanAct(Action<T> action, ActionableObject<T> object) {
    switch (object.getType()) {
      case MULTIPLE:
        return contextCanAct(action, object.getObjects());
      case SINGLE:
        return contextCanAct(action, object.getObject());
      case NONE:
      default:
        return contextCanAct(action);
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
        act(action, object.getObjects(), callback);
        break;
      case SINGLE:
        act(action, object.getObject(), callback);
        break;
      case NONE:
      default:
        act(action, callback);
    }
  }

  @Override
  public CanActResult canAct(Action<T> action) {
    CanActResult userCanActResult = userCanAct(action);
    if (userCanActResult.canAct()) {
      return contextCanAct(action);
    } else {
      return userCanActResult;
    }
  }

  @Override
  public CanActResult userCanAct(Action<T> action) {
    // by default the actionable can not act
    return new CanActResult(false, CanActResult.Reason.USER, "");
  }

  @Override
  public CanActResult contextCanAct(Action<T> action) {
    // by default the actionable can not act
    return new CanActResult(false, CanActResult.Reason.CONTEXT, "");
  }

  @Override
  public void act(Action<T> action, AsyncCallback<ActionImpact> callback) {
    // by default the actionable can not act
    unsupportedAction(action, callback);
  }

  @Override
  public CanActResult canAct(Action<T> action, T object) {
    CanActResult userCanActResult = userCanAct(action, object);
    if (userCanActResult.canAct()) {
      return contextCanAct(action, object);
    } else {
      return userCanActResult;
    }
  }

  @Override
  public CanActResult userCanAct(Action<T> action, T object) {
    // by default the actionable can not act
    return new CanActResult(false, CanActResult.Reason.USER, "");
  }

  @Override
  public CanActResult contextCanAct(Action<T> action, T object) {
    // by default the actionable can not act
    return new CanActResult(false, CanActResult.Reason.CONTEXT, "");
  }

  @Override
  public void act(Action<T> action, T object, AsyncCallback<ActionImpact> callback) {
    // by default the actionable can not act
    unsupportedAction(action, callback);
  }

  @Override
  public CanActResult canAct(Action<T> action, SelectedItems<T> objects) {
    CanActResult userCanActResult = userCanAct(action, objects);
    if (userCanActResult.canAct()) {
      return contextCanAct(action, objects);
    } else {
      return userCanActResult;
    }
  }

  @Override
  public CanActResult userCanAct(Action<T> action, SelectedItems<T> objects) {
    // by default the actionable can not act
    return new CanActResult(false, CanActResult.Reason.USER, "");
  }

  @Override
  public CanActResult contextCanAct(Action<T> action, SelectedItems<T> objects) {
    // by default the actionable can not act
    return new CanActResult(false, CanActResult.Reason.CONTEXT, "");
  }

  @Override
  public void act(Action<T> action, SelectedItems<T> objects, AsyncCallback<ActionImpact> callback) {
    // by default the actionable can not act
    unsupportedAction(action, callback);
  }

  public boolean hasPermissions(Action<T> action) {
    return hasPermissions(action, null);
  }

  public boolean hasPermissions(Action<T> action, Permissions permissions) {
    return PermissionClientUtils.hasPermissions(action.getMethods(), permissions);
  }

  public boolean hasAnyRoles() {
    return Arrays.stream(this.getActions()).anyMatch(action -> hasPermissions(action));
  }
}
