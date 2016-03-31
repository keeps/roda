/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index;

import java.util.List;

public class SelectedItemsList implements SelectedItems {

  private static final long serialVersionUID = -5364779540199737165L;

  private List<String> ids;

  public SelectedItemsList() {
    super();
  }

  public SelectedItemsList(List<String> ids) {
    super();
    this.ids = ids;
  }

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
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
    return "SelectedItemsList [ids=" + ids + "]";
  }

}
