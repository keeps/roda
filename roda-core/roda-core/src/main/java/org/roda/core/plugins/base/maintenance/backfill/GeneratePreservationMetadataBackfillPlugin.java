/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.backfill;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.base.maintenance.backfill.beans.Add;
import org.roda.core.plugins.base.maintenance.backfill.beans.DocType;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratePreservationMetadataBackfillPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePreservationMetadataBackfillPlugin.class);
  private String outputDirectory = ".";
  private boolean onlyGenerateInventory = false;
  private Date startDate = null;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY, "Output directory",
          PluginParameter.PluginParameterType.STRING)
        .withDefaultValue(".").isMandatory(true).withDescription("This job's output directory path").build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY, "Only generate inventory",
          PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("false").isMandatory(true)
        .withDescription(
          "Whether this job should only generate the inventory of RODA objects and not the index backfill files")
        .build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_START_DATE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_START_DATE, "Object starting date",
          PluginParameter.PluginParameterType.STRING)
        .isMandatory(false).withDescription(
          "The last modified data for source objects to process. If not set, all objects will be processed.")
        .build());
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return List.of(Void.class);
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Generate Preservation Agents index backfill";
  }

  @Override
  public String getDescription() {
    return "Description of example plugin";
  }

  @Override
  public String getVersionImpl() {
    // Get from pom.xml <version>
    return getClass().getPackage().getImplementationVersion();
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY)) {
      outputDirectory = parameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY);
    }
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY)) {
      onlyGenerateInventory = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY));
    }
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_START_DATE)) {
      String dateString = parameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE);
      if (!dateString.isEmpty()) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
          startDate = formatter.parse(parameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE));
        } catch (ParseException e) {
          throw new InvalidParameterException(e);
        }
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper
      .processVoids(this,
              (index1, model1, storage1, report, cachedJob, jobPluginInfo,
                plugin) -> generateBackfill(model1, index1, storage1, report, jobPluginInfo, cachedJob),
        index, model, storage);
  }

  protected void generateBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) {
    generateAgentsBackfill(model, index, storage, report, jobPluginInfo, cachedJob);
    generateEventsBackfill(model, index, storage, report, jobPluginInfo, cachedJob);
  }

  protected void generateAgentsBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) {
    // TODO: Report
    List<String> processedIds = new LinkedList<>();

    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationObjects;
    // TODO: Handle exceptions
    try {
      preservationObjects = model.listPreservationAgents();
    } catch (GenericException e) {
      throw new RuntimeException(e);
    } catch (AuthorizationDeniedException e) {
      throw new RuntimeException(e);
    } catch (RequestNotValidException e) {
      throw new RuntimeException(e);
    }
    // TODO: Get this from config
    int batchSize = 100;
    int blockSize = 10;
    Add addBean = new Add();
    int docCount = 0;
    int addCount = 0;
    for (OptionalWithCause<PreservationMetadata> preservationObject : preservationObjects) {
      if (preservationObject.isPresent()) {
        // TODO Handle exceptions
        try {
          DocType docBean = GenerateBackfillPluginUtils.toDocBean(preservationObject.get(),
            IndexedPreservationAgent.class);
          addBean.getDoc().add(docBean);
          processedIds.addLast(preservationObject.get().getId());

          docCount++;
          if (docCount >= blockSize * batchSize) {
            StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory,
              IndexedPreservationAgent.class, Integer.toString(addCount));
            GenerateBackfillPluginUtils.writeAddBean(storage, addPath, addBean);
            addBean = new Add();
            addCount++;
            docCount = 0;
          }
        } catch (AuthorizationDeniedException e) {
          throw new RuntimeException(e);
        } catch (RequestNotValidException e) {
          throw new RuntimeException(e);
        } catch (NotFoundException e) {
          throw new RuntimeException(e);
        } catch (NotSupportedException e) {
          throw new RuntimeException(e);
        } catch (GenericException e) {
          throw new RuntimeException(e);
        } catch (AlreadyExistsException e) {
          throw new RuntimeException(e);
        }
      }
    }
    // TODO Handle exceptions
    try {
      preservationObjects.close();
      if (docCount > 0) {
        StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory,
          IndexedPreservationAgent.class, Integer.toString(addCount));
        GenerateBackfillPluginUtils.writeAddBean(storage, addPath, addBean);
      }
      StoragePath inventoryPath = GenerateBackfillPluginUtils.constructInventoryOutputPath(outputDirectory,
        IndexedPreservationAgent.class);
      GenerateBackfillPluginUtils.writeInventoryPartial(storage, inventoryPath, processedIds);
    } catch (AlreadyExistsException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void generateEventsBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) {
    // TODO: Report
    List<String> processedIds = new LinkedList<>();

    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationObjects;
    // TODO: Handle exceptions
    try {
      preservationObjects = model.listPreservationRepositoryEvents();
    } catch (GenericException e) {
      throw new RuntimeException(e);
    } catch (AuthorizationDeniedException e) {
      throw new RuntimeException(e);
    } catch (RequestNotValidException e) {
      throw new RuntimeException(e);
    }
    // TODO: Get this from config
    int batchSize = 100;
    int blockSize = 10;
    Add addBean = new Add();
    int docCount = 0;
    int addCount = 0;
    for (OptionalWithCause<PreservationMetadata> preservationObject : preservationObjects) {
      if (preservationObject.isPresent()) {
        // TODO Handle exceptions
        try {
          DocType docBean = GenerateBackfillPluginUtils.toDocBean(preservationObject.get(),
            IndexedPreservationEvent.class);
          addBean.getDoc().add(docBean);
          processedIds.addLast(preservationObject.get().getId());

          docCount++;
          if (docCount >= blockSize * batchSize) {
            StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory,
              IndexedPreservationEvent.class, Integer.toString(addCount));
            GenerateBackfillPluginUtils.writeAddBean(storage, addPath, addBean);
            addBean = new Add();
            addCount++;
            docCount = 0;
          }
        } catch (AuthorizationDeniedException e) {
          throw new RuntimeException(e);
        } catch (RequestNotValidException e) {
          throw new RuntimeException(e);
        } catch (NotFoundException e) {
          throw new RuntimeException(e);
        } catch (NotSupportedException e) {
          throw new RuntimeException(e);
        } catch (GenericException e) {
          throw new RuntimeException(e);
        } catch (AlreadyExistsException e) {
          throw new RuntimeException(e);
        }
      }
    }
    // TODO Handle exceptions
    try {
      preservationObjects.close();
      if (docCount > 0) {
        StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory,
          IndexedPreservationEvent.class, Integer.toString(addCount));
        GenerateBackfillPluginUtils.writeAddBean(storage, addPath, addBean);
      }
      StoragePath inventoryPath = GenerateBackfillPluginUtils.constructInventoryOutputPath(outputDirectory,
        IndexedPreservationEvent.class);
      GenerateBackfillPluginUtils.writeInventoryPartial(storage, inventoryPath, processedIds);
    } catch (AlreadyExistsException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.MIGRATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Checked if ...";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "... with success.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to ...";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_EXPERIMENTAL);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new GeneratePreservationMetadataBackfillPlugin();
  }
}
