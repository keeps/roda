/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.plugins.orchestrate.JobException;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

public interface PluginOrchestrator {

  public void setup();

  public void shutdown();

  public <T extends IsIndexed> void runPluginFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> plugin);

  public void runPluginOnAIPs(Plugin<AIP> plugin, List<String> uuids);

  public void runPluginOnRepresentations(Plugin<Representation> plugin, List<String> uuids);

  public void runPluginOnFiles(Plugin<File> plugin, List<String> uuids);

  public void runPluginOnAllAIPs(Plugin<AIP> plugin);

  public void runPluginOnAllRepresentations(Plugin<Representation> plugin);

  public void runPluginOnAllFiles(Plugin<File> plugin);

  public void runPluginOnTransferredResources(Plugin<TransferredResource> plugin, List<String> uuids);

  public <T extends Serializable> void runPlugin(Plugin<T> plugin);

  /*
   * Job related methods
   * _________________________________________________________________________________________________________________
   */
  /** 201603 hsilva: this method should be async */
  public void executeJob(Job job) throws JobAlreadyStartedException;

  public void stopJob(Job job);

  public void startJobsInTheStateCreated();

  public void cleanUnfinishedJobs();

  public <T extends Serializable> void updateJobInformation(Plugin<T> plugin, JobPluginInfo jobPluginInfo)
    throws JobException;

  public <T extends Serializable> void updateJobPercentage(Plugin<T> plugin, int percentage);

}
