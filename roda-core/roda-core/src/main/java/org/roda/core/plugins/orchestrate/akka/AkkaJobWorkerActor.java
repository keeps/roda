/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class AkkaJobWorkerActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobWorkerActor.class);

  public AkkaJobWorkerActor() {

  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Job) {
      List<Report> reports = new ArrayList<>();
      Job job = (Job) msg;
      Plugin<?> plugin = (Plugin<?>) RodaCoreFactory.getPluginManager().getPlugin(job.getPlugin());
      PluginHelper.setPluginParameters(plugin, job);

      PluginHelper.updateJobPercentage(plugin, 0);

      if (ORCHESTRATOR_METHOD.ON_TRANSFERRED_RESOURCES == job.getOrchestratorMethod()) {
        if (job.getObjects() instanceof SelectedItemsFilter) {
          SelectedItemsFilter<TransferredResource> selectedItems = (SelectedItemsFilter<TransferredResource>) job
            .getObjects();

          Long objectsCount = RodaCoreFactory.getIndexService().count(TransferredResource.class,
            selectedItems.getFilter());
          PluginHelper.updateJobObjectsCount(plugin, RodaCoreFactory.getModelService(), objectsCount);

          reports = RodaCoreFactory.getPluginOrchestrator().runPluginFromIndex(TransferredResource.class,
            selectedItems.getFilter(), (Plugin<TransferredResource>) plugin);
        } else {
          reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(
            (Plugin<TransferredResource>) plugin,
            JobsHelper.getTransferredResourcesFromObjectIds((SelectedItems<TransferredResource>) job.getObjects()));
        }
      } else if (ORCHESTRATOR_METHOD.ON_ALL_AIPS == job.getOrchestratorMethod()) {
        reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs((Plugin<AIP>) plugin);
      } else if (ORCHESTRATOR_METHOD.ON_AIPS == job.getOrchestratorMethod()) {
        reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs((Plugin<AIP>) plugin,
          JobsHelper.getAIPs((SelectedItems<IndexedAIP>) job.getObjects()));
      } else if (ORCHESTRATOR_METHOD.ON_REPRESENTATIONS == job.getOrchestratorMethod()) {
        Pair<String, List<String>> representations = JobsHelper
          .getRepresentations((SelectedItems<IndexedRepresentation>) job.getObjects());
        reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnRepresentations((Plugin<Representation>) plugin,
          representations.getFirst(), representations.getSecond());
      } else if (ORCHESTRATOR_METHOD.ON_FILES == job.getOrchestratorMethod()) {
        Pair<Pair<String, String>, List<String>> files = JobsHelper
          .getFiles((SelectedItems<IndexedFile>) job.getObjects());
        reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnFiles((Plugin<File>) plugin,
          files.getFirst().getFirst(), files.getFirst().getSecond(), files.getSecond());
      } else if (ORCHESTRATOR_METHOD.RUN_PLUGIN == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPlugin(plugin);
      }

      getSender().tell(reports, getSelf());
    } else {
      LOGGER.error(AkkaJobWorkerActor.class.getName() + " received a message that it doesn't know how to process...");
    }
  }

}