/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.common;

import java.util.List;

import org.roda.core.data.v2.IsRODAObject;

public interface RODAObjectList<T extends IsRODAObject> {

  public List<T> getObjects();

  public void setObjects(List<T> objects);

  public void addObject(T object);
}
