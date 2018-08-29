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
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.storage.StorageService;

public interface RODAObjectProcessingLogic<T extends IsRODAObject> extends RODAObjectProcessingLogicNew<T> {
  
  /**
   * @deprecated use {@link #process(IndexService, ModelService, StorageService, Report, Job, JobPluginInfo, Plugin, IsRODAObject)} instead.
   */
  @Deprecated
  public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
    SimpleJobPluginInfo jobPluginInfo, Plugin<T> plugin, T object);

  public default void process(IndexService index, ModelService model, StorageService storage, Report report,
    Job cachedJob, JobPluginInfo jobPluginInfo, Plugin<T> plugin, T object) {
    process(index, model, storage, report, cachedJob, (SimpleJobPluginInfo) jobPluginInfo, plugin, object);
  }

}
