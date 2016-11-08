/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

public interface IsModelObject extends IsRODAObject {
  /**
   * 20161102 hsilva: a <code>@JsonIgnore</code> should be added to avoid
   * serializing
   */
  public int getClassVersion();
}
