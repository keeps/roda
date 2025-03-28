package org.roda.wui.client.common;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.actions.Actionable;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public abstract class BrowseObjectActionsToolbar<T extends IsIndexed> extends ActionsToolbar {
  // Data
  protected T object;
  protected Permissions actionPermissions;
  protected AsyncCallback<Actionable.ActionImpact> actionCallback;

  public void setObjectAndBuild(T object, Permissions permissions,
    AsyncCallback<Actionable.ActionImpact> actionCallback) {
    this.object = object;
    this.actionPermissions = permissions;
    this.actionCallback = actionCallback;
    buildIcon();
    buildTags();
    buildActions();
  }

  protected abstract void buildIcon();

  protected abstract void buildTags();

  protected abstract void buildActions();
}
