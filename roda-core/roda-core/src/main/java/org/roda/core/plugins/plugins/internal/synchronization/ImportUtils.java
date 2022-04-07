package org.roda.core.plugins.plugins.internal.synchronization;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.roda.core.common.SyncUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.utils.CentralEntitiesJsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.CentralEntities;
import org.roda.core.data.v2.synchronization.bundle.EntitiesBundle;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.internal.DeleteRodaObjectPluginUtils;
import org.roda.core.plugins.plugins.reindex.ReindexPreservationAgentPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexPreservationRepositoryEventPlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class ImportUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImportUtils.class);

  private ImportUtils() {
    // do nothing
  }

  /**
   * Reindex the bundle from Local.
   * 
   * @param model
   *          {@link ModelService}.
   * @param index
   *          {@link IndexService}.
   * @param bundleState
   *          {@link BundleState}
   * @param jobPluginInfo
   *          {@link JobPluginInfo}.
   * @param reportItem
   *          {@link Report}.
   */
  public static void reindexBundle(final ModelService model, final IndexService index, final BundleState bundleState,
    final JobPluginInfo jobPluginInfo, final Report reportItem) {
    final List<PackageState> packageStateList = bundleState.getPackageStateList();
    int total = 0;
    for (PackageState packageState : packageStateList) {
      if (packageState.getCount() > 0) {
        total += packageState.getCount();
        jobPluginInfo.setSourceObjectsCount(total);
        try {
          if (PluginHelper.getReindexPluginName(packageState.getClassName())
            .equals(ReindexPreservationAgentPlugin.class.getName())) {
            // acrescentar método para reindexar preservation agents
            reindexPreservationAgent(model, jobPluginInfo, reportItem);
          } else if (PluginHelper.getReindexPluginName(packageState.getClassName())
            .equals(ReindexPreservationRepositoryEventPlugin.class.getName())) {
            // acrescentar método para reindexar preservation repository Event
            reindexPreservationRepositoryEvent(model, jobPluginInfo, reportItem);
          } else {
            defaultReindex(model, index, jobPluginInfo, packageState, reportItem);
          }
        } catch (final NotFoundException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.addPluginDetails(e.getMessage());
          reportItem.setPluginState(PluginState.FAILURE);
        }
      }
    }
  }

  /**
   * Do the reindex to all entities excepts {@link IndexedPreservationEvent} or
   * {@link IndexedPreservationAgent}.
   * 
   * @param model
   *          {@link ModelService}
   * @param index
   *          {@link IndexService}
   * @param jobPluginInfo
   *          {@link JobPluginInfo}
   * @param packageState
   *          {@link PackageState}
   * @param reportItem
   *          {@link Report}
   */
  private static void defaultReindex(final ModelService model, final IndexService index,
    final JobPluginInfo jobPluginInfo, final PackageState packageState, final Report reportItem) {
    final List<? extends IsRODAObject> objectsToReindex;
    try {
      objectsToReindex = JobsHelper.getObjectsFromUUID(model, index, packageState.getClassName(),
        packageState.getIdList());
      for (IsRODAObject object : objectsToReindex) {
        final ReturnWithExceptions<Void, ModelObserver> exceptions = index.reindex(object);
        final List<Exception> exceptionList = exceptions.getExceptions();
        if (exceptionList.isEmpty()) {
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();

          for (Exception e : exceptionList) {
            reportItem.addPluginDetails(e.getMessage() + "\n");
          }
          reportItem.setPluginState(PluginState.FAILURE);
        }
      }
    } catch (Exception e) {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.addPluginDetails(e.getMessage());
      reportItem.setPluginState(PluginState.FAILURE);
    }
  }

  /**
   * Do the reindex to {@link IndexedPreservationAgent}.
   * 
   * @param model
   *          {@link ModelService}
   * @param jobPluginInfo
   *          {@link JobPluginInfo}
   * @param reportItem
   *          {@link Report}
   */
  private static void reindexPreservationAgent(final ModelService model, final JobPluginInfo jobPluginInfo,
    final Report reportItem) {
    reportItem.setPluginState(PluginState.SUCCESS);
    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> iterable = model.listPreservationAgents()) {

      for (OptionalWithCause<PreservationMetadata> opm : iterable) {
        if (opm.isPresent()) {
          model.notifyPreservationMetadataCreated(opm.get()).failOnError();
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .addPluginDetails("Could not add preservation agent: " + opm.getCause());
        }
      }
    } catch (Exception e) {
      LOGGER.error("Error getting preservation agents to be reindexed", e);
      reportItem.setPluginState(PluginState.FAILURE);
    }
  }

  /**
   * Do the reindex to {@link IndexedPreservationEvent}.
   * 
   * @param model
   *          {@link ModelService}.
   * @param jobPluginInfo
   *          {@link JobPluginInfo}.
   * @param reportItem
   *          {@link Report}
   */
  private static void reindexPreservationRepositoryEvent(final ModelService model, final JobPluginInfo jobPluginInfo,
    final Report reportItem) {
    reportItem.setPluginState(PluginState.SUCCESS);
    try (
      CloseableIterable<OptionalWithCause<PreservationMetadata>> iterable = model.listPreservationRepositoryEvents()) {

      for (OptionalWithCause<PreservationMetadata> opm : iterable) {
        if (opm.isPresent()) {
          model.notifyPreservationMetadataCreated(opm.get()).failOnError();
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .addPluginDetails("Could not add preservation repository event: " + opm.getCause());
        }
      }

    } catch (final Exception e) {
      LOGGER.error("Error reindexing RODA entity", e);
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails("Could not list preservation repository events");
    }
  }

  public static void deleteBundleEntities(final ModelService model, final IndexService index, StorageService storage,
    final Job cachedJob, Plugin<? extends IsRODAObject> plugin, final JobPluginInfo jobPluginInfo,
    final String instanceIdentifier, final Path bundleWorkingDir, final EntitiesBundle entitiesBundle,
    final Report reportItem) {
    final Map<Path, Class<? extends IsIndexed>> entitiesPathMap = SyncUtils.createEntitiesPaths(bundleWorkingDir,
      entitiesBundle);
    final CentralEntities centralEntities = new CentralEntities();
    for (Map.Entry entry : entitiesPathMap.entrySet()) {
      deleteRodaObject(model, index, cachedJob, jobPluginInfo, plugin, (Path) entry.getKey(),
        (Class<? extends IsIndexed>) entry.getValue(), instanceIdentifier, centralEntities, reportItem);
    }
  }

  private static void deleteRodaObject(final ModelService model, final IndexService index, final Job cachedJob,
    final JobPluginInfo jobPluginInfo, Plugin<? extends IsRODAObject> plugin, final Path readPath,
    final Class<? extends IsIndexed> indexedClass, final String instanceIdentifier,
    final CentralEntities centralEntities, final Report report) {
    final List<String> listToRemove = getListToRemove(index, readPath, indexedClass, instanceIdentifier);

    if (!listToRemove.isEmpty()) {
      try {
        final List<? extends IsRODAObject> objectsToRemove = JobsHelper.getObjectsFromUUID(model, index,
          ModelUtils.giveRespectiveModelClass(indexedClass), listToRemove);
        int sourceObjects = jobPluginInfo.getSourceObjectsCount() + listToRemove.size();
        jobPluginInfo.setSourceObjectsCount(sourceObjects);
        for (IsRODAObject object : objectsToRemove) {
          DeleteRodaObjectPluginUtils.process(index, model, report, cachedJob, jobPluginInfo, plugin, object, "", true,
            false);
        }

      } catch (final Exception e) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        report.addPluginDetails(e.getMessage());
        report.setPluginState(PluginState.FAILURE);
      }

    }
  }

  private static List<String> getListToRemove(final IndexService index, final Path readPath,
    final Class<? extends IsIndexed> indexedClass, final String instanceIdentifier) {
    final List<String> listToRemove = new ArrayList<>();

    // Create Filter to SOLR query
    final Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_INSTANCE_ID, instanceIdentifier));
    if (indexedClass == IndexedPreservationEvent.class) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
        IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    }

    // Iterate over SOLR query result and the JSON file in bundle
    try (IterableIndexResult<? extends IsIndexed> result = index.findAll(indexedClass, filter, true,
      Collections.singletonList(RodaConstants.INDEX_UUID))) {
      result.forEach(indexed -> {
        boolean exist = false;
        try {
          final JsonParser jsonParser = CentralEntitiesJsonUtils.createJsonParser(readPath);
          while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            JsonToken token = jsonParser.currentToken();
            if ((token != JsonToken.START_ARRAY) && (token != JsonToken.END_ARRAY)
              && jsonParser.getText().equals(indexed.getId())) {
              exist = true;
            }
          }
          if (!exist) {
            listToRemove.add(indexed.getId());
          }
        } catch (final IOException e) {
          LOGGER.error("Can't read the json file {}", e.getMessage());
        }
      });

    } catch (IOException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting AIP iterator when creating aip list", e);
    }
    return listToRemove;
  }
}
