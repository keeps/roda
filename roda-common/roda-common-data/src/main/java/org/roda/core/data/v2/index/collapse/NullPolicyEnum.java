package org.roda.core.data.v2.index.collapse;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public enum NullPolicyEnum {
  IGNORE("ignore"), EXPAND("expand"), COLLAPSE("collapse");

  private final String value;

  NullPolicyEnum(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
