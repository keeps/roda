/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.ip.IndexedDIP;

public class IndexedDIPUtils {
  private IndexedDIPUtils() {
    // do nothing
  }

  public static String interpolateOpenExternalURL(IndexedDIP dip, String locale) {
    Map<String, String> replacements = new HashMap<>();
    replacements.put("locale", locale);
    return interpolateOpenExternalURL(dip, replacements);
  }

  private static String interpolateOpenExternalURL(IndexedDIP dip, Map<String, String> replacements) {
    String url = dip.getOpenExternalURL();

    for (Map.Entry<String, String> replacement : replacements.entrySet()) {
      String find = "{{" + replacement.getKey() + "}}";
      String replace = replacement.getValue();
      url = url.replace(find, replace);
    }

    return url;
  }
}
