package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("ParentWhichFilterParameter")
@Schema(description = "This parser takes a query that matches child documents and returns their parents.")
public class ParentWhichFilterParameter extends FilterParameter {

  @Serial
  private static final long serialVersionUID = 1936516004093585937L;

  @Schema(description = "The parameter must be a query string to use as a Block Mask, typically a query that matches the set of all possible parent documents")
  private FilterParameter parentFilter;
  @Schema(description = "The inner subordinate query must be a query that will match some child documents")
  private FilterParameter childrenFilter;

  public ParentWhichFilterParameter() {
    // empty constructor
  }

  public ParentWhichFilterParameter(FilterParameter parentFilter, FilterParameter childrenFilter) {
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
    ParentWhichFilterParameter that = (ParentWhichFilterParameter) object;
    return Objects.equals(parentFilter, that.parentFilter) && Objects.equals(childrenFilter, that.childrenFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), parentFilter, childrenFilter);
  }
}
