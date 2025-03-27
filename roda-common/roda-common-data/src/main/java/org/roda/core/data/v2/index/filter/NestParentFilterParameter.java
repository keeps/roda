package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("NestParentFilterParameter")
public class NestParentFilterParameter extends FilterParameter {

  @Serial
  private static final long serialVersionUID = 1936516004093585937L;

  private FilterParameter parentFilter;
  private FilterParameter childrenFilter;

  public NestParentFilterParameter() {
    // empty constructor
  }

  public NestParentFilterParameter(FilterParameter parentFilter, FilterParameter childrenFilter) {
    this.parentFilter = parentFilter;
    this.childrenFilter = childrenFilter;
  }

  public FilterParameter getParentFilter() {
    return parentFilter;
  }

  public void setParentFilter(FilterParameter parentFilter) {
    this.parentFilter = parentFilter;
  }

  public FilterParameter getChildrenFilter() {
    return childrenFilter;
  }

  public void setChildrenFilter(FilterParameter childrenFilter) {
    this.childrenFilter = childrenFilter;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass())
      return false;
    if (!super.equals(object))
      return false;
    NestParentFilterParameter that = (NestParentFilterParameter) object;
    return Objects.equals(parentFilter, that.parentFilter) && Objects.equals(childrenFilter, that.childrenFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), parentFilter, childrenFilter);
  }
}
