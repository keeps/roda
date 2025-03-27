package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("ChildOfFilterParameter")
@Schema(description = "This parser wraps a query that matches some parent documents and returns the children of those documents.")
public class ChildOfFilterParameter extends FilterParameter {

  @Serial
  private static final long serialVersionUID = 1936516004093585937L;

  @Schema(description = "The parameter must be a query string to use as a Block Mask, typically a query that matches the set of all possible parent documents")
  private FilterParameter childrenOfFilter;
  @Schema(description = "The inner subordinate query string must be a query that will match some parent documents")
  private FilterParameter parentFilter;

  public ChildOfFilterParameter() {
    // empty constructor
  }

  public ChildOfFilterParameter(FilterParameter parentFilter, FilterParameter childrenFilter) {
    this.parentFilter = parentFilter;
    this.childrenOfFilter = childrenFilter;
  }

  public FilterParameter getParentFilter() {
    return parentFilter;
  }

  public void setParentFilter(FilterParameter parentFilter) {
    this.parentFilter = parentFilter;
  }

  public FilterParameter getChildrenOfFilter() {
    return childrenOfFilter;
  }

  public void setChildrenOfFilter(FilterParameter childrenOfFilter) {
    this.childrenOfFilter = childrenOfFilter;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass())
      return false;
    if (!super.equals(object))
      return false;
    ChildOfFilterParameter that = (ChildOfFilterParameter) object;
    return Objects.equals(parentFilter, that.parentFilter) && Objects.equals(childrenOfFilter, that.childrenOfFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), parentFilter, childrenOfFilter);
  }
}
