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

public class AkkaJobInfoActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobInfoActor.class);

  private JobInfo jobInfo;
  private Plugin<?> plugin;

  public AkkaJobInfoActor(Plugin<?> plugin) {
    jobInfo = new JobInfo();
    this.plugin = plugin;
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Messages.JobInfoUpdated) {
      Messages.JobInfoUpdated message = (Messages.JobInfoUpdated) msg;
      boolean isJobPluginDone = message.jobPluginInfo.isDone();
      jobInfo.put(message.plugin, message.jobPluginInfo);
      JobPluginInfo infoUpdated = message.jobPluginInfo.processJobPluginInformation(message.plugin, jobInfo);
      jobInfo.setObjectsCount(infoUpdated.getSourceObjectsCount());
      PluginHelper.updateJobInformation(message.plugin, RodaCoreFactory.getModelService(), infoUpdated);
      if (isJobPluginDone) {
        message.plugin.afterBlockExecute(RodaCoreFactory.getIndexService(), RodaCoreFactory.getModelService(),
          RodaCoreFactory.getStorageService());
        if (jobInfo.isDone()) {
          plugin.afterAllExecute(RodaCoreFactory.getIndexService(), RodaCoreFactory.getModelService(),
            RodaCoreFactory.getStorageService());
          PluginHelper.updateJobState(message.plugin, RodaCoreFactory.getModelService(), JOB_STATE.COMPLETED);
        }
      }
    } else if (msg instanceof Messages.JobStateUpdated) {
      Messages.JobStateUpdated message = (Messages.JobStateUpdated) msg;
      PluginHelper.updateJobState(message.plugin, RodaCoreFactory.getModelService(), message.state);
      if (message.state == JOB_STATE.COMPLETED) {
        getContext().stop(getSelf());
      }
    } else {
      LOGGER.error(AkkaJobInfoActor.class.getName() + " received a message that it doesn't know how to process...");
    }
  }

}