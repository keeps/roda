/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions.model;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.actions.Actionable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionableButton<T extends IsIndexed> {
  private String text;
  private Actionable.Action<T> action;
  private Actionable.ActionImpact impact;
  private List<String> extraCssClasses;

  public ActionableButton() {

  }

  public ActionableButton(String text, Actionable.Action<T> action, Actionable.ActionImpact impact,
    String... extraCssClasses) {
    this.text = text;
    this.action = action;
    this.impact = impact;
    this.extraCssClasses = Arrays.asList(extraCssClasses);
  }

  public String getText() {
    return text;
  }

  public Actionable.Action<T> getAction() {
    return action;
  }

  public Actionable.ActionImpact getImpact() {
    return impact;
  }

  public List<String> getExtraCssClasses() {
    return extraCssClasses;
  }

  @Deprecated
  public String getId() {
    // TODO 2018-08-21 bferreira: generate an ID based on the action (and maybe also
    // something else) to make the button configurable via .properties and in
    // theme.css
    return null;
  }
}
