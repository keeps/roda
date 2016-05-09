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
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
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
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;
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
            getTransferredResourcesFromObjectIds((SelectedItems<TransferredResource>) job.getObjects()));
        }
        // FIXME 20160404 hsilva: this should be done inside the orchestrator
        // method
        PluginHelper.updateJobPercentage(plugin, 100);
      } else if (ORCHESTRATOR_METHOD.ON_ALL_AIPS == job.getOrchestratorMethod()) {
        reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs((Plugin<AIP>) plugin);
        // FIXME 20160404 hsilva: this should be done inside the orchestrator
        // method
        PluginHelper.updateJobPercentage(plugin, 100);
      } else if (ORCHESTRATOR_METHOD.ON_AIPS == job.getOrchestratorMethod()) {
        reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs((Plugin<AIP>) plugin,
          getAIPs((SelectedItems<IndexedAIP>) job.getObjects()));
        // FIXME 20160404 hsilva: this should be done inside the orchestrator
        // method
        PluginHelper.updateJobPercentage(plugin, 100);
      } else if (ORCHESTRATOR_METHOD.ON_REPRESENTATIONS == job.getOrchestratorMethod()) {
        Pair<String, List<String>> representations = getRepresentations(
          (SelectedItems<IndexedRepresentation>) job.getObjects());
        reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnRepresentations((Plugin<Representation>) plugin,
          representations.getFirst(), representations.getSecond());
        // FIXME 20160404 hsilva: this should be done inside the orchestrator
        // method
        PluginHelper.updateJobPercentage(plugin, 100);
      } else if (ORCHESTRATOR_METHOD.ON_FILES == job.getOrchestratorMethod()) {
        Pair<Pair<String, String>, List<String>> files = getFiles((SelectedItems<IndexedFile>) job.getObjects());
        reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnFiles((Plugin<File>) plugin,
          files.getFirst().getFirst(), files.getFirst().getSecond(), files.getSecond());
        // FIXME 20160404 hsilva: this should be done inside the orchestrator
        // method
        PluginHelper.updateJobPercentage(plugin, 100);
      } else if (ORCHESTRATOR_METHOD.RUN_PLUGIN == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPlugin(plugin);
      }

      getSender().tell(reports, getSelf());
    }
  }

  public List<TransferredResource> getTransferredResourcesFromObjectIds(
    SelectedItems<TransferredResource> selectedItems) {
    List<TransferredResource> res = new ArrayList<TransferredResource>();
    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<TransferredResource> list = (SelectedItemsList<TransferredResource>) selectedItems;
      for (String objectId : list.getIds()) {
        try {
          res.add(RodaCoreFactory.getIndexService().retrieve(TransferredResource.class, objectId));
        } catch (GenericException | NotFoundException e) {
          LOGGER.error("Error retrieving TransferredResource", e);
        }
      }
    } else {
      LOGGER.error("Still not implemented!!!!!!!!");
    }
    return res;
  }

  public List<String> getAIPs(SelectedItems<IndexedAIP> selectedItems) {
    List<String> res = new ArrayList<String>();
    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<IndexedAIP> list = (SelectedItemsList<IndexedAIP>) selectedItems;
      res.addAll(list.getIds());
    } else {
      try {
        IndexService index = RodaCoreFactory.getIndexService();
        SelectedItemsFilter<IndexedAIP> selectedItemsFilter = (SelectedItemsFilter<IndexedAIP>) selectedItems;
        long count = index.count(IndexedAIP.class, selectedItemsFilter.getFilter());
        for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
          List<IndexedAIP> aips = index.find(IndexedAIP.class, selectedItemsFilter.getFilter(), null,
            new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
          for (IndexedAIP aip : aips) {
            res.add(aip.getId());
          }
        }
      } catch (Throwable e) {
        LOGGER.error("Error while retrieving AIPs from index", e);
      }
    }
    return res;
  }

  public Pair<String, List<String>> getRepresentations(SelectedItems<IndexedRepresentation> selectedItems) {
    Pair<String, List<String>> resultPair = new Pair<String, List<String>>();
    try {

      String aipId = null;
      List<String> res = new ArrayList<String>();

      if (selectedItems instanceof SelectedItemsList) {
        SelectedItemsList<IndexedRepresentation> list = (SelectedItemsList<IndexedRepresentation>) selectedItems;

        if (list.getIds().size() > 0) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_UUID, list.getIds()));
          List<IndexedRepresentation> reps = RodaCoreFactory.getIndexService()
            .find(IndexedRepresentation.class, filter, null, new Sublist(0, list.getIds().size())).getResults();
          for (IndexedRepresentation rep : reps) {
            res.add(rep.getId());
            aipId = rep.getAipId();
          }
        }
      } else {
        IndexService index = RodaCoreFactory.getIndexService();
        SelectedItemsFilter<IndexedRepresentation> selectedItemsFilter = (SelectedItemsFilter<IndexedRepresentation>) selectedItems;
        long count = index.count(IndexedRepresentation.class, selectedItemsFilter.getFilter());
        for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
          List<IndexedRepresentation> reps = index.find(IndexedRepresentation.class, selectedItemsFilter.getFilter(),
            null, new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
          for (IndexedRepresentation rep : reps) {
            res.add(rep.getId());
            aipId = rep.getAipId();
          }
        }
      }

      resultPair.setFirst(aipId);
      resultPair.setSecond(res);
    } catch (Throwable e) {
      LOGGER.error("Error while retrieving Representations from index", e);
    }

    return resultPair;
  }

  public Pair<Pair<String, String>, List<String>> getFiles(SelectedItems<IndexedFile> selectedItems) {
    Pair<Pair<String, String>, List<String>> resultPair = new Pair<Pair<String, String>, List<String>>();
    try {

      String aipId = null;
      String representationId = null;
      List<String> res = new ArrayList<String>();

      if (selectedItems instanceof SelectedItemsList) {
        SelectedItemsList<IndexedFile> list = (SelectedItemsList<IndexedFile>) selectedItems;

        if (list.getIds().size() > 0) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_UUID, list.getIds()));
          List<IndexedFile> files = RodaCoreFactory.getIndexService()
            .find(IndexedFile.class, filter, null, new Sublist(0, list.getIds().size())).getResults();
          for (IndexedFile file : files) {
            res.add(file.getId());
            aipId = file.getAipId();
            representationId = file.getRepresentationId();
          }
        }
      } else {
        IndexService index = RodaCoreFactory.getIndexService();
        SelectedItemsFilter<IndexedFile> selectedItemsFilter = (SelectedItemsFilter<IndexedFile>) selectedItems;
        long count = index.count(IndexedFile.class, selectedItemsFilter.getFilter());
        for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
          List<IndexedFile> files = index.find(IndexedFile.class, selectedItemsFilter.getFilter(), null,
            new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
          for (IndexedFile file : files) {
            res.add(file.getId());
            aipId = file.getAipId();
            representationId = file.getRepresentationId();
          }
        }
      }

      Pair<String, String> fileInfo = new Pair<String, String>(aipId, representationId);
      resultPair.setFirst(fileInfo);
      resultPair.setSecond(res);
    } catch (Throwable e) {
      LOGGER.error("Error while retrieving Representations from index", e);
    }

    return resultPair;
  }

}