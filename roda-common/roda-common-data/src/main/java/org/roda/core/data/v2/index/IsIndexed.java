/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index;

import org.roda.core.data.v2.IsRODAObject;

public interface IsIndexed extends IsRODAObject {

  String getUUID();

  /**
   * Return the CSV header names for this object.
   * 
   * @return an array of String with the header names.
   */
  String[] toCsvHeaders();

  /**
   * Return the CSV values for this object.
   *
   * @return an array of Object with the CSV values.
   */
  Object[] toCsvValues();

}
