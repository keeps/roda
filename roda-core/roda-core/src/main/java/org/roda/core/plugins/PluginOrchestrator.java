/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.util.List;

import org.roda.core.common.pekko.messages.jobs.JobPartialUpdate;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

/**
 * Plugin Orchestrator interface
 *
 * <p>
 * <b>NOTE:</b> methods are synchronous unless stated otherwise by method name
 * or specific parameter
 * </p>
 */
public interface PluginOrchestrator {

  public void setup();

  public void shutdown();

  public <T extends IsRODAObject, T1 extends IsIndexed> void runPluginFromIndex(Object context, Job job,
    Class<T1> classToActOn, Filter filter, Boolean justActive, Plugin<T> plugin);

  public <T extends IsRODAObject> void runPluginOnObjects(Object context, Job job, Plugin<T> plugin,
    Class<T> objectClass, List<String> uuids);

  public <T extends IsRODAObject> void runPluginOnAllObjects(Object context, Plugin<T> plugin, Job job,
    Class<T> objectClass);

  public <T extends IsRODAObject> void runPlugin(Object context, Plugin<T> plugin, Job job);

  /*
   * Job related methods
   * _________________________________________________________________________________________________________________
   */
  /** 201603 hsilva: only tests should invoke this method synchronously */
  public void executeJob(Job job, boolean async) throws JobAlreadyStartedException;

  public void createAndExecuteJobs(Job job, boolean async) throws JobAlreadyStartedException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  /** 201712 hsilva: this method was known as stopJob */
  public void stopJobAsync(Job job);

  /** 201712 hsilva: this method was known as cleanUnfinishedJobs */
  public void cleanUnfinishedJobsAsync();

  public void setJobContextInformation(String jobId, Object object);

  public void setJobInError(String jobId);

  /** 201712 hsilva: this method was known as updateJobInformation */
  public <T extends IsRODAObject> void updateJobInformationAsync(Plugin<T> plugin, JobPluginInfo jobPluginInfo)
    throws JobException;

  /** 201712 hsilva: this method was known as updateJob */
  public <T extends IsRODAObject> void updateJobAsync(Plugin<T> plugin, JobPartialUpdate partialUpdate);

  public void acquireObjectLock(List<String> lites, int timeoutInSeconds, boolean waitForLockIfLocked,
    String requestUuid) throws LockingException;

  public void releaseObjectLockAsync(List<String> lites, String requestUuid);

  /** 20181029 hsilva: only tests should invoke this method */
  public void releaseAllObjectLocksAsync();

}
