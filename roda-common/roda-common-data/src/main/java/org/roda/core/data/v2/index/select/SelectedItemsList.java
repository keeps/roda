/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.IsRODAObject;

public class SelectedItemsList<T extends IsRODAObject> implements SelectedItems<T> {

  private static final long serialVersionUID = -5364779540199737165L;

  private List<String> ids;
  private String selectedClass;

  public SelectedItemsList() {
    super();
    ids = new ArrayList<>();
  }

  public SelectedItemsList(List<String> ids, String selectedClass) {
    super();
    this.ids = ids;
    this.selectedClass = selectedClass;
  }

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }

  @Override
  public String getSelectedClass() {
    return selectedClass;
  }

  public void setSelectedClass(String selectedClass) {
    this.selectedClass = selectedClass;
  }

  public static <T extends IsRODAObject> SelectedItemsList<T> create(Class<T> classToCreate, List<String> ids) {
    return new SelectedItemsList<>(ids, classToCreate.getName());
  }

  public static <T extends IsRODAObject> SelectedItemsList<T> create(String classNameToCreate, List<String> ids) {
    return new SelectedItemsList<>(ids, classNameToCreate);
  }

  public static <T extends IsRODAObject> SelectedItemsList<T> create(Class<T> classToCreate, String... ids) {
    return new SelectedItemsList<>(Arrays.asList(ids), classToCreate.getName());
  }

  public static <T extends IsRODAObject> SelectedItemsList<T> create(String classNameToCreate, String... ids) {
    return new SelectedItemsList<>(Arrays.asList(ids), classNameToCreate);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ids == null) ? 0 : ids.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SelectedItemsList other = (SelectedItemsList) obj;
    if (ids == null) {
      if (other.ids != null)
        return false;
    } else if (!ids.equals(other.ids))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "SelectedItemsList [ids=" + ids + ", selectedClass=" + selectedClass + "]";
  }

}
