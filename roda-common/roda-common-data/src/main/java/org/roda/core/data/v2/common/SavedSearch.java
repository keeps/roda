package org.roda.core.data.v2.common;

import org.roda.core.data.v2.index.filter.Filter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Miguel Guimarãese <mguimaraes@keep.pt>
 */
public class SavedSearch implements Serializable {
  private static final long serialVersionUID = -1930846417465568093L;

  private String searchClassName;
  private String title;
  private Filter filter;

  public SavedSearch() {
    super();
  }

  public String getSearchClassName() {
    return searchClassName;
  }

  public void setSearchClassName(String searchClassName) {
    this.searchClassName = searchClassName;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SavedSearch that = (SavedSearch) o;
    return Objects.equals(searchClassName, that.searchClassName) && Objects.equals(title, that.title)
        && Objects.equals(filter, that.filter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(searchClassName, title, filter);
  }

  @Override
  public String toString() {
    return "SavedSearch{" + "searchClassName='" + searchClassName + '\'' + ", title='" + title + '\'' + ", filter="
        + filter + '}';
  }
}
