package org.roda.wui.client.common.actions.model;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.common.actions.Actionable;

import com.google.gwt.user.client.Random;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionsButton<T extends IsIndexed> {
  private String text;
  private Actionable.Action<T> action;
  private Actionable.ActionImpact impact;
  private List<String> extraCssClasses;

  public ActionsButton() {

  }

  public ActionsButton(String text, Actionable.Action<T> action, Actionable.ActionImpact impact,
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

  public String getId() {
    // TODO: generate an ID based on the action and i18n key
    return String.valueOf(Random.nextInt(100000));
  }
}
