/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index;

import java.util.List;

import org.roda.core.data.v2.IsRODAObject;

public interface IsIndexed extends IsRODAObject {

  String getUUID();

  /**
   * Return CSV header names for this object.
   * 
   * @return a {@link List} of {@link String} with the header names.
   */
  List<String> toCsvHeaders();

  /**
   * Return CSV values for this object.
   *
   * @return a {@link List} of {@link Object} with the CSV values.
   */
  List<Object> toCsvValues();

  /**
   * Return the fields to create lite
   *
   * @return a {@link List} of {@link String} with the fields.
   */
  List<String> liteFields();

}
