/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;

public class ListboxUtils {

  private ListboxUtils() {
    // do nothing
  }

  public static final void removeItemByValue(ListBox listbox, String value) {
    List<Integer> indexesToRemove = new ArrayList<>();
    // going from the end to the start so remove is easier
    for (int i = listbox.getItemCount() - 1; i >= 0; i--) {
      if (listbox.getValue(i).equals(value)) {
        indexesToRemove.add(i);
      }
    }

    for (Integer index : indexesToRemove) {
      listbox.removeItem(index);
    }
  }

  public static final int insertItemByAlphabeticOrder(ListBox listbox, String item, String value) {
    int indexToInsert = -1;
    for (int i = 0; i < listbox.getItemCount(); i++) {
      String itemText = listbox.getItemText(i);
      if (itemText.compareToIgnoreCase(item) > 0) {
        indexToInsert = i;
        break;
      }
    }

    if (indexToInsert < 0) {
      indexToInsert = listbox.getItemCount();
    }

    listbox.insertItem(item, value, indexToInsert);
    return indexToInsert;
  }

  public static final void copyValues(ListBox listBoxOrigin, ListBox listBoxDestination) {
    for (int i = 0; i < listBoxOrigin.getItemCount(); i++) {
      listBoxDestination.addItem(listBoxOrigin.getItemText(i), listBoxOrigin.getValue(i));
    }
  }

  public static void select(ListBox listBox, String field) {
    boolean found = false;
    for (int i = 0; i < listBox.getItemCount() && !found; i++) {
      if (listBox.getValue(i).equals(field)) {
        listBox.setSelectedIndex(i);
        found = true;
      }
    }
  }
}
