package org.roda.core.data.v2.index.collapse;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public enum HintEnum {

  NONE("none"), TOP_FC("top_fc"), BLOCK("block");

  private String value;

  HintEnum(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
