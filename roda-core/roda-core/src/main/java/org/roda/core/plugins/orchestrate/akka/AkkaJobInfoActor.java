/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import org.roda.core.RodaCoreFactory;
import org.roda.core.plugins.orchestrate.JobInfo;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class AkkaJobInfoActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobInfoActor.class);
  private JobInfo jobInfo;

  public AkkaJobInfoActor() {
    jobInfo = new JobInfo();
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Messages.JobInfoMessage) {
      Messages.JobInfoMessage message = (Messages.JobInfoMessage) msg;
      jobInfo.put(message.plugin, message.jobPluginInfo);
      JobPluginInfo infoUpdated = message.jobPluginInfo.processJobPluginInformation(message.plugin, jobInfo);
      jobInfo.setObjectsCount(infoUpdated.getSourceObjectsCount());
      PluginHelper.updateJobInformation(message.plugin, RodaCoreFactory.getModelService(), infoUpdated);
    } else if (msg instanceof Messages.JobIsDone) {
      Messages.JobIsDone message = (Messages.JobIsDone) msg;
      PluginHelper.updateJobState(message.plugin, RodaCoreFactory.getModelService(), message.state);
      getContext().stop(getSelf());
    } else {
      LOGGER.error(AkkaJobInfoActor.class.getName() + " received a message that it doesn't know how to process...");
    }
  }

}