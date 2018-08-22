/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.ArrayList;
import java.util.Optional;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobInfo;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Iterables;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.routing.RoundRobinPool;

public class AkkaJobStateInfoActor extends AkkaBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobStateInfoActor.class);

  private JobInfo jobInfo;
  private Plugin<?> plugin;
  private ActorRef jobCreator;
  private ActorRef jobsManager;
  private ActorRef workersRouter;
  boolean stopping = false;
  boolean errorDuringBeforeAll = false;
  private String jobId;

  // metrics
  // private Map<String, Histogram> stateMessagesMetrics;
  private Histogram stateMessagesMetricsHistogram;

  public AkkaJobStateInfoActor(Plugin<?> plugin, ActorRef jobCreator, ActorRef jobsManager, String jobId,
    int numberOfJobsWorkers) {
    super();
    jobInfo = new JobInfo();
    this.plugin = plugin;
    this.jobCreator = jobCreator;
    this.jobsManager = jobsManager;
    this.jobId = jobId;

    LOGGER.debug("Starting AkkaJobStateInfoActor router with {} actors", numberOfJobsWorkers);
    Props workersProps = new RoundRobinPool(numberOfJobsWorkers).props(Props.create(AkkaWorkerActor.class));
    workersRouter = getContext().actorOf(workersProps, "WorkersRouter");
    // 20160914 hsilva: watch child events, so when they stop we can react
    getContext().watch(workersRouter);

    JobsHelper.createJobWorkingDirectory(jobId);

    String className = AkkaJobStateInfoActor.class.getSimpleName();
    // stateMessagesMetrics = new HashMap<>();
    stateMessagesMetricsHistogram = getMetricRegistry()
      .histogram(MetricRegistry.name(className, "msgCreationToProcessingStartedInMilis"));
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    super.setup(msg);
    if (msg instanceof Messages.JobStateUpdated) {
      handleJobStateUpdated((Messages.JobStateUpdated) msg);
    } else if (msg instanceof Messages.JobStateDetailsUpdated) {
      handleJobStateDetailsUpdated((Messages.JobStateDetailsUpdated) msg);
    } else if (msg instanceof Messages.JobSourceObjectsUpdated) {
      handleJobSourceObjectsUpdated(msg);
    } else if (msg instanceof Messages.JobInfoUpdated) {
      handleJobInfoUpdated(msg);
    } else if (msg instanceof Messages.JobStop) {
      handleJobStop(msg);
    } else if (msg instanceof Terminated) {
      handleTerminated(msg);
    } else if (msg instanceof Messages.PluginExecuteIsReady) {
      handleExecuteIsReady(msg);
    } else if (msg instanceof Messages.JobInitEnded) {
      handleJobInitEnded(msg);
    } else if (msg instanceof Messages.PluginBeforeAllExecuteIsReady) {
      handleBeforeAllExecuteIsReady(msg);
    } else if (msg instanceof Messages.PluginExecuteIsDone) {
      handleExecuteIsDone(msg);
    } else if (msg instanceof Messages.PluginAfterAllExecuteIsDone) {
      handleAfterAllExecuteIsDone(msg);
    } else if (msg instanceof Messages.JobCleanup) {
      handleJobCleanup(msg);
    } else {
      LOGGER.error("Received a message that don't know how to process ({})...", msg.getClass().getName());
      unhandled(msg);
    }
  }

  private void handleJobStateUpdated(Messages.JobStateUpdated message) {
    markMessageProcessingAsStarted(message);
    Plugin<?> p = message.getPlugin() == null ? this.plugin : message.getPlugin();
    try {
      Job job = PluginHelper.getJob(p, getIndex());
      LOGGER.info("Setting job '{}' ({}) state to {}. Details: {}", job.getName(), job.getId(), message.getState(),
        message.getStateDatails().orElse("NO DETAILS"));
    } catch (NotFoundException | GenericException | RequestNotValidException e) {
      LOGGER.warn("Unable to get Job from index to log its state change. Reason: {}", e.getMessage());
    }
    JobsHelper.updateJobState(p, getModel(), message.getState(), message.getStateDatails());
    if (Job.isFinalState(message.getState())) {
      // 20160817 hsilva: the following instruction is needed for the "sync"
      // execution of a job (i.e. for testing purposes)
      jobCreator.tell("Done", getSelf());
      jobsManager.tell(new Messages.JobsManagerJobEnded(jobId, plugin.getClass().getName()), getSelf());
      JobsHelper.deleteJobWorkingDirectory(jobId);
      getContext().stop(getSelf());
    }
    markMessageProcessingAsEnded(message);
  }

  private void handleJobStateDetailsUpdated(Messages.JobStateDetailsUpdated message) {
    markMessageProcessingAsStarted(message);
    Plugin<?> p = message.getPlugin() == null ? this.plugin : message.getPlugin();
    JobsHelper.updateJobStateDetails(p, getModel(), message.getStateDatails());
    markMessageProcessingAsEnded(message);
  }

  private <T extends IsRODAObject> void handleJobSourceObjectsUpdated(Object msg) {
    Messages.JobSourceObjectsUpdated message = (Messages.JobSourceObjectsUpdated) msg;
    markMessageProcessingAsStarted(message);
    try {
      Job job = PluginHelper.getJob(plugin, getModel());
      SelectedItems<?> sourceObjects = job.getSourceObjects();
      if (sourceObjects instanceof SelectedItemsList) {
        SelectedItemsList<T> items = (SelectedItemsList<T>) sourceObjects;
        ArrayList<String> newIds = new ArrayList<>();
        for (String oldId : items.getIds()) {
          if (message.getOldToNewIds().get(oldId) != null) {
            newIds.add(message.getOldToNewIds().get(oldId));
          } else {
            newIds.add(oldId);
          }
        }
        items.setIds(newIds);
        getModel().createOrUpdateJob(job);
      }
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error while retrieving Job for doing an update", e);
    }

    markMessageProcessingAsEnded(message);
  }

  private void handleJobInfoUpdated(Object msg) {
    Messages.JobInfoUpdated message = (Messages.JobInfoUpdated) msg;
    markMessageProcessingAsStarted(message);
    jobInfo.put(message.getPlugin(), message.getJobPluginInfo());
    JobPluginInfo infoUpdated = message.getJobPluginInfo().processJobPluginInformation(message.getPlugin(), jobInfo);
    jobInfo.setObjectsCount(infoUpdated.getSourceObjectsCount());
    JobsHelper.updateJobInformation(message.getPlugin(), getModel(), infoUpdated);
    markMessageProcessingAsEnded(message);
  }

  private void handleJobStop(Object msg) {
    Messages.JobStop message = (Messages.JobStop) msg;
    markMessageProcessingAsStarted(message);
    getSelf().tell(new Messages.JobStateUpdated(plugin, JOB_STATE.STOPPING), getSelf());
    stopping = true;
    getContext().getChildren().forEach(e -> getContext().stop(e));
    markMessageProcessingAsEnded(message);
  }

  private void handleTerminated(Object msg) {
    LOGGER.trace("{} Started processing message {}", "NO_UUID", Terminated.class.getSimpleName());
    boolean allChildrenAreDead = true;
    if (stopping) {
      allChildrenAreDead = Iterables.isEmpty(getContext().getChildren());
      if (allChildrenAreDead) {
        getSelf().tell(new Messages.JobStateUpdated(plugin, JOB_STATE.STOPPED), getSelf());
      }
    }
    LOGGER.trace("{} Ended processing message {} (stopping={} allChildrenAreDead={})", "NO_UUID",
      Terminated.class.getSimpleName(), stopping, allChildrenAreDead);
  }

  private void handleExecuteIsReady(Object msg) {
    if (!errorDuringBeforeAll) {
      Messages.PluginExecuteIsReady message = (Messages.PluginExecuteIsReady) msg;
      markMessageProcessingAsStarted(message);
      jobInfo.setStarted(message.getPlugin());
      // 20160819 hsilva: the following it's just for debugging purposes
      message.setHasBeenForwarded();
      workersRouter.tell(message, getSelf());
      markMessageProcessingAsEnded(message);
    }
  }

  private void handleJobInitEnded(Object msg) {
    Messages.JobInitEnded message = (Messages.JobInitEnded) msg;
    markMessageProcessingAsStarted(message);
    jobInfo.setInitEnded(true);
    // INFO 20160630 hsilva: the following test is needed because messages can
    // be out of order and a plugin might already arrived to the end
    if (jobInfo.isDone()) {
      workersRouter.tell(new Messages.PluginAfterAllExecuteIsReady(plugin), getSelf());
    }
    markMessageProcessingAsEnded(message);
  }

  private void handleBeforeAllExecuteIsReady(Object msg) throws PluginException {
    Messages.PluginBeforeAllExecuteIsReady message = (Messages.PluginBeforeAllExecuteIsReady) msg;
    markMessageProcessingAsStarted(message);
    try {
      message.getPlugin().beforeAllExecute(getIndex(), getModel(), getStorage());
      // do nothing because if all goes good, the next messages are of type
      // PluginExecuteIsReady
    } catch (Throwable e) {
      // 20170120 hsilva: it is required to catch Throwable as there are some
      // linking errors that only will happen during the execution (e.g.
      // java.lang.NoSuchMethodError)
      errorDuringBeforeAll = true;
      getPluginOrchestrator().setJobInError(PluginHelper.getJobId(plugin));
      getSelf().tell(
        new Messages.JobStateUpdated(plugin, JOB_STATE.FAILED_TO_COMPLETE, Optional.ofNullable(e.getMessage())),
        getSelf());

    }
    markMessageProcessingAsEnded(message);
  }

  private void handleExecuteIsDone(Object msg) {
    Messages.PluginExecuteIsDone message = (Messages.PluginExecuteIsDone) msg;
    markMessageProcessingAsStarted(message);
    jobInfo.setDone(message.getPlugin(), message.isWithError());

    if (message.isWithError()) {
      getSelf().tell(new Messages.JobStateDetailsUpdated(plugin, Optional.of(message.getErrorMessage())), getSelf());
    }

    if (jobInfo.isDone() && jobInfo.isInitEnded()) {
      if (jobInfo.atLeastOneErrorOccurred()) {
        getSelf().tell(new Messages.JobStateUpdated(plugin, JOB_STATE.FAILED_TO_COMPLETE), getSelf());
      } else {
        workersRouter.tell(new Messages.PluginAfterAllExecuteIsReady(plugin), getSelf());
      }
    }
    markMessageProcessingAsEnded(message);
  }

  private void handleAfterAllExecuteIsDone(Object msg) {
    Messages.PluginAfterAllExecuteIsDone message = (Messages.PluginAfterAllExecuteIsDone) msg;
    markMessageProcessingAsStarted(message);
    getSelf().tell(new Messages.JobCleanup(), getSelf());
    getSelf().tell(
      new Messages.JobStateUpdated(plugin, message.isWithError() ? JOB_STATE.FAILED_TO_COMPLETE : JOB_STATE.COMPLETED),
      getSelf());
    markMessageProcessingAsEnded(message);
  }

  private void handleJobCleanup(Object msg) {
    Messages.JobCleanup message = (Messages.JobCleanup) msg;
    markMessageProcessingAsStarted(message);
    try {
      LOGGER.info("Doing job cleanup");
      IndexService indexService = getIndex();
      Job job = PluginHelper.getJob(plugin, indexService);
      JobsHelper.cleanJobObjects(job, super.getModel(), indexService);
      LOGGER.info("Ended doing job cleanup");
    } catch (NotFoundException | GenericException | RequestNotValidException e) {
      LOGGER.error("Unable to get Job for doing cleanup", e);
    }
    markMessageProcessingAsEnded(message);
  }

  private void markMessageProcessingAsStarted(Messages.AbstractMessage message) {
    message.logProcessingStarted();
    stateMessagesMetricsHistogram.update(message.getTimeSinceCreation());
  }

  private void markMessageProcessingAsEnded(Messages.AbstractMessage message) {
    message.logProcessingEnded();
  }

}