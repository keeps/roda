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
}
