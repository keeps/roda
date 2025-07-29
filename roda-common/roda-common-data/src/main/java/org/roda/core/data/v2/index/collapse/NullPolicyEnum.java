/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
