/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

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
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
import akka.japi.pf.DeciderBuilder;

public class AkkaJobActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobActor.class);

  private SupervisorStrategy strategy = new OneForOneStrategy(false, DeciderBuilder.matchAny(e -> {
    for (ActorRef actorRef : getContext().getChildren()) {
      actorRef.tell(new Messages.JobStateUpdated(null, JOB_STATE.FAILED_TO_COMPLETE, e), ActorRef.noSender());
    }
    return SupervisorStrategy.resume();
  }).build());

  /** Public empty constructor */
  public AkkaJobActor() {

  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Job) {
      Job job = (Job) msg;
      Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager().getPlugin(job.getPlugin());
      PluginHelper.setPluginParameters(plugin, job);

      String jobId = job.getId();
      ActorRef sender = getSender();
      ActorRef jobStateInfoActor = getContext().actorOf(Props.create(AkkaJobStateInfoActor.class, plugin, sender),
        jobId);
      RodaCoreFactory.getPluginOrchestrator().setJobContextInformation(jobId, jobStateInfoActor);

      jobStateInfoActor.tell(new Messages.JobStateUpdated(plugin, JOB_STATE.STARTED), getSelf());

      try {
        if (job.getSourceObjects() instanceof SelectedItemsAll<?>) {
          runOnAll(job, plugin, jobStateInfoActor);
        } else if (job.getSourceObjects() instanceof SelectedItemsNone<?>) {
          RodaCoreFactory.getPluginOrchestrator().runPlugin(jobStateInfoActor, plugin);
        } else if (job.getSourceObjects() instanceof SelectedItemsList<?>) {
          runFromList(job, plugin, jobStateInfoActor);
        } else if (job.getSourceObjects() instanceof SelectedItemsFilter<?>) {
          runFromFilter(job, plugin, jobStateInfoActor);
        }
      } catch (Exception e) {
        jobStateInfoActor.tell(new Messages.JobStateUpdated(plugin, JOB_STATE.FAILED_TO_COMPLETE, e), getSelf());
        getSelf().tell("Failed to complete", getSelf());
      }

    } else {
      LOGGER.error("Received a message that it doesn't know how to process (" + msg.getClass().getName() + ")...");
      unhandled(msg);
    }
  }

  private void runOnAll(Job job, Plugin<?> plugin, ActorRef jobStateInfoActor) throws GenericException {
    // get class
    Class<IsRODAObject> sourceObjectsClass = JobsHelper
      .getSelectedClassFromString(job.getSourceObjects().getSelectedClass());

    if (AIP.class.getName().equals(sourceObjectsClass.getName())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs(jobStateInfoActor, (Plugin<AIP>) plugin);
    } else if (Representation.class.getName().equals(sourceObjectsClass.getName())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(jobStateInfoActor,
        (Plugin<Representation>) plugin);
    } else if (File.class.getName().equals(sourceObjectsClass.getName())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnAllFiles(jobStateInfoActor, (Plugin<File>) plugin);
    } else {
      LOGGER.error("Error executing job on unknown source objects class '{}'", sourceObjectsClass.getName());
      throw new GenericException(
        "Error executing job on unknown source objects class '" + sourceObjectsClass.getName() + "'");
    }
  }

  private void runFromList(Job job, Plugin<?> plugin, ActorRef jobStateInfoActor) throws GenericException {
    // get class
    Class<IsRODAObject> sourceObjectsClass = JobsHelper
      .getSelectedClassFromString(job.getSourceObjects().getSelectedClass());

    if (AIP.class.getName().equals(sourceObjectsClass.getName())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(jobStateInfoActor, (Plugin<AIP>) plugin,
        ((SelectedItemsList<IsRODAObject>) job.getSourceObjects()).getIds(), true);

    } else if (IndexedAIP.class.getName().equals(sourceObjectsClass.getName())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(jobStateInfoActor, (Plugin<AIP>) plugin,
        ((SelectedItemsList<IsRODAObject>) job.getSourceObjects()).getIds(), false);

    } else if (IndexedRepresentation.class.getName().equals(sourceObjectsClass.getName())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnRepresentations(jobStateInfoActor,
        (Plugin<Representation>) plugin, ((SelectedItemsList<IndexedRepresentation>) job.getSourceObjects()).getIds());

    } else if (IndexedFile.class.getName().equals(sourceObjectsClass.getName())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnFiles(jobStateInfoActor, (Plugin<File>) plugin,
        ((SelectedItemsList<IndexedFile>) job.getSourceObjects()).getIds());

    } else if (TransferredResource.class.getName().equals(sourceObjectsClass.getName())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(jobStateInfoActor,
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
    Long objectsCount = RodaCoreFactory.getIndexService().count(sourceObjectsClass, selectedItems.getFilter());
    PluginHelper.updateJobObjectsCount(plugin, RodaCoreFactory.getModelService(), objectsCount);

    // execute
    RodaCoreFactory.getPluginOrchestrator().runPluginFromIndex(jobStateInfoActor, sourceObjectsClass,
      selectedItems.getFilter(), (Plugin) plugin);
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }

}