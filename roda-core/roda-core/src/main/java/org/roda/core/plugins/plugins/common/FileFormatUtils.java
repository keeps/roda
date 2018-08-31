/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;

public class FileFormatUtils {
  private static final String CORE = "core";
  private static final String TOOLS = "tools";

  private FileFormatUtils() {
    // do nothing
  }

  public static Map<String, List<String>> getPronomToExtension(String tool) {
    Map<String, List<String>> map = new HashMap<>();
    String inputFormatPronoms = RodaCoreFactory.getRodaConfigurationAsString(CORE, TOOLS, tool, "inputFormatPronoms");

    if (StringUtils.isNotBlank(inputFormatPronoms)) {
      for (String pronom : Arrays.asList(inputFormatPronoms.split(" "))) {
        String pronomExtensions = RodaCoreFactory.getRodaConfigurationAsString(CORE, TOOLS, "pronom", pronom);
        if (StringUtils.isNotBlank(pronomExtensions)) {
          map.put(pronom, Arrays.asList(pronomExtensions.split(" ")));
        }
      }
    }

    return map;
  }

  public static Map<String, List<String>> getMimetypeToExtension(String tool) {
    Map<String, List<String>> map = new HashMap<>();
    String inputFormatMimetypes = RodaCoreFactory.getRodaConfigurationAsString(CORE, TOOLS, tool,
      "inputFormatMimetypes");

    if (StringUtils.isNotBlank(inputFormatMimetypes)) {
      for (String mimetype : Arrays.asList(inputFormatMimetypes.split(" "))) {
        String mimeExtensions = RodaCoreFactory.getRodaConfigurationAsString(CORE, TOOLS, "mimetype", mimetype);
        if (StringUtils.isNotBlank(mimeExtensions)) {
          map.put(mimetype, Arrays.asList(mimeExtensions.split(" ")));
        }
      }
    }

    return map;
  }

  public static List<String> getInputExtensions(String tool) {
    String inputFormatExtensions = RodaCoreFactory.getRodaConfigurationAsString(CORE, TOOLS, tool,
      "inputFormatExtensions");
    if (StringUtils.isNotBlank(inputFormatExtensions)) {
      return Arrays.asList(inputFormatExtensions.split(" "));
    } else {
      return Collections.emptyList();
    }
  }
}
