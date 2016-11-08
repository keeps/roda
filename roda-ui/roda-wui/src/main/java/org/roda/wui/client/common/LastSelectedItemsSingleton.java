/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.v2.index.select.SelectedItems;

public class LastSelectedItemsSingleton {

  private static LastSelectedItemsSingleton singleton = null;
  private static SelectedItems<?> selected = null;

  private LastSelectedItemsSingleton() {
  }

  public static LastSelectedItemsSingleton getInstance() {
    if (singleton == null) {
      singleton = new LastSelectedItemsSingleton();
    }
    return singleton;
  }

  public SelectedItems<?> getSelectedItems() {
    return selected;
  }

  public void setSelectedItems(SelectedItems<?> items) {
    selected = items;
  }

}
