/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.collapse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public enum NullPolicyEnum {
  IGNORE("ignore"), EXPAND("expand"), COLLAPSE("collapse");

  private final String value;

  NullPolicyEnum(String value) {
    this.value = value;
  }

  @JsonValue
  @Override
  public String toString() {
    return value;
  }

  @JsonCreator
  public static NullPolicyEnum fromString(String key) {
    if (key == null) {
      return null;
    }
    for (NullPolicyEnum e : NullPolicyEnum.values()) {
      if (e.name().equalsIgnoreCase(key)) {
        return e;
      }
    }
    throw new IllegalArgumentException("Unknown NullPolicyEnum value: " + key);
  }
}