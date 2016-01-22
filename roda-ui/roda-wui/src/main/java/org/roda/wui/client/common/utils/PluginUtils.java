/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.roda.core.data.v2.jobs.PluginInfo;

public class PluginUtils {

  public static void sortByName(List<PluginInfo> list) {
    Collections.sort(list, new Comparator<PluginInfo>() {

      @Override
      public int compare(PluginInfo o1, PluginInfo o2) {
        int ret;
        String name1 = o1.getName();
        String name2 = o2.getName();
        if (name1 != null && name2 != null) {
          ret = name1.compareTo(name2);
        } else {
          ret = 0;
        }
        return ret;
      }
    });
  }

}
