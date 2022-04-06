package org.roda.core.plugins.plugins.internal.synchronization;

import java.util.List;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.reindex.ReindexPreservationAgentPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexPreservationRepositoryEventPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    jobPluginInfo.setSourceObjectsCount(packageStateList.size());
    for (PackageState packageState : packageStateList) {
      if (packageState.getCount() > 0) {
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
      int agentCounter = 0;

      for (OptionalWithCause<PreservationMetadata> opm : iterable) {
        if (opm.isPresent()) {
          model.notifyPreservationMetadataCreated(opm.get()).failOnError();
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .addPluginDetails("Could not add preservation agent: " + opm.getCause());
        }
        agentCounter++;
      }
      jobPluginInfo.setSourceObjectsCount(agentCounter);
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
      int eventCounter = 0;

      for (OptionalWithCause<PreservationMetadata> opm : iterable) {
        if (opm.isPresent()) {
          model.notifyPreservationMetadataCreated(opm.get()).failOnError();
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .addPluginDetails("Could not add preservation repository event: " + opm.getCause());
        }
        eventCounter++;
      }

      jobPluginInfo.setSourceObjectsCount(eventCounter);
    } catch (final Exception e) {
      LOGGER.error("Error reindexing RODA entity", e);
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails("Could not list preservation repository events");
    }

  }
}
