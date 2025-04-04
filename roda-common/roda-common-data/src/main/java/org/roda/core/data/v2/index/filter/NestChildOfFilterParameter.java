package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("NestChildOfFilterParameter")
@Schema(description = "This parser wraps a query that matches some parent documents and returns the children of those documents.")
public class NestChildOfFilterParameter extends FilterParameter {

  @Serial
  private static final long serialVersionUID = 1936516004093585937L;

  private FilterParameter childrenFilter;
  private FilterParameter parentFilter;

  public NestChildOfFilterParameter() {
    // empty constructor
  }

  public NestChildOfFilterParameter(FilterParameter parentFilter, FilterParameter childrenFilter) {
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
    NestChildOfFilterParameter that = (NestChildOfFilterParameter) object;
    return Objects.equals(parentFilter, that.parentFilter) && Objects.equals(childrenFilter, that.childrenFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), parentFilter, childrenFilter);
  }
}
