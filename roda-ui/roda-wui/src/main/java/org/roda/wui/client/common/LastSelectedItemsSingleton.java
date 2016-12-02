/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.select.SelectedItems;

public class LastSelectedItemsSingleton {

  private static LastSelectedItemsSingleton singleton = null;
  private static SelectedItems<?> selected = null;
  private static String detailsMessage = "";
  private static List<String> lastHistory = new ArrayList<String>();

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

  public String getDetailsMessage() {
    return detailsMessage;
  }

  public void setDetailsMessage(String details) {
    detailsMessage = details;
  }

  public List<String> getLastHistory() {
    return lastHistory;
  }

  public void setLastHistory(List<String> lastHistory) {
    LastSelectedItemsSingleton.lastHistory = lastHistory;
  }

}
