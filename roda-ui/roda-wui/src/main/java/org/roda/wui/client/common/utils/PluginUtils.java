/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class PluginUtils {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void sortByName(List<PluginInfo> list) {
    Collections.sort(list, new Comparator<PluginInfo>() {

      @Override
      public int compare(PluginInfo o1, PluginInfo o2) {
        int ret;

        if (o1 != null && o2 != null) {
          String name1 = o1.getName();
          String name2 = o2.getName();
          if (name1 != null && name2 != null) {
            ret = name1.compareTo(name2);
          } else {
            ret = 0;
          }
        } else {
          ret = o1 == o2 ? 0 : 1;
        }
        return ret;
      }
    });
  }

  public static List<PluginType> getPluginTypesWithoutIngest() {
    List<PluginType> types = new ArrayList<>(Arrays.asList(PluginType.values()));
    types.remove(PluginType.INGEST);
    return types;
  }

}
