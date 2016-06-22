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
import java.util.Optional;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

public interface PluginOrchestrator {

  public void setup();

  public void shutdown();

  public <T extends IsIndexed> void runPluginFromIndex(Object context, Class<T> classToActOn, Filter filter,
    Plugin<T> plugin);

  public void runPluginOnAIPs(Object context, Plugin<AIP> plugin, List<String> uuids, boolean retrieveFromModel);

  public void runPluginOnRepresentations(Object context, Plugin<Representation> plugin, List<String> uuids);

  public void runPluginOnFiles(Object context, Plugin<File> plugin, List<String> uuids);

  public void runPluginOnAllAIPs(Object context, Plugin<AIP> plugin);

  public void runPluginOnAllRepresentations(Object context, Plugin<Representation> plugin);

  public void runPluginOnAllFiles(Object context, Plugin<File> plugin);

  public void runPluginOnTransferredResources(Object context, Plugin<TransferredResource> plugin, List<String> uuids);

  public <T extends Serializable> void runPlugin(Object context, Plugin<T> plugin);

  /*
   * Job related methods
   * _________________________________________________________________________________________________________________
   */
  /** 201603 hsilva: this method should be async */
  public void executeJob(Job job) throws JobAlreadyStartedException;

  public void stopJob(Job job);

  public void startJobsInTheStateCreated();

  public void cleanUnfinishedJobs();

  // FIXME 20160602 hsilva: rename this (in akka case this will receive the
  // ActorRef for the actor holding JobInfo
  public void setInitialJobStateInfo(String jobId, Object object);

  public <T extends Serializable> void updateJobInformation(Plugin<T> plugin, JobPluginInfo jobPluginInfo)
    throws JobException;

  public <T extends Serializable> void updateJobState(Plugin<T> plugin, JOB_STATE state, Optional<String> stateDetails);

}
