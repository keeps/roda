package org.roda.core.plugins.plugins.synchronization.plugins.calculateDeleteEntities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.CentralEntitiesJsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.CentralEntities;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class CalculateDeleteAIPPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CalculateDeleteAIPPlugin.class);
  private String bundlePath = null;
  private String instanceIdentifier = null;
  private List<String> aipsToRemove = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  @Override
  public List<PluginParameter> getParameters() {
    final ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER));
    return parameters;
  }

  @Override
  public void setParameterValues(final Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "Calculate List of AIP's to Remove";
  }

  @Override
  public String getDescription() {
    return "Calculate List of AIP's to Remove";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "";
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CalculateDeleteAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return false;
  }

  @Override
  public void init() throws PluginException {

  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Collections.singletonList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        calculateDeleteAIP(model, storage, report, cachedJob, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void calculateDeleteAIP(ModelService model, StorageService storage, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo) {
    if (Files.exists(Paths.get(bundlePath))) {
      Path bundleWorkingDir = null;
      try {
        bundleWorkingDir = SyncUtils.extractBundle(instanceIdentifier, Paths.get(bundlePath));
        // Vai ser usado nos outros plugins para já fica mas depois vai ser retirado
        BundleState bundleState = SyncUtils.getIncomingBundleState(instanceIdentifier);

        SyncUtils.copyAttachments(instanceIdentifier);
      } catch (IOException e) {
        LOGGER.error("Error extracting bundle to {}", bundleWorkingDir.toString(), e);
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error extracting bundle to " + bundleWorkingDir.toString());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } catch (GenericException e) {
        e.printStackTrace();
      } catch (RODAException e) {
        LOGGER.error("Error creating temporary StorageService on {}", bundleWorkingDir.toString(), e);
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error creating temporary StorageService on " + bundleWorkingDir.toString());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }

    } else {
      report.setPluginState(PluginState.FAILURE).setPluginDetails("Cannot find bundle on path " + bundlePath);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }
  }

  /**
   * Checks and delete entities from Central instance.
   *
   * @param readPath
   *          {@link Path}.
   * @param indexedClass
   *          {@link Class<? extends IsIndexed >}.
   * @param instanceIdentifier
   *          Instance identifier.
   * @param centralEntities
   *          {@link CentralEntities}.
   * @throws GenericException
   *           if some error occurs.
   * @throws AuthorizationDeniedException
   *           if does not have permission to do this action.
   * @throws RequestNotValidException
   *           if the request is not valid
   * @throws NotFoundException
   *           if some error occurs
   * @throws JobAlreadyStartedException
   *           if the job is already in execution.
   */
  private void deleteBundleEntities(final Path readPath, final Class<? extends IsIndexed> indexedClass,
    final String instanceIdentifier, final CentralEntities centralEntities) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, JobAlreadyStartedException {
    final List<String> listToRemove = new ArrayList<>();
    final IndexService index = RodaCoreFactory.getIndexService();
    final Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_INSTANCE_ID, instanceIdentifier));
    if (indexedClass == IndexedPreservationEvent.class) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
        IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    }

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
        } catch (IOException e) {
          LOGGER.error("Can't read the json file {}", e.getMessage());
        }
      });

    } catch (IOException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting AIP iterator when creating aip list", e);
    }

    if (!listToRemove.isEmpty()) {
      aipsToRemove = new ArrayList<>(listToRemove);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
