package org.roda.core.plugins.plugins.synchronization.bundle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreatePreservationAgentPackagePlugin extends CreateRodaEntityPackagePlugin<IndexedPreservationAgent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreatePreservationAgentPackagePlugin.class);

  @Override
  public String getName() {
    return "Create Preservation Agent Bundle";
  }

  @Override
  public String getVersionImpl() {
    return "1.0.0";
  }

  @Override
  protected String getEntity() {
    return "preservation_agent";
  }

  @Override
  protected String getEntityStoragePath() {
    return "preservation/agents";
  }

  @Override
  protected void createBundle(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    SelectedItems<?> sourceObjects = job.getSourceObjects();

    if (sourceObjects instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) sourceObjects).getFilter();
      try {
        int counter = index.count(IndexedPreservationAgent.class, filter).intValue();
        jobPluginInfo.setSourceObjectsCount(counter);
        ArrayList<String> idList = new ArrayList<>();

        IterableIndexResult<IndexedPreservationAgent> agents = index.findAll(IndexedPreservationAgent.class, filter,
          Arrays.asList(RodaConstants.INDEX_UUID));
        for (IndexedPreservationAgent agent : agents) {
          Report reportItem = PluginHelper.initPluginReportItem(this, agent.getId(), IndexedPreservationAgent.class);
          PreservationMetadata retrieveAgent = null;
          try {
            retrieveAgent = model.retrievePreservationMetadata(agent.getId(),
              PreservationMetadata.PreservationMetadataType.AGENT);
            createAgentBundle(model, retrieveAgent);
            idList.add(retrieveAgent.getId());
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error on create bundle for preservation agent {}", retrieveAgent.getId());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.addPluginDetails(
              "Failed to create bundle for " + retrieveAgent.getClass() + " " + retrieveAgent.getId() + "\n");
            reportItem.addPluginDetails(e.getMessage());
            pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
            PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
          }
        }
        updateEntityPackageState(IndexedPreservationAgent.class, idList);
      } catch (RODAException e) {
        LOGGER.error("Error on retrieve indexes of a RODA entity", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } catch (IOException e) {
        LOGGER.error("Error on update entity package state file", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    }
  }

  public void createAgentBundle(ModelService model, PreservationMetadata agent) throws RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath agentStoragePath = ModelUtils.getPreservationAgentStoragePath();
    String agentFile = FSUtils.encodePathPartial(agent.getId() + RodaConstants.PREMIS_SUFFIX);

    Path destinationPath = getDestinationPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION).resolve(RodaConstants.STORAGE_DIRECTORY_AGENTS);

    Path agentPath = destinationPath.resolve(agentFile);

    storage.copy(storage, agentStoragePath, agentPath, agentFile);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CreatePreservationAgentPackagePlugin();
  }
}
