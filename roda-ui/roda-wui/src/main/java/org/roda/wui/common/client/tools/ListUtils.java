/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

  private ListUtils() {
    // do nothing
  }

  @SafeVarargs
  public static <T> List<T> concat(List<T> list, T... items) {
    List<T> ret = new ArrayList<>(list);
    for (T item : items) {
      ret.add(item);
    }
    return ret;
  }

  public static <T> List<T> concat(List<T> list, List<T> list2) {
    List<T> ret = new ArrayList<>(list);
    ret.addAll(list2);
    return ret;
  }

}
