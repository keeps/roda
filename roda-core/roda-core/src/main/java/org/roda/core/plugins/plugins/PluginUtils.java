/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.NotFoundException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Job;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.roda.core.model.AIP;

public final class PluginUtils {

  private PluginUtils() {
  }

  public static String getJobId(Map<String, String> pluginParameters) {
    return pluginParameters.get(RodaConstants.PLUGIN_PARAMS_JOB_ID);
  }

  public static Job getJobFromIndex(IndexService index, Map<String, String> pluginParameters)
    throws IndexServiceException, NotFoundException {
    return index.retrieve(Job.class, getJobId(pluginParameters));
  }

  public static List<AIP> getJobAIPs(IndexService index, Map<String, String> pluginParameters)
    throws IndexServiceException, NotFoundException {
    // FIXME this should be a constant and the maximum number of objects sent
    // to a plugin
    int maxAips = 200;
    IndexResult<AIP> aipsFromIndex = index.find(AIP.class,
      new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID,
        new ArrayList<>(getJobFromIndex(index, pluginParameters).getObjectIdsToAipIds().values()))),
      null, new Sublist(0, maxAips));
    return aipsFromIndex.getResults();

  }
}
