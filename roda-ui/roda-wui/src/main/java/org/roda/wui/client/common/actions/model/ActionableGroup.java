package org.roda.wui.client.common.actions.model;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasPermissions;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.common.client.tools.ConfigurationManager;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionableGroup<T extends IsIndexed> {
  private final ActionableTitle title;
  private List<ActionableButton<T>> buttons = new ArrayList<>();

  public ActionableGroup(String title) {
    this.title = new ActionableTitle(title);
  }

  public ActionableGroup() {
    this.title = new ActionableTitle(null);
  }

  // TODO: remove extraCssClasses and generate them based on other info
  public ActionableGroup addButton(String text, Actionable.Action<T> action, Actionable.ActionImpact impact,
    String... extraCssClasses) {
    ActionableButton<T> button = new ActionableButton<>(text, action, impact, extraCssClasses);
    buttons.add(button);
    return this;
  }

  public ActionableTitle getTitle() {
    return title;
  }

  public List<ActionableButton<T>> getButtons() {
    return this.buttons;
  }
}
