/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.backfill;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.roda.core.RodaCoreFactory;
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
import org.roda.core.data.utils.XMLUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.base.maintenance.backfill.beans.DocType;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;

public abstract class GenerateRODAEntityBackfillPlugin<T extends IsRODAObject & IsModelObject>
  extends AbstractPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateRODAEntityBackfillPlugin.class);
  private String outputDirectory = ".";
  private boolean onlyGenerateInventory = false;

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
  }

  protected GenerateRODAEntityBackfillPlugin() {
    super();
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Generate " + getObjectClasses().getFirst().getSimpleName() + " index backfill";
  }

  @Override
  public String getDescription() {
    return "Description of example plugin";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY));
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
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    Exception thrown = null;
    return PluginHelper.processObjects(this,
      (RODAObjectsProcessingLogic<T>) (index1, model1, storage1, report, cachedJob, jobPluginInfo, plugin,
        objects) -> {
          try {
              generateBackfill(model1, index1, storage1, report, jobPluginInfo, cachedJob, objects);
          } catch (PluginException e) {
              throw new RuntimeException(e);
          }
      },
      index, model, storage, liteList);
  }

  protected void generateBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job job, List<T> objects) throws PluginException {
    StringBuilder docXMLs = new StringBuilder();
    List<String> processedIds = new LinkedList<>();
    String batchId = IdUtils.createUUID();
    report.setPluginState(PluginState.SUCCESS);

    for (T object : objects) {
      try {
        if (!onlyGenerateInventory) {
          DocType docBean = GenerateBackfillPluginUtils.toDocBean(object, getIndexClass());
          JAXBElement<DocType> rootElement = new JAXBElement<>(new QName("doc"), DocType.class, docBean);
          docXMLs.append(XMLUtils.getXMLFragFromObject(rootElement));
        }
        processedIds.addLast(getObjectId(object));
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      } catch (NotSupportedException | GenericException | RequestNotValidException | NotFoundException
        | AuthorizationDeniedException e) {
        report.setPluginState(PluginState.FAILURE);
        report.addPluginDetails("Exception occurred while processing object: " + e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        throw new PluginException(e);
      }
    }

    try {
      if (!onlyGenerateInventory) {
        StoragePath addPartialPath = GenerateBackfillPluginUtils.constructAddPartialOutputPath(outputDirectory,
          objects.getFirst().getClass(), batchId);
        GenerateBackfillPluginUtils.writeAddPartial(storage, addPartialPath, docXMLs.toString());
      }
      StoragePath inventoryPartialPath = GenerateBackfillPluginUtils
        .constructInventoryPartialOutputPath(outputDirectory, objects.getFirst().getClass(), batchId);
      GenerateBackfillPluginUtils.writeInventoryPartial(storage, inventoryPartialPath, processedIds);
    } catch (AlreadyExistsException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException e) {
      report.setPluginState(PluginState.FAILURE);
      report.addPluginDetails("Exception occurred while writing generated XML: " + e.getMessage());
      throw new PluginException(e);
    }
  }

  protected abstract <I extends IsIndexed> Class<I> getIndexClass();

  protected abstract String getObjectId(T object);

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
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
    Report report = new Report();
    if (!onlyGenerateInventory) {
      report.addReport(concatenateAndWriteAdds(storage));
    }
    report.addReport(concatenateAndWriteInventory(storage));
    report.setPluginState(
      report.getReports().stream().allMatch(r -> r.getPluginState().equals(PluginState.SUCCESS)) ? PluginState.SUCCESS
        : PluginState.FAILURE);
    return new Report();
  }

  protected Report concatenateAndWriteAdds(StorageService storage) {
    Report report = new Report();
    report.setPluginState(PluginState.SUCCESS);
    int blockSize = RodaCoreFactory.getRodaConfigurationAsInt("core", "plugins", "internal", "backfill",
      "blockBatches");
    if (blockSize == 0) {
      blockSize = 10;
    }
    try {
      StoragePath addPartialsDirectory = GenerateBackfillPluginUtils.constructAddPartialsDirectoryPath(outputDirectory,
        getObjectClasses().getFirst());
      CloseableIterable<Resource> addPartials = storage.listResourcesUnderDirectory(addPartialsDirectory, false);
      StringBuilder addStringBuilder = new StringBuilder();
      addStringBuilder.append("<add>");
      int addedBatches = 0;
      int writtenBlocks = 0;
      for (Resource addPartialResource : addPartials) {
        try (InputStream addPartialStream = ((DefaultBinary) addPartialResource).getContent().createInputStream()) {
          addStringBuilder.append(new String(addPartialStream.readAllBytes(), StandardCharsets.UTF_8));
        }
        addedBatches++;
        if (addedBatches > blockSize) {
          addStringBuilder.append("</add>");
          StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory,
            getObjectClasses().getFirst(), Integer.toString(writtenBlocks));
          storage.createBinary(addPath, new StringContentPayload(addStringBuilder.toString()), false);

          addStringBuilder = new StringBuilder();
          addStringBuilder.append("<add>");
          addedBatches = 0;
          writtenBlocks++;
        }
        storage.deleteResource(addPartialResource.getStoragePath());
      }
      addPartials.close();
      if (addedBatches > 0) {
        addStringBuilder.append("</add>");
        StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory,
          getObjectClasses().getFirst(), Integer.toString(writtenBlocks));
        storage.createBinary(addPath, new StringContentPayload(addStringBuilder.toString()), false);
      }
    } catch (RequestNotValidException | IOException | NotFoundException | GenericException
      | AuthorizationDeniedException | AlreadyExistsException e) {
      report.setPluginState(PluginState.FAILURE);
      report.addPluginDetails("Exception during document concatenation: " + e.getMessage());
    }
    return report;
  }

  protected Report concatenateAndWriteInventory(StorageService storage) {
    Report report = new Report();
    try {
      StoragePath inventoryPartialsDirectory = GenerateBackfillPluginUtils
        .constructInventoryPartialsDirectoryPath(outputDirectory, getObjectClasses().getFirst());
      CloseableIterable<Resource> inventoryPartials = storage.listResourcesUnderDirectory(inventoryPartialsDirectory,
        false);
      StringBuilder inventoryStringBuilder = new StringBuilder();
      for (Resource inventoryPartialResource : inventoryPartials) {
        try (InputStream inventoryPartialStream = ((DefaultBinary) inventoryPartialResource).getContent()
          .createInputStream()) {
          inventoryStringBuilder.append(new String(inventoryPartialStream.readAllBytes(), StandardCharsets.UTF_8));
        }
        inventoryStringBuilder.append("\n");
        storage.deleteResource(inventoryPartialResource.getStoragePath());
      }
      inventoryPartials.close();
      StoragePath inventoryPath = GenerateBackfillPluginUtils.constructInventoryOutputPath(outputDirectory,
        getObjectClasses().getFirst());
      storage.createBinary(inventoryPath, new StringContentPayload(inventoryStringBuilder.toString()), false);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IOException | AlreadyExistsException e) {
      report.setPluginState(PluginState.FAILURE);
      report.addPluginDetails("Exception during inventory concatenation: " + e.getMessage());
    }
    return report;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_EXPERIMENTAL);
  }
}
