package org.roda.wui.client.common;

import java.util.List;
import java.util.Optional;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.labels.Tag;

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
  protected AIPState state;

  public void setObjectAndBuild(T object, AIPState state, Permissions permissions,
    AsyncCallback<Actionable.ActionImpact> actionCallback) {
    this.object = object;
    this.state = state;
    this.actionPermissions = permissions;
    this.actionCallback = actionCallback;
    buildIcon();
    buildTags();
    buildActions();
  }

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

  protected Optional<Tag> getStateTag() {
    switch (state) {
      case ACTIVE:
        return Optional.empty();
      case DESTROYED:
        return Optional.empty();
      case UNDER_APPRAISAL:
        return Optional
          .of(Tag.fromText(messages.aipState(state), List.of(Tag.TagStyle.WARNING_LIGHT, Tag.TagStyle.MONO)));
      default:
        return Optional
          .of(Tag.fromText(messages.aipState(state), List.of(Tag.TagStyle.DANGER_LIGHT, Tag.TagStyle.MONO)));
    }
  }
}
