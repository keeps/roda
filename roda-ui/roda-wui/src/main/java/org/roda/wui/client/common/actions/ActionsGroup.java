package org.roda.wui.client.common.actions;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionsGroup<T extends IsIndexed> {
  private final ActionsTitle title;
  private List<ActionsButton<T>> buttons = new ArrayList<>();

  public ActionsGroup(String title) {
    this.title = new ActionsTitle(title);
  }

  public ActionsGroup() {
    this.title = new ActionsTitle(null);
  }

  // TODO: remove extraCssClasses and generate them based on other info
  public ActionsGroup addButton(String text, Actionable.Action<T> action, Actionable.ActionImpact impact,
    String... extraCssClasses) {
    ActionsButton<T> button = new ActionsButton<>(text, action, impact, extraCssClasses);
    buttons.add(button);
    return this;
  }

  public ActionsTitle getTitle() {
    return title;
  }

  public List<ActionsButton<T>> getButtons() {
    return this.buttons;
  }
}
