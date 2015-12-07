/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.util.Date;
import java.util.Map;

import org.roda.core.data.Attribute;
import org.roda.core.data.Report;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.Job;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.w3c.util.DateParser;

public final class PluginUtils {

  private PluginUtils() {
  }

  public static Report instantiatePluginReport(String pluginName, String pluginVersion) {
    Report report = new Report();
    report.setType(Report.TYPE_PLUGIN_REPORT);
    report.setTitle("Report of plugin " + pluginName);

    report.addAttribute(new Attribute("Agent name", pluginName))
      .addAttribute(new Attribute("Agent version", pluginVersion))
      .addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));

    return report;
  }

  public static String getJobId(Map<String, String> pluginParameters) {
    return pluginParameters.get(RodaConstants.PLUGIN_PARAMS_JOB_ID);
  }

  public static Job getJobFromIndex(IndexService index, Map<String, String> pluginParameters)
    throws IndexServiceException, NotFoundException {
    return index.retrieve(Job.class, getJobId(pluginParameters));
  }
  //
  // public static List<AIP> getJobAIPs(IndexService index, Map<String, String>
  // pluginParameters)
  // throws IndexServiceException, NotFoundException {
  // // FIXME this should be a constant and the maximum number of objects sent
  // // to a plugin
  // int maxAips = 200;
  // IndexResult<AIP> aipsFromIndex = index.find(AIP.class,
  // new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID,
  // new ArrayList<>(getJobFromIndex(index,
  // pluginParameters).getObjectIdsToAipIds().values()))),
  // null, new Sublist(0, maxAips));
  // return aipsFromIndex.getResults();
  //
  // }
}
