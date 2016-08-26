package org.roda.core.data.v2.common;

import java.util.List;

import org.roda.core.data.v2.IsRODAObject;

public interface RODAObjectList<T extends IsRODAObject> {

  public List<T> getObjects();

  public void setObjects(List<T> objects);

  public void addObject(T object);
}
