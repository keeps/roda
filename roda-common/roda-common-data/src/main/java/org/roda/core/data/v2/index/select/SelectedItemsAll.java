/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.select;

import org.roda.core.data.v2.IsRODAObject;

public class SelectedItemsAll<T extends IsRODAObject> implements SelectedItems<T> {

  private static final long serialVersionUID = -5364779540199737165L;

  public static <T extends IsRODAObject> SelectedItemsAll<T> create(Class<T> classToCreate) {
    return new SelectedItemsAll<>(classToCreate.getName());
  }

  public static <T extends IsRODAObject> SelectedItemsAll<T> create(String classToCreate) {
    return new SelectedItemsAll<>(classToCreate);
  }

  private String selectedClass;

  public SelectedItemsAll() {
    super();
  }

  public SelectedItemsAll(String selectedClass) {
    super();
    this.selectedClass = selectedClass;
  }

  @Override
  public String getSelectedClass() {
    return selectedClass;
  }

  public void setSelectedClass(String selectedClass) {
    this.selectedClass = selectedClass;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((selectedClass == null) ? 0 : selectedClass.hashCode());
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
    if (!(obj instanceof SelectedItemsAll)) {
      return false;
    }
    SelectedItemsAll other = (SelectedItemsAll) obj;
    if (selectedClass == null) {
      if (other.selectedClass != null) {
        return false;
      }
    } else if (!selectedClass.equals(other.selectedClass)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SelectedItemsAll [selectedClass=" + selectedClass + "]";
  }

}
