package org.roda.wui.client.common.actions.model;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ActionableObject<T extends IsIndexed> {
  public enum ActionableObjectType {
    NONE, SINGLE, MULTIPLE
  }

  private final ActionableObjectType type;
  private final SelectedItems<T> objects;
  private final T object;
  private final Class<T> objectClass;

  public ActionableObject(SelectedItems<T> objects) {
    this.type = ActionableObjectType.MULTIPLE;
    this.object = null;
    this.objects = objects;
    this.objectClass = null;
  }

  public ActionableObject(T object) {
    this.type = ActionableObjectType.SINGLE;
    this.object = object;
    this.objects = null;
    this.objectClass = null;
  }

  public ActionableObject(Class<T> objectClass) {
    this.type = ActionableObjectType.NONE;
    this.object = null;
    this.objects = null;
    this.objectClass = objectClass;
  }

  public ActionableObjectType getType() {
    return type;
  }

  public SelectedItems<T> getObjects() {
    return objects;
  }

  public T getObject() {
    return object;
  }

  public Class<T> getObjectClass() {
    return objectClass;
  }
}
