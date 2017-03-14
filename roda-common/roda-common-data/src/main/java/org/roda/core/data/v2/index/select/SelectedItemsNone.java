/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.select;

import org.roda.core.data.v2.IsRODAObject;

public class SelectedItemsNone<T extends IsRODAObject> implements SelectedItems<T> {

  private static final long serialVersionUID = -5364779540199737165L;

  private static SelectedItemsNone selectedItemsNone = new SelectedItemsNone<>();

  public SelectedItemsNone() {
    super();
  }

  @Override
  public String getSelectedClass() {
    return "";
  }

  public static <T extends IsRODAObject> SelectedItemsNone<T> create() {
    return selectedItemsNone;
  }

}
