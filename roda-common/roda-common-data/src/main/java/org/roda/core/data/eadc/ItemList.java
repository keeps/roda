/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class ItemList implements EadCValue, Serializable {

  private static final long serialVersionUID = -8711549208729618897L;

  private String[] items = null;

  /**
   * Constructs a new empty {@link ItemList}
   * */
  public ItemList() {

  }

  /**
   * Constructs a new {@link ItemList} using the provided parameters
   * 
   * @param items
   */
  public ItemList(String[] items) {
    this.items = items;
  }

  public String[] getItems() {
    return items;
  }

  public void setItems(String[] items) {
    this.items = items;
  }

  @Override
  public String toString() {
    return "ItemList [items=" + Arrays.toString(items) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(items);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ItemList)) {
      return false;
    }
    ItemList other = (ItemList) obj;
    if (!Arrays.equals(items, other.items)) {
      return false;
    }
    return true;
  }

}
