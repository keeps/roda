/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.pekko;

import java.util.Optional;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.OneForOneStrategy;
import org.apache.pekko.actor.Props;
import org.apache.pekko.actor.SupervisorStrategy;
import org.apache.pekko.japi.pf.DeciderBuilder;
import org.roda.core.common.pekko.Messages;
import org.roda.core.common.pekko.PekkoBaseActor;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarãese <mguimaraes@keep.pt>
 */
public class PekkoLimitedJobActor extends PekkoBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(PekkoLimitedJobActor.class);

  private final ActorRef jobsManager;

  private final SupervisorStrategy strategy = new OneForOneStrategy(false, DeciderBuilder.matchAny(e -> {
    LOGGER.error("A child actor of {} has thrown an exception", PekkoLimitedJobActor.class.getSimpleName(), e);
    for (ActorRef actorRef : getContext().getChildren()) {
      actorRef.tell(Messages.newJobStateUpdated(null, Job.JOB_STATE.FAILED_TO_COMPLETE, e), ActorRef.noSender());
    }
    return SupervisorStrategy.resume();
  }).build());

  /** Public constructor */
  public PekkoLimitedJobActor(ActorRef jobsManager) {
    super();
    this.jobsManager = jobsManager;
  }

  @Override
  public void onReceive(Object msg) throws Exception {
    super.setup(msg);
    if (msg instanceof Job job) {
      Plugin<? extends IsRODAObject> plugin = super.getPluginManager().getPlugin(job.getPlugin());
      if (plugin == null) {
        JobsHelper.updateJobState(job, super.getModel(), Job.JOB_STATE.FAILED_TO_COMPLETE,
          Optional.of("Plugin is NULL"));
        // 20160818 hsilva: the following instruction is needed for the "sync"
        // execution of a job (i.e. for testing purposes)
        getSender().tell("Failed to complete", getSelf());
        return;
      }
      JobsHelper.setPluginParameters(plugin, job);

      String jobId = job.getId();
      JobPriority jobPriority = job.getPriority();
      JobParallelism jobParallelism = job.getParallelism();
      ActorRef jobStateInfoActor = getContext().actorOf(Props.create(PekkoJobStateInfoActor.class, plugin, getSender(),
        jobsManager, jobId, JobsHelper.getNumberOfJobsWorkers(), JobsHelper.getNumberOfLimitedJobsWorkers()), jobId);
      super.getPluginOrchestrator().setJobContextInformation(jobId, jobStateInfoActor);

      jobStateInfoActor.tell(Messages.newJobStateUpdated(plugin, Job.JOB_STATE.STARTED).withParallelism(jobParallelism)
        .withJobPriority(jobPriority), getSelf());

      try {
        if (job.getSourceObjects() instanceof SelectedItemsAll<?>) {
          runOnAll(job, plugin);
        } else if (job.getSourceObjects() instanceof SelectedItemsNone<?>) {
          super.getPluginOrchestrator().runPlugin(getSelf(), plugin, job);
        } else if (job.getSourceObjects() instanceof SelectedItemsList<?>) {
          runFromList(job, plugin);
        } else if (job.getSourceObjects() instanceof SelectedItemsFilter<?>) {
          runFromFilter(job, plugin);
        }
      } catch (Exception e) {
        LOGGER.error("Error while invoking orchestration method", e);
        jobStateInfoActor.tell(Messages.newJobStateUpdated(plugin, Job.JOB_STATE.FAILED_TO_COMPLETE, e)
          .withParallelism(jobParallelism).withJobPriority(jobPriority), getSelf());
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

    getPluginOrchestrator().runPluginOnAllObjects(getSelf(), plugin, job, (Class<T>) sourceObjectsClass);
  }

  private <T extends IsRODAObject> void runFromList(Job job, Plugin<T> plugin) throws GenericException {
    // get class
    Class<IsRODAObject> sourceObjectsClass = JobsHelper
      .getSelectedClassFromString(job.getSourceObjects().getSelectedClass());

    getPluginOrchestrator().runPluginOnObjects(getSelf(), job, plugin,
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
    getPluginOrchestrator().runPluginFromIndex(getSelf(), job, sourceObjectsClass, selectedItems.getFilter(),
      selectedItems.justActive(), plugin);
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }
}
