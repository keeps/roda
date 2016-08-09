/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

public class StringUtils {

  public static final boolean isNotBlank(String s) {
    return s != null && s.trim().length() > 0;
  }

  public static final boolean isBlank(String s) {
    return !isNotBlank(s);
  }

  // method to prettify method names
  public static String getPrettifiedActionMethod(String actionMethod) {
    String method = actionMethod.substring(0, 1).toUpperCase() + actionMethod.substring(1);
    method = method.replaceAll("([A-Z])", " $1").trim();
    return method.replaceAll("A I P", "AIP");
  }
}
