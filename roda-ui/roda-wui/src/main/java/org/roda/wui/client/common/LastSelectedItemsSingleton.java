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

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;

public class LastSelectedItemsSingleton {

  private static LastSelectedItemsSingleton singleton = null;
  private SelectedItems<? extends IsIndexed> selected = null;
  private String detailsMessage = "";
  private List<String> lastHistory = new ArrayList<String>();
  private IsIndexed lastObject = null;

  private LastSelectedItemsSingleton() {
  }

  public static LastSelectedItemsSingleton getInstance() {
    if (singleton == null) {
      singleton = new LastSelectedItemsSingleton();
    }
    return singleton;
  }

  public SelectedItems<? extends IsIndexed> getSelectedItems() {
    return selected;
  }

  public void setSelectedItems(SelectedItems<? extends IsIndexed> selected) {
    this.selected = selected;
  }

  public String getDetailsMessage() {
    return detailsMessage;
  }

  public void setDetailsMessage(String detailsMessage) {
    this.detailsMessage = detailsMessage;
  }

  public List<String> getLastHistory() {
    return lastHistory;
  }

  public void setLastHistory(List<String> lastHistory) {
    this.lastHistory = lastHistory;
  }

  public IsIndexed getLastObject() {
    return this.lastObject;
  }

  public void setLastObject(IsIndexed lastObject) {
    this.lastObject = lastObject;
  }

}
