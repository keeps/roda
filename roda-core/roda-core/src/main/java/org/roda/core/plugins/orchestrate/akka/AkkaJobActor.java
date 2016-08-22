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
import org.roda.core.data.v2.index.SelectedItemsAll;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.index.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
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

  private SupervisorStrategy strategy = new OneForOneStrategy(false, DeciderBuilder.matchAny(e -> {
    LOGGER.error("A child actor of {} has thrown an exception", AkkaJobActor.class.getSimpleName(), e);
    for (ActorRef actorRef : getContext().getChildren()) {
      actorRef.tell(new Messages.JobStateUpdated(null, JOB_STATE.FAILED_TO_COMPLETE, e), ActorRef.noSender());
    }
    return SupervisorStrategy.resume();
  }).build());

  /** Public empty constructor */
  public AkkaJobActor() {
    super();
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
      ActorRef jobStateInfoActor = getContext().actorOf(
        Props.create(AkkaJobStateInfoActor.class, plugin, getSender(), JobsHelper.getNumberOfJobsWorkers()), jobId);
      super.getPluginOrchestrator().setJobContextInformation(jobId, jobStateInfoActor);

      jobStateInfoActor.tell(new Messages.JobStateUpdated(plugin, JOB_STATE.STARTED), getSelf());

      try {
        if (job.getSourceObjects() instanceof SelectedItemsAll<?>) {
          runOnAll(job, plugin, jobStateInfoActor);
        } else if (job.getSourceObjects() instanceof SelectedItemsNone<?>) {
          super.getPluginOrchestrator().runPlugin(jobStateInfoActor, plugin);
        } else if (job.getSourceObjects() instanceof SelectedItemsList<?>) {
          runFromList(job, plugin, jobStateInfoActor);
        } else if (job.getSourceObjects() instanceof SelectedItemsFilter<?>) {
          runFromFilter(job, plugin, jobStateInfoActor);
        }
      } catch (Exception e) {
        LOGGER.error("Error while invoking orchestration method", e);
        jobStateInfoActor.tell(new Messages.JobStateUpdated(plugin, JOB_STATE.FAILED_TO_COMPLETE, e), getSelf());
        getSender().tell("Failed to complete", getSelf());
      }

    } else {
      LOGGER.error("Received a message that it doesn't know how to process ({})...", msg.getClass().getName());
      unhandled(msg);
    }
  }

  private <T extends IsRODAObject> void runOnAll(Job job, Plugin<T> plugin, ActorRef jobStateInfoActor)
    throws GenericException {
    // get class
    Class<IsRODAObject> sourceObjectsClass = JobsHelper
      .getSelectedClassFromString(job.getSourceObjects().getSelectedClass());

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllObjects(jobStateInfoActor, plugin,
      (Class<T>) sourceObjectsClass);
  }

  private void runFromList(Job job, Plugin<?> plugin, ActorRef jobStateInfoActor) throws GenericException {
    // get class
    Class<IsRODAObject> sourceObjectsClass = JobsHelper
      .getSelectedClassFromString(job.getSourceObjects().getSelectedClass());

    if (AIP.class.getName().equals(sourceObjectsClass.getName())) {
      super.getPluginOrchestrator().runPluginOnAIPs(jobStateInfoActor, (Plugin<AIP>) plugin,
        ((SelectedItemsList<IsRODAObject>) job.getSourceObjects()).getIds(), true);

    } else if (IndexedAIP.class.getName().equals(sourceObjectsClass.getName())) {
      super.getPluginOrchestrator().runPluginOnAIPs(jobStateInfoActor, (Plugin<AIP>) plugin,
        ((SelectedItemsList<IsRODAObject>) job.getSourceObjects()).getIds(), false);

    } else if (IndexedRepresentation.class.getName().equals(sourceObjectsClass.getName())) {
      super.getPluginOrchestrator().runPluginOnRepresentations(jobStateInfoActor, (Plugin<Representation>) plugin,
        ((SelectedItemsList<IndexedRepresentation>) job.getSourceObjects()).getIds());

    } else if (IndexedFile.class.getName().equals(sourceObjectsClass.getName())) {
      super.getPluginOrchestrator().runPluginOnFiles(jobStateInfoActor, (Plugin<File>) plugin,
        ((SelectedItemsList<IndexedFile>) job.getSourceObjects()).getIds());

    } else if (TransferredResource.class.getName().equals(sourceObjectsClass.getName())) {
      super.getPluginOrchestrator().runPluginOnTransferredResources(jobStateInfoActor,
        (Plugin<TransferredResource>) plugin,
        ((SelectedItemsList<TransferredResource>) job.getSourceObjects()).getIds());
    } else {
      LOGGER.error("Error executing job on unknown source objects class '{}'", sourceObjectsClass.getName());
      throw new GenericException(
        "Error executing job on unknown source objects class '" + sourceObjectsClass.getName() + "'");
    }
  }

  private void runFromFilter(Job job, Plugin<?> plugin, ActorRef jobStateInfoActor)
    throws GenericException, RequestNotValidException {
    // cast
    SelectedItemsFilter<?> selectedItems = (SelectedItemsFilter<?>) job.getSourceObjects();

    // get class
    Class<IsIndexed> sourceObjectsClass = JobsHelper
      .getIsIndexedSelectedClassFromString(selectedItems.getSelectedClass());

    // count objects & update job stats
    Long objectsCount = super.getIndex().count(sourceObjectsClass, selectedItems.getFilter());
    JobsHelper.updateJobObjectsCount(plugin, super.getModel(), objectsCount);

    // execute
    super.getPluginOrchestrator().runPluginFromIndex(jobStateInfoActor, sourceObjectsClass, selectedItems.getFilter(),
      (Plugin) plugin);
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }

}