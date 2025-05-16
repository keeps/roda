/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.pekko;

import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.pekko.Messages;
import org.roda.core.common.pekko.PekkoBaseActor;
import org.roda.core.common.pekko.messages.plugins.PluginAfterAllExecuteIsReady;
import org.roda.core.common.pekko.messages.plugins.PluginExecuteIsReady;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.transaction.RODATransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PekkoWorkerActor extends PekkoBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(PekkoWorkerActor.class);

  private final IndexService index;
  private final ModelService model;
  private RODATransactionManager RODATransactionManager;

  public PekkoWorkerActor() {
    super();
    this.model = getModel();
    this.index = getIndex();
    this.RODATransactionManager = getStorageTransactionManager();
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    super.setup(msg);
    if (msg instanceof PluginExecuteIsReady) {
      handlePluginExecuteIsReady(msg);
    } else if (msg instanceof PluginAfterAllExecuteIsReady) {
      handlePluginAfterAllExecuteIsReady(msg);
    } else {
      LOGGER.error("Received a message that it doesn't know how to process ({})...", msg.getClass().getName());
      unhandled(msg);
    }
  }

  private void handlePluginExecuteIsReady(Object msg) {
    PluginExecuteIsReady message = (PluginExecuteIsReady) msg;
    List<LiteOptionalWithCause> objectsToBeProcessed = message.getList();
    message.logProcessingStarted();
    Plugin<IsRODAObject> plugin = message.getPlugin();

    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(RodaCoreFactory.getNodeType());

    try {
      if (writeIsAllowed && RODATransactionManager != null) {
        RODATransactionManager.runPluginInTransaction(plugin, objectsToBeProcessed);
      } else {
        plugin.execute(index, model, objectsToBeProcessed);
      }
      getSender().tell(Messages.newPluginExecuteIsDone(plugin, false).withParallelism(message.getParallelism())
        .withJobPriority(message.getJobPriority()), getSelf());
    } catch (Throwable e) {
      // 20170120 hsilva: it is required to catch Throwable as there are some
      // linking errors that only will happen during the execution (e.g.
      // java.lang.NoSuchMethodError)
      LOGGER.error("Error executing plugin.execute()", e);
      getSender().tell(Messages.newPluginExecuteIsDone(plugin, true, getErrorMessage(e))
        .withParallelism(message.getParallelism()).withJobPriority(message.getJobPriority()), getSelf());
    }

    message.logProcessingEnded();
  }

  private String getErrorMessage(Throwable e) {
    StringBuilder ret = new StringBuilder();
    ret.append("An exception has occurred. Exception '").append(e.getClass().getName()).append("' with message '")
      .append(e.getMessage()).append("'");
    if (e.getCause() != null) {
      ret.append(" [inner exception '").append(e.getCause().getClass().getName()).append("' with message '")
        .append(e.getCause().getMessage()).append("']");
    }
    return ret.toString();
  }

  private void handlePluginAfterAllExecuteIsReady(Object msg) {
    PluginAfterAllExecuteIsReady message = (PluginAfterAllExecuteIsReady) msg;
    message.logProcessingStarted();
    Plugin<?> plugin = message.getPlugin();
    try {
      Job job = PluginHelper.getJob(plugin, model);
      JobParallelism parallelism = job.getParallelism();
      JobPriority priority = job.getPriority();
      try {
        plugin.afterAllExecute(index, model);
        getSender().tell(
          Messages.newPluginAfterAllExecuteIsDone(plugin, false).withJobPriority(priority).withParallelism(parallelism),
          getSelf());
      } catch (Throwable e) {
        // 20170120 hsilva: it is required to catch Throwable as there are some
        // linking errors that only will happen during the execution (e.g.
        // java.lang.NoSuchMethodError)
        LOGGER.error("Error executing plugin.afterAllExecute()", e);
        getSender().tell(
          Messages.newPluginAfterAllExecuteIsDone(plugin, true).withJobPriority(priority).withParallelism(parallelism),
          getSelf());
      }
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.warn("Unable to get Job from model. Reason: {}", e.getMessage());
    }

    message.logProcessingEnded();
  }

}
