/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

@FunctionalInterface
public interface RODAProcessingLogic<T extends IsRODAObject> {
  public void process(IndexService index, ModelService model, Report report, Job cachedJob, JobPluginInfo jobPluginInfo,
    Plugin<T> plugin) throws PluginException;
}
