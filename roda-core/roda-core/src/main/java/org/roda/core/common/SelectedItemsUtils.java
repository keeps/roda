/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.IsRODAObject;

public class SelectedItemsUtils {

  private SelectedItemsUtils() {
    // do nothing
  }

  @SuppressWarnings("unchecked")
  public static <T extends IsRODAObject> Class<T> parseClass(String classNameToReturn) throws GenericException {
    Class<T> classToReturn;
    try {
      classToReturn = (Class<T>) Class.forName(classNameToReturn);
    } catch (ClassNotFoundException e) {
      throw new GenericException("Could not find class " + classNameToReturn);
    }
    return classToReturn;
  }
}
