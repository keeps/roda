/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;

/**
 * One string based "lite" representation of a RODA object. Great for keeping in
 * memory for long periods of time (e.g. orchestration messages)
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class LiteRODAObject implements Serializable {
  private static final long serialVersionUID = 9111571847100077796L;

  private String value;

  public LiteRODAObject(String value) {
    this.value = value;
  }

  public String getInfo() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "LiteRODAObject [value=" + value + "]";
  }

}
