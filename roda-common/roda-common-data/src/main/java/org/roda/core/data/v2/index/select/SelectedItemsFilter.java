/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.select;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;

public class SelectedItemsFilter<T extends IsIndexed> implements SelectedItems<T> {

  private static final long serialVersionUID = 975693329806484985L;

  private Filter filter;
  private String selectedClass;
  private Boolean justActive;

  public SelectedItemsFilter() {
    super();
  }

  public SelectedItemsFilter(Filter filter, String selectedClass, Boolean justActive) {
    super();
    this.filter = filter;
    this.selectedClass = selectedClass;
    this.justActive = justActive;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  @Override
  public String getSelectedClass() {
    return selectedClass;
  }

  public void setSelectedClass(String selectedClass) {
    this.selectedClass = selectedClass;
  }

  public Boolean justActive() {
    return justActive;
  }

  public void setJustActive(Boolean justActive) {
    this.justActive = justActive;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((filter == null) ? 0 : filter.hashCode());
    result = prime * result + ((justActive == null) ? 0 : justActive.hashCode());
    result = prime * result + ((selectedClass == null) ? 0 : selectedClass.hashCode());
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
    SelectedItemsFilter<?> other = (SelectedItemsFilter<?>) obj;
    if (filter == null) {
      if (other.filter != null)
        return false;
    } else if (!filter.equals(other.filter))
      return false;
    if (justActive == null) {
      if (other.justActive != null)
        return false;
    } else if (!justActive.equals(other.justActive))
      return false;
    if (selectedClass == null) {
      if (other.selectedClass != null)
        return false;
    } else if (!selectedClass.equals(other.selectedClass))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "SelectedItemsFilter [filter=" + filter + ", selectedClass=" + selectedClass + ", justActive=" + justActive
      + "]";
  }

}
