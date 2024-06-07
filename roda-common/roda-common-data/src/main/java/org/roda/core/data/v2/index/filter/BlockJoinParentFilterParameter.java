package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("BlockJoinParentFilterParameter")
public class BlockJoinParentFilterParameter extends FilterParameter {

  @Serial
  private static final long serialVersionUID = 1936516004093585937L;

  private FilterParameter blockMask;
  private FilterParameter someChildren;

  public BlockJoinParentFilterParameter() {
    // empty constructor
  }

  public BlockJoinParentFilterParameter(FilterParameter blockMask, FilterParameter someChildren) {
    this.blockMask = blockMask;
    this.someChildren = someChildren;
  }

  public FilterParameter getBlockMask() {
    return blockMask;
  }

  public void setBlockMask(FilterParameter blockMask) {
    this.blockMask = blockMask;
  }

  public FilterParameter getSomeChildren() {
    return someChildren;
  }

  public void setSomeChildren(FilterParameter someChildren) {
    this.someChildren = someChildren;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || getClass() != object.getClass())
      return false;
    if (!super.equals(object))
      return false;
    BlockJoinParentFilterParameter that = (BlockJoinParentFilterParameter) object;
    return Objects.equals(blockMask, that.blockMask) && Objects.equals(someChildren, that.someChildren);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), blockMask, someChildren);
  }
}
