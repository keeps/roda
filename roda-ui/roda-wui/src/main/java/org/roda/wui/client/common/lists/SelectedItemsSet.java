package org.roda.wui.client.common.lists;

import java.util.Set;

import org.roda.core.data.v2.index.IsIndexed;

public class SelectedItemsSet<T extends IsIndexed> implements SelectedItems<T> {

  private static final long serialVersionUID = -5364779540199737165L;

  private Set<T> set;

  public SelectedItemsSet() {
    super();
  }

  public SelectedItemsSet(Set<T> set) {
    super();
    this.set = set;
  }

  public Set<T> getSet() {
    return set;
  }

  public void setSet(Set<T> set) {
    this.set = set;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((set == null) ? 0 : set.hashCode());
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SelectedItemsSet other = (SelectedItemsSet) obj;
    if (set == null) {
      if (other.set != null)
        return false;
    } else if (!set.equals(other.set))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "SelectedItemsSet [set=" + set + "]";
  }

}
