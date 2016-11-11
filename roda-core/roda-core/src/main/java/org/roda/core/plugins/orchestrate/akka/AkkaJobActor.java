/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class AkkaJobActor extends AkkaBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobActor.class);

  private ActorRef jobsManager;

  private SupervisorStrategy strategy = new OneForOneStrategy(false, DeciderBuilder.matchAny(e -> {
    LOGGER.error("A child actor of {} has thrown an exception", AkkaJobActor.class.getSimpleName(), e);
    for (ActorRef actorRef : getContext().getChildren()) {
      actorRef.tell(new Messages.JobStateUpdated(null, JOB_STATE.FAILED_TO_COMPLETE, e), ActorRef.noSender());
    }
    return SupervisorStrategy.resume();
  }).build());

  /** Public constructor */
  public AkkaJobActor(ActorRef jobsManager) {
    super();
    this.jobsManager = jobsManager;
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    super.setup(msg);
    if (msg instanceof Job) {
      Job job = (Job) msg;
      Plugin<? extends IsRODAObject> plugin = super.getPluginManager().getPlugin(job.getPlugin());
      if (plugin == null) {
        JobsHelper.updateJobState(job, super.getModel(), JOB_STATE.FAILED_TO_COMPLETE, Optional.of("Plugin is NULL"));
        // 20160818 hsilva: the following instruction is needed for the "sync"
        // execution of a job (i.e. for testing purposes)
        getSender().tell("Failed to complete", getSelf());
        return;
      }
      JobsHelper.setPluginParameters(plugin, job);

      String jobId = job.getId();
      ActorRef jobStateInfoActor = getContext().actorOf(Props.create(AkkaJobStateInfoActor.class, plugin, getSender(),
        jobsManager, jobId, JobsHelper.getNumberOfJobsWorkers()), jobId);
      super.getPluginOrchestrator().setJobContextInformation(jobId, jobStateInfoActor);

      jobStateInfoActor.tell(new Messages.JobStateUpdated(plugin, JOB_STATE.STARTED), getSelf());

      try {
        if (job.getSourceObjects() instanceof SelectedItemsAll<?>) {
          runOnAll(job, plugin);
        } else if (job.getSourceObjects() instanceof SelectedItemsNone<?>) {
          super.getPluginOrchestrator().runPlugin(getSelf(), plugin);
        } else if (job.getSourceObjects() instanceof SelectedItemsList<?>) {
          runFromList(job, plugin);
        } else if (job.getSourceObjects() instanceof SelectedItemsFilter<?>) {
          runFromFilter(job, plugin);
        }
      } catch (Exception e) {
        LOGGER.error("Error while invoking orchestration method", e);
        jobStateInfoActor.tell(new Messages.JobStateUpdated(plugin, JOB_STATE.FAILED_TO_COMPLETE, e), getSelf());
        getSender().tell("Failed to complete", getSelf());
      }

    } else {
      LOGGER.error("Received a message that don't know how to process ({})...", msg.getClass().getName());
      unhandled(msg);
    }
  }

  private <T extends IsRODAObject> void runOnAll(Job job, Plugin<T> plugin) throws GenericException {
    // get class
    Class<IsRODAObject> sourceObjectsClass = JobsHelper
      .getSelectedClassFromString(job.getSourceObjects().getSelectedClass());

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllObjects(getSelf(), plugin, (Class<T>) sourceObjectsClass);
  }

  private <T extends IsRODAObject> void runFromList(Job job, Plugin<T> plugin) throws GenericException {
    // get class
    Class<IsRODAObject> sourceObjectsClass = JobsHelper
      .getSelectedClassFromString(job.getSourceObjects().getSelectedClass());

    RodaCoreFactory.getPluginOrchestrator().runPluginOnObjects(getSelf(), plugin,
      (Class<T>) ModelUtils.giveRespectiveModelClass(sourceObjectsClass),
      ((SelectedItemsList<IsRODAObject>) job.getSourceObjects()).getIds());
  }

  private void runFromFilter(Job job, Plugin<?> plugin) throws GenericException, RequestNotValidException {
    // cast
    SelectedItemsFilter<?> selectedItems = (SelectedItemsFilter<?>) job.getSourceObjects();

    // get class
    Class<IsIndexed> sourceObjectsClass = JobsHelper
      .getIsIndexedSelectedClassFromString(selectedItems.getSelectedClass());

    // count objects & update job stats
    Long objectsCount = super.getIndex().count(sourceObjectsClass, selectedItems.getFilter());
    JobsHelper.updateJobObjectsCount(plugin, super.getModel(), objectsCount);

    // execute
    super.getPluginOrchestrator().runPluginFromIndex(getSelf(), sourceObjectsClass, selectedItems.getFilter(),
      (Plugin) plugin);
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }

}