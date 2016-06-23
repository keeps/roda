/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobInfo;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class AkkaJobStateInfoActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobStateInfoActor.class);

  private JobInfo jobInfo;
  private Plugin<?> plugin;

  public AkkaJobStateInfoActor(Plugin<?> plugin) {
    jobInfo = new JobInfo();
    this.plugin = plugin;
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Messages.JobInfoUpdated) {
      handleJobInfoUpdated(msg);
    } else if (msg instanceof Messages.PluginExecuteIsDone) {
      handleExecuteIsDone(msg);
    } else if (msg instanceof Messages.PluginAfterAllExecuteIsReady) {
      handleAfterAllExecuteIsReady();
    } else if (msg instanceof Messages.JobStateUpdated) {
      handleJobStateUpdated(msg);
    } else {
      LOGGER.error("Received a message that it doesn't know how to process (" + msg.getClass().getName() + ")...");
      unhandled(msg);
    }
  }

  private void handleJobInfoUpdated(Object msg) {
    Messages.JobInfoUpdated message = (Messages.JobInfoUpdated) msg;
    jobInfo.put(message.plugin, message.jobPluginInfo);
    JobPluginInfo infoUpdated = message.jobPluginInfo.processJobPluginInformation(message.plugin, jobInfo);
    jobInfo.setObjectsCount(infoUpdated.getSourceObjectsCount());
    PluginHelper.updateJobInformation(message.plugin, RodaCoreFactory.getModelService(), infoUpdated);
  }

  private void handleExecuteIsDone(Object msg) {
    Messages.PluginExecuteIsDone message = (Messages.PluginExecuteIsDone) msg;
    JobPluginInfo jobPluginInfo = jobInfo.getJobInfo().get(message.getPlugin());
    jobPluginInfo.setDone(true);
    jobInfo.put(message.getPlugin(), jobPluginInfo);
    try {
      message.getPlugin().afterBlockExecute(RodaCoreFactory.getIndexService(), RodaCoreFactory.getModelService(),
        RodaCoreFactory.getStorageService());
    } catch (Exception e) {
      LOGGER.error("Error executing plugin.afterBlockExecute", e);
    }

    if (jobInfo.isDone()) {
      getSelf().tell(new Messages.PluginAfterAllExecuteIsReady(message.getPlugin()), getSelf());
    }
  }

  private void handleAfterAllExecuteIsReady() {
    try {
      plugin.afterAllExecute(RodaCoreFactory.getIndexService(), RodaCoreFactory.getModelService(),
        RodaCoreFactory.getStorageService());
      getSelf().tell(new Messages.JobStateUpdated(plugin, JOB_STATE.COMPLETED), getSelf());
    } catch (Exception e) {
      LOGGER.error("Error executing plugin.afterAllExecute", e);
      getSelf().tell(new Messages.JobStateUpdated(plugin, JOB_STATE.FAILED_TO_COMPLETE, e), getSelf());
    }
  }

  private void handleJobStateUpdated(Object msg) {
    Messages.JobStateUpdated message = (Messages.JobStateUpdated) msg;
    Plugin<?> p = message.getPlugin() == null ? plugin : message.getPlugin();
    PluginHelper.updateJobState(p, RodaCoreFactory.getModelService(), message.getState(), message.getStateDatails());
    if (message.getState() == JOB_STATE.COMPLETED) {
      getContext().stop(getSelf());
    }
  }

}