package org.roda.wui.client.common.actions.model;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionableBundle<T extends IsIndexed> {
  private List<ActionableGroup<T>> groups = new ArrayList<>();

  public ActionableBundle() {

  }

  public ActionableBundle<T> addGroup(ActionableGroup<T> group) {
    groups.add(group);
    return this;
  }

  public List<ActionableGroup<T>> getGroups() {
    return groups;
  }
}
