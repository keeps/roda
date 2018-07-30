package org.roda.wui.client.common.actions.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionsBundle<T extends IsIndexed> implements Iterable<ActionsGroup<T>> {
  private List<ActionsGroup<T>> groups = new ArrayList<>();

  public ActionsBundle() {

  }

  public ActionsBundle<T> addGroup(ActionsGroup<T> group) {
    groups.add(group);
    return this;
  }

  @Override
  public Iterator<ActionsGroup<T>> iterator() {
    return groups.iterator();
  }
}
