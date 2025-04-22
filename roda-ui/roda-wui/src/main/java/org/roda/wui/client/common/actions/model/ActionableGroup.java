/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions.model;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.actions.Actionable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionableGroup<T extends IsIndexed> {
  private final ActionableTitle title;
  private final String icon;
  private final List<ActionableButton<T>> buttons = new ArrayList<>();

  public ActionableGroup(String title, String icon) {
    this.title = new ActionableTitle(title);
    this.icon = icon;
  }

  public ActionableGroup(String title) {
    this.title = new ActionableTitle(title);
    this.icon = null;
  }

  public ActionableGroup() {
    this.title = new ActionableTitle(null);
    this.icon = null;
  }

  // TODO: remove extraCssClasses and generate them based on other info
  public ActionableGroup<T> addButton(String text, Actionable.Action<T> action, Actionable.ActionImpact impact,
    String... extraCssClasses) {
    buttons.add(new ActionableButton<>(text, action, impact, extraCssClasses));
    return this;
  }

  public ActionableTitle getTitle() {
    return title;
  }

  public String getIcon() {
    return icon;
  }

  public List<ActionableButton<T>> getButtons() {
    return this.buttons;
  }
}
