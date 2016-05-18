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
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
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
      Job job = (Job) msg;
      Plugin<?> plugin = (Plugin<?>) RodaCoreFactory.getPluginManager().getPlugin(job.getPlugin());
      PluginHelper.setPluginParameters(plugin, job);

      PluginHelper.updateJobPercentage(plugin, 0);

      if (ORCHESTRATOR_METHOD.ON_TRANSFERRED_RESOURCES == job.getOrchestratorMethod()) {
        executeJobOnTransferredResources(job, plugin);
      } else if (ORCHESTRATOR_METHOD.ON_ALL_AIPS == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs((Plugin<AIP>) plugin);
      } else if (ORCHESTRATOR_METHOD.ON_AIPS == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs((Plugin<AIP>) plugin,
          JobsHelper.getAIPs((SelectedItems<IndexedAIP>) job.getObjects()));
      } else if (ORCHESTRATOR_METHOD.ON_REPRESENTATIONS == job.getOrchestratorMethod()) {
        if (job.getObjects() instanceof SelectedItemsFilter) {
          SelectedItemsFilter<IndexedRepresentation> selectedItems = (SelectedItemsFilter<IndexedRepresentation>) job
            .getObjects();

          Long objectsCount = RodaCoreFactory.getIndexService().count(IndexedRepresentation.class,
            selectedItems.getFilter());
          PluginHelper.updateJobObjectsCount(plugin, RodaCoreFactory.getModelService(), objectsCount);

          RodaCoreFactory.getPluginOrchestrator().runPluginFromIndex(IndexedRepresentation.class,
            selectedItems.getFilter(), (Plugin<IndexedRepresentation>) plugin);
        } else {
          RodaCoreFactory.getPluginOrchestrator().runPluginOnRepresentations((Plugin<Representation>) plugin,
            ((SelectedItemsList<IndexedRepresentation>) job.getObjects()).getIds());
        }
      } else if (ORCHESTRATOR_METHOD.ON_FILES == job.getOrchestratorMethod()) {
        if (job.getObjects() instanceof SelectedItemsFilter) {
          SelectedItemsFilter<IndexedFile> selectedItems = (SelectedItemsFilter<IndexedFile>) job.getObjects();

          Long objectsCount = RodaCoreFactory.getIndexService().count(IndexedFile.class, selectedItems.getFilter());
          PluginHelper.updateJobObjectsCount(plugin, RodaCoreFactory.getModelService(), objectsCount);

          RodaCoreFactory.getPluginOrchestrator().runPluginFromIndex(IndexedFile.class, selectedItems.getFilter(),
            (Plugin<IndexedFile>) plugin);
        } else {
          RodaCoreFactory.getPluginOrchestrator().runPluginOnFiles((Plugin<File>) plugin,
            ((SelectedItemsList<IndexedFile>) job.getObjects()).getIds());
        }
      } else if (ORCHESTRATOR_METHOD.RUN_PLUGIN == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPlugin(plugin);
      }

    } else {
      LOGGER.error(AkkaJobWorkerActor.class.getName() + " received a message that it doesn't know how to process...");
    }
  }

  private void executeJobOnTransferredResources(Job job, Plugin<?> plugin)
    throws GenericException, RequestNotValidException {
    if (job.getObjects() instanceof SelectedItemsFilter) {
      // cast
      SelectedItemsFilter<TransferredResource> selectedItems = (SelectedItemsFilter<TransferredResource>) job
        .getObjects();

      // count objects & update job stats
      Long objectsCount = RodaCoreFactory.getIndexService().count(TransferredResource.class, selectedItems.getFilter());
      PluginHelper.updateJobObjectsCount(plugin, RodaCoreFactory.getModelService(), objectsCount);

      // execute
      RodaCoreFactory.getPluginOrchestrator().runPluginFromIndex(TransferredResource.class, selectedItems.getFilter(),
        (Plugin<TransferredResource>) plugin);
    } else {
      // execute
      RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources((Plugin<TransferredResource>) plugin,
        JobsHelper.getTransferredResourcesFromObjectIds((SelectedItems<TransferredResource>) job.getObjects()));
    }
  }

}